package com.pot.sparkhub.service.impl;

import com.pot.sparkhub.dto.LoginRequest;
import com.pot.sparkhub.dto.RegisterRequest;
import com.pot.sparkhub.dto.UserDTO;
import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.mapper.UserMapper;
import com.pot.sparkhub.security.JwtFilter;
import com.pot.sparkhub.security.JwtUtil;
import com.pot.sparkhub.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${sparkhub.jwt.expiration}") // 注入过期时间
    private Long expiration;

    // 1. 注册
    @Override
    @Transactional // 开启事务 (因为涉及多张表操作)
    public void register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 2. 检查邮箱是否已存在
        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("邮箱已注册");
        }

        // 3. 创建 User 对象
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // 4. 密码加密
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreateTime(LocalDateTime.now());

        // 5. 插入 user 表
        userMapper.insert(user); // user.id 会被自动回填

        // 6. 分配默认角色 "ROLE_USER"
        Long roleId = userMapper.findRoleIdByName("ROLE_USER");
        if (roleId == null) {
            throw new RuntimeException("默认角色 'ROLE_USER' 未在数据库中初始化");
        }
        userMapper.insertUserRole(user.getId(), roleId);
    }

    // 2. 登录
    @Override
    public Map<String, Object> login(LoginRequest request) {
        // 1. 使用 AuthenticationManager 进行认证 (它会调用 UserDetailsServiceImpl)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. 认证通过, 将认证信息存入 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 从认证信息中获取 UserDetails
        User user = (User) authentication.getPrincipal();

        // 4. 生成 JWT
        String jwt = jwtUtil.generateToken(user);

        // 5. 【核心】将 JWT 存入 Redis, 并设置过期时间 (与JWT过期时间一致)
        String redisKey = JwtFilter.JWT_REDIS_KEY_PREFIX + user.getUsername();
        redisTemplate.opsForValue().set(redisKey, jwt, expiration, TimeUnit.MILLISECONDS);

        // 6. 创建 UserDTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setAvatar(user.getAvatar());
        userDTO.setRoles(user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // 7. 返回 Map<String, Object>
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("token", jwt);
        responseMap.put("user", userDTO); // <-- 将用户信息放入

        return responseMap;
    }

    // 3. 登出
    @Override
    public void logout() {
        // 1. 从 SecurityContext 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("用户未登录");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // 2. 【核心】从 Redis 中删除该用户的 JWT
        String redisKey = JwtFilter.JWT_REDIS_KEY_PREFIX + username;
        redisTemplate.delete(redisKey);

        // 3. 清除 SecurityContext
        SecurityContextHolder.clearContext();
    }
}