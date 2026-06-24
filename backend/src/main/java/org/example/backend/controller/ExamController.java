package org.example.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.example.backend.common.ApiResponse;
import org.example.backend.dto.ExamDtos.ExamDetail;
import org.example.backend.dto.ExamDtos.ExamPaper;
import org.example.backend.dto.ExamDtos.GenerateExamRequest;
import org.example.backend.dto.ExamDtos.SubmitExamRequest;
import org.example.backend.entity.ExamRecord;
import org.example.backend.service.ExamService;
import org.example.backend.util.UserContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
public class ExamController {
    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/generate")
    public ApiResponse<ExamPaper> generate(HttpServletRequest request, @Valid @RequestBody GenerateExamRequest body) {
        return ApiResponse.ok(examService.generate(UserContext.userId(request), body));
    }

    @PostMapping("/submit")
    public ApiResponse<ExamRecord> submit(HttpServletRequest request, @Valid @RequestBody SubmitExamRequest body) {
        return ApiResponse.ok(examService.submit(UserContext.userId(request), body));
    }

    @GetMapping("/history")
    public ApiResponse<List<ExamRecord>> history(HttpServletRequest request) {
        return ApiResponse.ok(examService.history(UserContext.userId(request)));
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<ExamDetail> detail(HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.ok(examService.detail(UserContext.userId(request), id));
    }
}
