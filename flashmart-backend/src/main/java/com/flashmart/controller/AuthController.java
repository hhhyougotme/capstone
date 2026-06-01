package com.flashmart.controller;

import com.flashmart.common.ApiResult;
import com.flashmart.dto.LoginRequest;
import com.flashmart.dto.LoginResponse;
import com.flashmart.dto.RegisterRequest;
import com.flashmart.dto.SendVerificationCodeRequest;
import com.flashmart.dto.VerificationCodeResponse;
import com.flashmart.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/verification-code")
    public ApiResult<VerificationCodeResponse> sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest req) {
        return ApiResult.ok(authService.sendRegisterVerificationCode(req));
    }

    @PostMapping("/register")
    public ApiResult<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResult.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResult.ok(authService.login(req));
    }
}
