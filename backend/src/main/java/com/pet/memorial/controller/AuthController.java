package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.AuthResponse;
import com.pet.memorial.dto.LoginRequest;
import com.pet.memorial.dto.RegisterRequest;
import com.pet.memorial.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthResponse> me() {
        return ApiResponse.success(authService.me());
    }
}
