package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.common.BizException;
import org.example.backend.dto.UserDtos.ChangePasswordRequest;
import org.example.backend.dto.UserDtos.ProfileResponse;
import org.example.backend.dto.UserDtos.UpdateProfileRequest;
import org.example.backend.entity.UserAccount;
import org.example.backend.mapper.UserAccountMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserAccountMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserAccountMapper userMapper) {
        this.userMapper = userMapper;
    }

    public ProfileResponse getProfile(Long userId) {
        UserAccount user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(9004, "用户不存在");
        }
        return new ProfileResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    public void updateProfile(Long userId, UpdateProfileRequest request) {
        UserAccount user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(9004, "用户不存在");
        }
        if (request.username() != null && !request.username().isBlank()
                && !request.username().equals(user.getUsername())) {
            Long count = userMapper.selectCount(new LambdaQueryWrapper<UserAccount>()
                    .eq(UserAccount::getUsername, request.username()));
            if (count > 0) {
                throw new BizException(9006, "用户名已被占用");
            }
            user.setUsername(request.username());
        }
        user.setEmail(request.email());
        userMapper.updateById(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        UserAccount user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(9004, "用户不存在");
        }
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BizException(9005, "旧密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(user);
    }
}
