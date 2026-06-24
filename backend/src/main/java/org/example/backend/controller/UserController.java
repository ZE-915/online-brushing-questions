package org.example.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.backend.common.ApiResponse;
import org.example.backend.dto.UserDtos.ChangePasswordRequest;
import org.example.backend.dto.UserDtos.ProfileResponse;
import org.example.backend.dto.UserDtos.UpdateProfileRequest;
import org.example.backend.service.UserService;
import org.example.backend.util.UserContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> getProfile(HttpServletRequest request) {
        Long userId = UserContext.userId(request);
        return ApiResponse.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(HttpServletRequest request, @RequestBody UpdateProfileRequest req) {
        Long userId = UserContext.userId(request);
        userService.updateProfile(userId, req);
        return ApiResponse.ok();
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(HttpServletRequest request, @Valid @RequestBody ChangePasswordRequest req) {
        Long userId = UserContext.userId(request);
        userService.changePassword(userId, req);
        return ApiResponse.ok();
    }
}
