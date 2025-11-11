package com.pot.sparkhub.service;

import com.pot.sparkhub.dto.LoginRequest;
import com.pot.sparkhub.dto.RegisterRequest;

import java.util.Map;

public interface AuthService {

    /**
     * 用户注册
     * @param request 注册请求
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     * @param request 登录请求
     * @return 包含 JWT 的 Map
     */
    Map<String, Object> login(LoginRequest request);

    /**
     * 用户登出
     */
    void logout();
}