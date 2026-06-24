package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.example.backend.common.BizException;
import org.example.backend.config.RabbitConfig;
import org.example.backend.dto.ExamDtos;
import org.example.backend.dto.ExamDtos.AnswerItem;
import org.example.backend.dto.ExamDtos.ExamPaper;
import org.example.backend.dto.ExamDtos.GenerateExamRequest;
import org.example.backend.dto.ExamDtos.SubmitExamRequest;
import org.example.backend.entity.AnswerRecord;
import org.example.backend.entity.ErrorBook;
import org.example.backend.entity.ExamRecord;
import org.example.backend.entity.Question;
import org.example.backend.mapper.AnswerRecordMapper;
import org.example.backend.mapper.ErrorBookMapper;
import org.example.backend.mapper.ExamRecordMapper;
import org.example.backend.mapper.QuestionMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamService {
    private final QuestionMapper questionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final ErrorBookMapper errorBookMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    public ExamService(QuestionMapper questionMapper, ExamRecordMapper examRecordMapper,
                       AnswerRecordMapper answerRecordMapper, ErrorBookMapper errorBookMapper,
                       RedisTemplate<String, Object> redisTemplate, RabbitTemplate rabbitTemplate) {
        this.questionMapper = questionMapper;
        this.examRecordMapper = examRecordMapper;
        this.answerRecordMapper = answerRecordMapper;
        this.errorBookMapper = errorBookMapper;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public ExamPaper generate(Long userId, GenerateExamRequest request) {
        String mode = request.mode() == null ? "random" : request.mode();
        int count = request.count() == null ? 10 : request.count();
        List<Question> questions = switch (mode) {
            case "knowledge" -> questionsByKnowledge(userId, request.knowledgePointId(), request, count);
            case "error" -> errorQuestions(userId, request, count);
            default -> randomQuestions(userId, request, count);
        };
        if (questions.isEmpty()) {
            throw new BizException(9020, "没有符合条件的题目");
        }
        String paperId = UUID.randomUUID().toString();
        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        redisTemplate.opsForValue().set(cacheKey(userId, paperId), questionIds, 30, TimeUnit.MINUTES);
        return new ExamPaper(paperId, mode, request.durationMinutes(), questions);
    }

    @Transactional
    public ExamRecord submit(Long userId, SubmitExamRequest request) {
        Object cached = redisTemplate.opsForValue().get(cacheKey(userId, request.paperId()));
        if (cached == null) {
            throw new BizException(9021, "试卷已过期，请重新出卷");
        }
        @SuppressWarnings("unchecked")
        List<Number> idNumbers = (List<Number>) cached;
        List<Long> questionIds = idNumbers.stream().map(Number::longValue).toList();
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        if (questions.isEmpty()) {
            throw new BizException(9021, "试卷已过期，请重新出卷");
        }
        ExamRecord exam = new ExamRecord();
        exam.setUserId(userId);
        exam.setName("自测试卷");
        exam.setMode(request.mode());
        exam.setTotalCount(questions.size());
        exam.setDurationSeconds(request.durationSeconds());
        exam.setStartTime(LocalDateTime.now().minusSeconds(request.durationSeconds() == null ? 0 : request.durationSeconds()));
        exam.setEndTime(LocalDateTime.now());

        int correct = 0;
        for (Question question : questions) {
            AnswerItem item = findAnswer(request.answers(), question.getId());
            int status = score(question, item);
            if (status == 1) {
                correct++;
            }
        }
        exam.setCorrectCount(correct);
        examRecordMapper.insert(exam);

        for (Question question : questions) {
            AnswerItem item = findAnswer(request.answers(), question.getId());
            int status = score(question, item);
            AnswerRecord answer = new AnswerRecord();
            answer.setUserId(userId);
            answer.setExamRecordId(exam.getId());
            answer.setQuestionId(question.getId());
            answer.setUserAnswer(item == null ? "" : item.answer());
            answer.setAnswerSeconds(item == null ? 0 : item.answerSeconds());
            answer.setCorrectStatus(status);
            answerRecordMapper.insert(answer);
            if (status == 0) {
                upsertError(userId, question.getId());
            }
        }
        redisTemplate.delete(cacheKey(userId, request.paperId()));
        rabbitTemplate.convertAndSend(RabbitConfig.STAT_QUEUE, exam.getId());
        return exam;
    }

    public List<ExamRecord> history(Long userId) {
        return examRecordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .orderByDesc(ExamRecord::getCreateTime));
    }

    public ExamDtos.ExamDetail detail(Long userId, Long examId) {
        ExamRecord exam = examRecordMapper.selectById(examId);
        if (exam == null || !userId.equals(exam.getUserId())) {
            throw new BizException(9023, "试卷记录不存在");
        }
        List<AnswerRecord> records = answerRecordMapper.selectList(new LambdaQueryWrapper<AnswerRecord>()
                .eq(AnswerRecord::getExamRecordId, examId));
        List<Long> qIds = records.stream().map(AnswerRecord::getQuestionId).toList();
        java.util.Map<Long, Question> qMap = qIds.isEmpty() ? java.util.Map.of() :
                questionMapper.selectBatchIds(qIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Question::getId, q -> q));

        List<ExamDtos.AnswerDetail> details = records.stream().map(r -> {
            Question q = qMap.get(r.getQuestionId());
            return new ExamDtos.AnswerDetail(
                    r.getQuestionId(),
                    q != null ? q.getStem() : "题目已删除",
                    q != null ? q.getType() : "",
                    q != null ? q.getOptionsJson() : "[]",
                    q != null ? q.getAnswer() : "",
                    r.getUserAnswer(),
                    r.getCorrectStatus());
        }).toList();
        return new ExamDtos.ExamDetail(exam, details);
    }

    private List<Question> randomQuestions(Long userId, GenerateExamRequest request, int count) {
        return questionMapper.selectList(baseQuestionQuery(userId, request)
                .last("ORDER BY RAND() LIMIT " + count));
    }

    private List<Question> questionsByKnowledge(Long userId, Long knowledgePointId, GenerateExamRequest request, int count) {
        if (knowledgePointId == null) {
            throw new BizException(9022, "请选择知识点");
        }
        return questionMapper.selectList(baseQuestionQuery(userId, request)
                .eq(Question::getKnowledgePointId, knowledgePointId)
                .last("ORDER BY RAND() LIMIT " + count));
    }

    private List<Question> errorQuestions(Long userId, GenerateExamRequest request, int count) {
        List<ErrorBook> errors = errorBookMapper.selectList(new LambdaQueryWrapper<ErrorBook>()
                .eq(ErrorBook::getUserId, userId)
                .orderByDesc(ErrorBook::getWrongCount)
                .last("LIMIT " + count));
        List<Long> ids = errors.stream().map(ErrorBook::getQuestionId).toList();
        if (ids.isEmpty()) {
            return randomQuestions(userId, request, count);
        }
        return questionMapper.selectBatchIds(ids);
    }

    private LambdaQueryWrapper<Question> baseQuestionQuery(Long userId, GenerateExamRequest request) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getUserId, userId)
                .and(w -> w
                        .notIn(Question::getType, List.of("single", "multiple"))
                        .or()
                        .apply("options_json IS NOT NULL AND options_json != '[]' AND options_json != ''")
                );
        if (request.minDifficulty() != null) {
            wrapper.ge(Question::getDifficulty, request.minDifficulty());
        }
        if (request.maxDifficulty() != null) {
            wrapper.le(Question::getDifficulty, request.maxDifficulty());
        }
        return wrapper;
    }

    private AnswerItem findAnswer(List<AnswerItem> answers, Long questionId) {
        if (answers == null) {
            return null;
        }
        return answers.stream().filter(item -> questionId.equals(item.questionId())).findFirst().orElse(null);
    }

    private int score(Question question, AnswerItem item) {
        if (item == null) {
            return 0;
        }
        if ("short".equals(question.getType()) || "calculate".equals(question.getType())) {
            return Boolean.TRUE.equals(item.selfCorrect()) ? 1 : 2;
        }
        String expected = normalize(question.getAnswer());
        String actual = normalize(item.answer());
        if ("multiple".equals(question.getType())) {
            expected = sortCsv(expected);
            actual = sortCsv(actual);
        }
        return expected.equals(actual) ? 1 : 0;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replace("，", ",").toUpperCase(Locale.ROOT);
    }

    private String sortCsv(String value) {
        return String.join(",", List.of(value.split(",")).stream().map(String::trim).sorted().toList());
    }

    private void upsertError(Long userId, Long questionId) {
        ErrorBook error = errorBookMapper.selectOne(new LambdaQueryWrapper<ErrorBook>()
                .eq(ErrorBook::getUserId, userId)
                .eq(ErrorBook::getQuestionId, questionId));
        if (error == null) {
            error = new ErrorBook();
            error.setUserId(userId);
            error.setQuestionId(questionId);
            error.setWrongCount(1);
            error.setMarked(false);
            error.setLastWrongTime(LocalDateTime.now());
            errorBookMapper.insert(error);
        } else {
            error.setWrongCount(error.getWrongCount() + 1);
            error.setLastWrongTime(LocalDateTime.now());
            errorBookMapper.updateById(error);
        }
    }

    private String cacheKey(Long userId, String paperId) {
        return "quiz:paper:" + userId + ":" + paperId;
    }
}
