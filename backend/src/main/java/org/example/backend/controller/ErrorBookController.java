package org.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.backend.common.ApiResponse;
import org.example.backend.entity.ErrorBook;
import org.example.backend.entity.Question;
import org.example.backend.mapper.ErrorBookMapper;
import org.example.backend.mapper.QuestionMapper;
import org.example.backend.util.UserContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/errors")
public class ErrorBookController {
    private final ErrorBookMapper errorBookMapper;
    private final QuestionMapper questionMapper;

    public ErrorBookController(ErrorBookMapper errorBookMapper, QuestionMapper questionMapper) {
        this.errorBookMapper = errorBookMapper;
        this.questionMapper = questionMapper;
    }

    public record ErrorBookItem(Long id, String stem, String type, String optionsJson,
                                String answer, String analysis, Integer wrongCount,
                                Boolean marked, String note, LocalDateTime lastWrongTime) {}

    @GetMapping
    public ApiResponse<List<ErrorBookItem>> list(HttpServletRequest request) {
        List<ErrorBook> errors = errorBookMapper.selectList(new LambdaQueryWrapper<ErrorBook>()
                .eq(ErrorBook::getUserId, UserContext.userId(request))
                .orderByDesc(ErrorBook::getWrongCount));
        if (errors.isEmpty()) return ApiResponse.ok(List.of());

        List<Long> qIds = errors.stream().map(ErrorBook::getQuestionId).toList();
        Map<Long, Question> qMap = questionMapper.selectBatchIds(qIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<ErrorBookItem> items = new ArrayList<>();
        for (ErrorBook e : errors) {
            Question q = qMap.get(e.getQuestionId());
            items.add(new ErrorBookItem(e.getId(),
                    q != null ? q.getStem() : "题目已删除",
                    q != null ? q.getType() : "",
                    q != null ? q.getOptionsJson() : null,
                    q != null ? q.getAnswer() : "",
                    q != null ? q.getAnalysis() : "",
                    e.getWrongCount(), e.getMarked(), e.getNote(), e.getLastWrongTime()));
        }
        return ApiResponse.ok(items);
    }

    @PutMapping("/{id}/mark")
    public ApiResponse<Void> mark(HttpServletRequest request, @PathVariable Long id, @RequestParam boolean marked) {
        ErrorBook item = errorBookMapper.selectById(id);
        if (item != null && UserContext.userId(request).equals(item.getUserId())) {
            item.setMarked(marked);
            errorBookMapper.updateById(item);
        }
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/note")
    public ApiResponse<Void> note(HttpServletRequest request, @PathVariable Long id, @RequestBody String note) {
        ErrorBook item = errorBookMapper.selectById(id);
        if (item != null && UserContext.userId(request).equals(item.getUserId())) {
            item.setNote(note);
            errorBookMapper.updateById(item);
        }
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        errorBookMapper.delete(new LambdaQueryWrapper<ErrorBook>()
                .eq(ErrorBook::getUserId, UserContext.userId(request)).eq(ErrorBook::getId, id));
        return ApiResponse.ok();
    }
}
