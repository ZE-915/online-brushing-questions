package org.example.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.example.backend.common.ApiResponse;
import org.example.backend.dto.SubjectDtos.KnowledgePointRequest;
import org.example.backend.dto.SubjectDtos.KnowledgePointUpdateRequest;
import org.example.backend.dto.SubjectDtos.SubjectRequest;
import org.example.backend.entity.KnowledgePoint;
import org.example.backend.entity.Subject;
import org.example.backend.service.CatalogService;
import org.example.backend.util.UserContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/subjects")
    public ApiResponse<List<Subject>> subjects(HttpServletRequest request) {
        return ApiResponse.ok(catalogService.subjects(UserContext.userId(request)));
    }

    @PostMapping("/subjects")
    public ApiResponse<Subject> createSubject(HttpServletRequest request, @Valid @RequestBody SubjectRequest body) {
        return ApiResponse.ok(catalogService.saveSubject(UserContext.userId(request), body));
    }

    @PutMapping("/subjects/{id}")
    public ApiResponse<Subject> updateSubject(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody SubjectRequest body) {
        return ApiResponse.ok(catalogService.updateSubject(UserContext.userId(request), id, body));
    }

    @DeleteMapping("/subjects/{id}")
    public ApiResponse<Void> deleteSubject(HttpServletRequest request, @PathVariable Long id) {
        catalogService.deleteSubject(UserContext.userId(request), id);
        return ApiResponse.ok();
    }

    @GetMapping("/knowledge-points")
    public ApiResponse<List<KnowledgePoint>> knowledgePoints(HttpServletRequest request, @RequestParam(required = false) Long subjectId) {
        return ApiResponse.ok(catalogService.knowledgePoints(UserContext.userId(request), subjectId));
    }

    @PostMapping("/knowledge-points")
    public ApiResponse<KnowledgePoint> createKnowledgePoint(HttpServletRequest request, @Valid @RequestBody KnowledgePointRequest body) {
        return ApiResponse.ok(catalogService.saveKnowledgePoint(UserContext.userId(request), body));
    }

    @PutMapping("/knowledge-points/{id}")
    public ApiResponse<KnowledgePoint> updateKnowledgePoint(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody KnowledgePointUpdateRequest body) {
        return ApiResponse.ok(catalogService.updateKnowledgePoint(UserContext.userId(request), id, body));
    }

    @DeleteMapping("/knowledge-points/{id}")
    public ApiResponse<Void> deleteKnowledgePoint(HttpServletRequest request, @PathVariable Long id) {
        catalogService.deleteKnowledgePoint(UserContext.userId(request), id);
        return ApiResponse.ok();
    }
}
