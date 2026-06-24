package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.common.BizException;
import org.example.backend.dto.AuthDtos.LoginRequest;
import org.example.backend.dto.AuthDtos.LoginResponse;
import org.example.backend.dto.AuthDtos.RegisterRequest;
import org.example.backend.entity.UserAccount;
import org.example.backend.mapper.UserAccountMapper;
import org.example.backend.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserAccountMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserAccountMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, request.username()));
        if (count > 0) {
            throw new BizException(9001, "用户名已存在");
        }
        UserAccount user = new UserAccount();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount user = userMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BizException(9003, "用户名或密码错误");
        }
        return new LoginResponse(jwtUtil.generate(user.getId(), user.getUsername()), user.getId(), user.getUsername());
    }
}
