package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.backend.entity.AnswerRecord;
import org.example.backend.entity.ErrorBook;
import org.example.backend.entity.ExamRecord;
import org.example.backend.entity.KnowledgePointStat;
import org.example.backend.entity.Question;
import org.example.backend.mapper.AnswerRecordMapper;
import org.example.backend.mapper.ErrorBookMapper;
import org.example.backend.mapper.ExamRecordMapper;
import org.example.backend.mapper.KnowledgePointStatMapper;
import org.example.backend.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    private final QuestionMapper questionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ErrorBookMapper errorBookMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final KnowledgePointStatMapper statMapper;

    public AnalyticsService(QuestionMapper questionMapper, ExamRecordMapper examRecordMapper,
                            ErrorBookMapper errorBookMapper, AnswerRecordMapper answerRecordMapper,
                            KnowledgePointStatMapper statMapper) {
        this.questionMapper = questionMapper;
        this.examRecordMapper = examRecordMapper;
        this.errorBookMapper = errorBookMapper;
        this.answerRecordMapper = answerRecordMapper;
        this.statMapper = statMapper;
    }

    public Map<String, Object> overview(Long userId) {
        long questionCount = questionMapper.selectCount(new LambdaQueryWrapper<Question>().eq(Question::getUserId, userId));
        long examCount = examRecordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getUserId, userId));
        long errorCount = errorBookMapper.selectCount(new LambdaQueryWrapper<ErrorBook>().eq(ErrorBook::getUserId, userId));
        List<KnowledgePointStat> stats = statMapper.selectList(new LambdaQueryWrapper<KnowledgePointStat>().eq(KnowledgePointStat::getUserId, userId));
        BigDecimal avg = stats.isEmpty() ? BigDecimal.ZERO : stats.stream()
                .map(KnowledgePointStat::getMasteryDegree)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(stats.size()), 2, RoundingMode.HALF_UP);
        Map<String, Object> data = new HashMap<>();
        data.put("questionCount", questionCount);
        data.put("examCount", examCount);
        data.put("errorCount", errorCount);
        data.put("averageMastery", avg);
        data.put("knowledgeStats", stats);
        return data;
    }

    public void rebuildStats(Long userId) {
        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>().eq(Question::getUserId, userId));
        Map<Long, List<Question>> byKnowledge = questions.stream().collect(java.util.stream.Collectors.groupingBy(Question::getKnowledgePointId));
        for (Map.Entry<Long, List<Question>> entry : byKnowledge.entrySet()) {
            List<Long> questionIds = entry.getValue().stream().map(Question::getId).toList();
            List<AnswerRecord> answers = answerRecordMapper.selectList(new LambdaQueryWrapper<AnswerRecord>()
                    .eq(AnswerRecord::getUserId, userId)
                    .in(AnswerRecord::getQuestionId, questionIds));
            int total = answers.size();
            int correct = (int) answers.stream().filter(a -> a.getCorrectStatus() == 1).count();
            KnowledgePointStat stat = statMapper.selectOne(new LambdaQueryWrapper<KnowledgePointStat>()
                    .eq(KnowledgePointStat::getUserId, userId)
                    .eq(KnowledgePointStat::getKnowledgePointId, entry.getKey()));
            if (stat == null) {
                stat = new KnowledgePointStat();
                stat.setUserId(userId);
                stat.setKnowledgePointId(entry.getKey());
            }
            stat.setTotalQuestions(total);
            stat.setCorrectCount(correct);
            stat.setMasteryDegree(total == 0 ? BigDecimal.ZERO :
                    BigDecimal.valueOf(correct * 100.0 / total).setScale(2, RoundingMode.HALF_UP));
            stat.setLastTestTime(LocalDateTime.now());
            if (stat.getId() == null) {
                statMapper.insert(stat);
            } else {
                statMapper.updateById(stat);
            }
        }
    }
}
