package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.common.ApiResponse;
import org.example.backend.dto.AuthDtos.LoginRequest;
import org.example.backend.dto.AuthDtos.LoginResponse;
import org.example.backend.dto.AuthDtos.RegisterRequest;
import org.example.backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
