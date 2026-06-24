package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDtos {
    public record ProfileResponse(Long id, String username, String email) {
    }

    public record UpdateProfileRequest(String username, String email) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "旧密码不能为空") String oldPassword,
            @NotBlank(message = "新密码不能为空") @Size(min = 6, max = 20, message = "密码长度在6到20个字符") String newPassword) {
    }
}
