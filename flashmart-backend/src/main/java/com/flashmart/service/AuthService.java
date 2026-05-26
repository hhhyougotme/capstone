package com.flashmart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashmart.common.BizException;
import com.flashmart.dto.LoginRequest;
import com.flashmart.dto.LoginResponse;
import com.flashmart.dto.RegisterRequest;
import com.flashmart.entity.User;
import com.flashmart.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    @Transactional
    public void register(RegisterRequest req) {
        Long c = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (c != null && c > 0) {
            throw new BizException("Phone already registered");
        }
        User u = new User();
        u.setPhone(req.getPhone());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setNickname(req.getNickname() != null ? req.getNickname() : req.getPhone());
        u.setStatus(1);
        userMapper.insert(u);
    }

    public LoginResponse login(LoginRequest req) {
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (u == null || !passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new BizException("Invalid phone or password");
        }
        if (u.getStatus() != null && u.getStatus() != 1) {
            throw new BizException("Account disabled");
        }
        String token = sessionService.createSession(u.getId());
        LoginResponse r = new LoginResponse();
        r.setToken(token);
        r.setUserId(u.getId());
        r.setNickname(u.getNickname());
        return r;
    }
}
