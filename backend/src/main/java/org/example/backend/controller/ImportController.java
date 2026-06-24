package org.example.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.backend.common.ApiResponse;
import org.example.backend.dto.ImportDtos.ImportResult;
import org.example.backend.service.ImportService;
import org.example.backend.util.UserContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
public class ImportController {
    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/questions")
    public ApiResponse<ImportResult> questions(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "knowledgePointId", required = false) Long knowledgePointId
    ) throws Exception {
        return ApiResponse.ok(importService.importExcel(UserContext.userId(request), file, subjectId, knowledgePointId));
    }
}
