package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.backend.dto.ImportDtos.ImportResult;
import org.example.backend.entity.KnowledgePoint;
import org.example.backend.entity.Question;
import org.example.backend.entity.Subject;
import org.example.backend.mapper.KnowledgePointMapper;
import org.example.backend.mapper.QuestionMapper;
import org.example.backend.mapper.SubjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportService {
    private final SubjectMapper subjectMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final QuestionMapper questionMapper;

    public ImportService(SubjectMapper sm, KnowledgePointMapper kpm, QuestionMapper qm) {
        this.subjectMapper = sm;
        this.knowledgePointMapper = kpm;
        this.questionMapper = qm;
    }

    private static class ColumnMapping {
        int subjectCol = -1;
        int pointCol = -1;
        int typeCol = -1;
        int stemCol = -1;
        int answerCol = -1;
        int difficultyCol = -1;
        int analysisCol = -1;
        List<Integer> optionCols = new ArrayList<>();
    }

    public ImportResult importExcel(Long userId, MultipartFile file, Long subjectId, Long knowledgePointId) throws Exception {
        List<String> errors = new ArrayList<>();
        int success = 0;
        boolean useExternalCatalog = (subjectId != null && knowledgePointId != null);
        try (var workbook = WorkbookFactory.create(file.getInputStream())) {
            var sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add("表头行为空，无法解析");
                return new ImportResult(0, 1, errors);
            }
            ColumnMapping mapping = parseHeader(headerRow);
            if (mapping.stemCol < 0) {
                errors.add("格式错误：缺少题目列（表头需包含\"题目\"或\"题干\"）");
                return new ImportResult(0, 1, errors);
            }
            if (mapping.answerCol < 0) {
                errors.add("格式错误：缺少答案列（表头需包含\"答案\"或\"正确答案\"）");
                return new ImportResult(0, 1, errors);
            }
            if (mapping.typeCol < 0) {
                errors.add("格式错误：缺少题型列（表头需包含\"题型\"）");
                return new ImportResult(0, 1, errors);
            }
            if (!useExternalCatalog && mapping.subjectCol < 0) {
                errors.add("格式错误：未通过页面指定科目，且Excel缺少\"科目\"列");
                return new ImportResult(0, 1, errors);
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String stem = text(row, mapping.stemCol);
                    if (stem.isBlank()) continue;

                    Long resolvedSubjectId;
                    Long resolvedPointId;
                    if (useExternalCatalog) {
                        resolvedSubjectId = subjectId;
                        resolvedPointId = knowledgePointId;
                    } else {
                        String subjectName = text(row, mapping.subjectCol);
                        if (subjectName.isBlank()) {
                            errors.add("第 " + (i + 1) + " 行：科目为空");
                            continue;
                        }
                        Subject subject = findOrCreateSubject(userId, subjectName);
                        resolvedSubjectId = subject.getId();
                        if (mapping.pointCol >= 0 && !text(row, mapping.pointCol).isBlank()) {
                            KnowledgePoint point = findOrCreatePoint(userId, subject.getId(), text(row, mapping.pointCol));
                            resolvedPointId = point.getId();
                        } else {
                            resolvedPointId = null;
                        }
                    }

                    String type = mapping.typeCol >= 0 ? normalizeType(text(row, mapping.typeCol)) : "single";
                    if (type.isBlank()) type = "single";

                    Question q = new Question();
                    q.setUserId(userId);
                    q.setSubjectId(resolvedSubjectId);
                    q.setKnowledgePointId(resolvedPointId);
                    q.setType(type);
                    q.setStem(stem);

                    if (!mapping.optionCols.isEmpty()) {
                        List<String> options = new ArrayList<>();
                        for (int col : mapping.optionCols) {
                            options.add(text(row, col));
                        }
                        q.setOptionsJson(buildOptionsJson(type, options));
                        String rawAnswer = text(row, mapping.answerCol);
                        q.setAnswer(normalizeAnswer(rawAnswer, mapping.optionCols.size()));
                    } else {
                        q.setOptionsJson(buildOptionsJson(type, List.of()));
                        q.setAnswer(text(row, mapping.answerCol));
                    }

                    int difficulty = mapping.difficultyCol >= 0 ? parseDifficulty(text(row, mapping.difficultyCol)) : 1;
                    q.setDifficulty(difficulty);
                    String analysis = mapping.analysisCol >= 0 ? text(row, mapping.analysisCol) : "";
                    q.setAnalysis(analysis);

                    questionMapper.insert(q);
                    success++;
                } catch (Exception ex) {
                    errors.add("第 " + (i + 1) + " 行导入失败：" + ex.getMessage());
                }
            }
        }
        return new ImportResult(success, errors.size(), errors);
    }

    private ColumnMapping parseHeader(Row headerRow) {
        ColumnMapping mapping = new ColumnMapping();
        for (int i = 0; i < 30; i++) {
            String header = text(headerRow, i);
            if (header.isBlank()) continue;
            String h = header.toLowerCase().trim();

            if (h.equals("科目") || h.equals("subject")) {
                mapping.subjectCol = i;
            } else if (h.equals("知识点") || h.equals("knowledge") || h.equals("knowledge point")) {
                mapping.pointCol = i;
            } else if (h.equals("题型") || h.equals("type") || h.equals("题目类型")) {
                mapping.typeCol = i;
            } else if (h.equals("题目") || h.equals("题干") || h.equals("stem") || h.equals("question")) {
                mapping.stemCol = i;
            } else if (h.matches("选项[a-zA-Z]") || h.matches("option[a-zA-Z]") || h.matches("[a-zA-Z]选项") || (h.length() == 1 && Character.isLetter(h.charAt(0)))) {
                mapping.optionCols.add(i);
            } else if (h.equals("正确答案") || h.equals("答案") || h.equals("answer")) {
                mapping.answerCol = i;
            } else if (h.equals("难度") || h.equals("difficulty")) {
                mapping.difficultyCol = i;
            } else if (h.equals("解析") || h.equals("analysis") || h.equals("explanation")) {
                mapping.analysisCol = i;
            }
        }
        return mapping;
    }

    private String buildOptionsJson(String type, List<String> options) {
        if ("judge".equals(type) && (options.isEmpty() || options.stream().allMatch(String::isBlank))) {
            options = List.of("正确", "错误");
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < options.size(); i++) {
            String optText = options.get(i);
            if (optText.isBlank()) continue;
            if (sb.length() > 1) sb.append(",");
            String key = String.valueOf((char) ('A' + i));
            sb.append("{\"key\":\"").append(key).append("\",\"text\":\"").append(escape(optText)).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String normalizeAnswer(String raw, int optionCount) {
        StringBuilder answer = new StringBuilder();
        for (int i = 0; i < optionCount; i++) {
            char letter = (char) ('A' + i);
            if (raw.toUpperCase().indexOf(letter) >= 0) {
                if (answer.length() > 0) answer.append(",");
                answer.append(letter);
            }
        }
        return answer.length() > 0 ? answer.toString() : raw;
    }

    private Subject findOrCreateSubject(Long userId, String name) {
        Subject subject = subjectMapper.selectOne(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getUserId, userId).eq(Subject::getName, name));
        if (subject != null) return subject;
        subject = new Subject();
        subject.setUserId(userId);
        subject.setName(name);
        subjectMapper.insert(subject);
        return subject;
    }

    private KnowledgePoint findOrCreatePoint(Long userId, Long subjectId, String name) {
        KnowledgePoint point = knowledgePointMapper.selectOne(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getUserId, userId)
                .eq(KnowledgePoint::getSubjectId, subjectId)
                .eq(KnowledgePoint::getName, name));
        if (point != null) return point;
        point = new KnowledgePoint();
        point.setUserId(userId);
        point.setSubjectId(subjectId);
        point.setName(name);
        knowledgePointMapper.insert(point);
        return point;
    }

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    private String text(Row row, int index) {
        if (index < 0) return "";
        var cell = row.getCell(index);
        if (cell == null) return "";
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String normalizeType(String type) {
        return switch (type) {
            case "单选" -> "single";
            case "多选" -> "multiple";
            case "填空" -> "blank";
            case "判断" -> "judge";
            case "简答" -> "short";
            case "计算" -> "calculate";
            default -> type;
        };
    }

    private int parseDifficulty(String value) {
        try {
            int d = Integer.parseInt(value);
            return Math.max(1, Math.min(5, d));
        } catch (Exception ignored) {
            return 1;
        }
    }
}
