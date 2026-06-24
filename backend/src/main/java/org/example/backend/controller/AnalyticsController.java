package org.example.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.example.backend.common.ApiResponse;
import org.example.backend.service.AnalyticsService;
import org.example.backend.util.UserContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview(HttpServletRequest request) {
        return ApiResponse.ok(analyticsService.overview(UserContext.userId(request)));
    }
}
