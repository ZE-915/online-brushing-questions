package org.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.backend.common.ApiResponse;
import org.example.backend.dto.QuestionDtos.QuestionListItem;
import org.example.backend.dto.QuestionDtos.QuestionQuery;
import org.example.backend.dto.QuestionDtos.QuestionRequest;
import org.example.backend.entity.AnswerRecord;
import org.example.backend.entity.Question;
import org.example.backend.mapper.AnswerRecordMapper;
import org.example.backend.service.QuestionService;
import org.example.backend.util.UserContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questionService;
    private final AnswerRecordMapper answerRecordMapper;

    public QuestionController(QuestionService questionService, AnswerRecordMapper answerRecordMapper) {
        this.questionService = questionService;
        this.answerRecordMapper = answerRecordMapper;
    }

    @GetMapping
    public ApiResponse<List<QuestionListItem>> list(HttpServletRequest request, QuestionQuery query) {
        Long userId = UserContext.userId(request);
        List<Question> questions = questionService.list(userId, query);
        if (questions.isEmpty()) return ApiResponse.ok(List.of());

        List<Long> qIds = questions.stream().map(Question::getId).toList();
        List<AnswerRecord> records = answerRecordMapper.selectList(new LambdaQueryWrapper<AnswerRecord>()
                .eq(AnswerRecord::getUserId, userId).in(AnswerRecord::getQuestionId, qIds)
                .orderByDesc(AnswerRecord::getCreateTime));
        Map<Long, Integer> statusMap = new HashMap<>();
        for (AnswerRecord r : records) {
            statusMap.putIfAbsent(r.getQuestionId(), r.getCorrectStatus());
        }

        List<QuestionListItem> items = questions.stream().map(q -> new QuestionListItem(
                q.getId(), q.getSubjectId(), q.getKnowledgePointId(), q.getType(),
                q.getStem(), q.getOptionsJson(), q.getAnswer(), q.getAnalysis(),
                q.getDifficulty(), statusMap.get(q.getId()))).toList();
        return ApiResponse.ok(items);
    }

    @GetMapping("/{id}")
    public ApiResponse<Question> get(HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.ok(questionService.get(UserContext.userId(request), id));
    }

    @PostMapping
    public ApiResponse<Question> create(HttpServletRequest request, @Valid @RequestBody QuestionRequest body) {
        return ApiResponse.ok(questionService.save(UserContext.userId(request), body));
    }

    @PutMapping("/{id}")
    public ApiResponse<Question> update(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody QuestionRequest body) {
        return ApiResponse.ok(questionService.update(UserContext.userId(request), id, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        questionService.delete(UserContext.userId(request), id);
        return ApiResponse.ok();
    }
}
