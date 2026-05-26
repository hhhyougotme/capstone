package com.flashmart.controller;

import com.flashmart.common.ApiResult;
import com.flashmart.dto.LoginRequest;
import com.flashmart.dto.LoginResponse;
import com.flashmart.dto.RegisterRequest;
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

    @PostMapping("/register")
    public ApiResult<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ApiResult.ok();
    }

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResult.ok(authService.login(req));
    }
}
