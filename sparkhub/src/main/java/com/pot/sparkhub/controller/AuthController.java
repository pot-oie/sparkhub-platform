package com.pot.sparkhub.controller;

import com.pot.sparkhub.common.Result;
import com.pot.sparkhub.dto.LoginRequest;
import com.pot.sparkhub.dto.RegisterRequest;
import com.pot.sparkhub.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 注册
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            authService.register(registerRequest);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage()); // 400 Bad Request
        }
    }

    // 登录
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) { // <-- 修改后
        try {
            // Map<String, String> tokenMap = authService.login(loginRequest); // <-- 修改前
            Map<String, Object> responseMap = authService.login(loginRequest); // <-- 修改后
            return Result.success(responseMap);
        } catch (Exception e) {
            // 认证失败通常是 401
            return Result.error(401, "认证失败: " + e.getMessage());
        }
    }

    // 登出
    @PostMapping("/logout")
    public Result<?> logout() {
        // 登出需要认证, JwtFilter 会先确保用户是登录状态
        try {
            authService.logout();
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }
}