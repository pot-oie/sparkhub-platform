package com.pot.sparkhub.config;

import com.pot.sparkhub.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- 1. 确保导入
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // <-- 2. 确保导入
import org.springframework.web.cors.CorsConfigurationSource; // <-- 3. 确保导入
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // <-- 4. 确保导入

import java.util.Arrays; // <-- 5. 确保导入

import static org.springframework.security.config.Customizer.withDefaults; // <-- 6. 确保导入

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsServiceImpl;

    @Autowired
    private JwtFilter jwtFilter;

    // 1. 密码编码器 (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. AuthenticationManager (认证管理器)
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsServiceImpl).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    // 3. SecurityFilterChain (核心安全链)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // (1) 启用并配置 CORS (使用 CorsConfigurationSource bean)
                .cors(withDefaults())

                // (2) 禁用 CSRF
                .csrf(csrf -> csrf.disable())

                // (3) 配置会话管理: 无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // (4) 配置 URL 路由权限
                .authorizeHttpRequests(authz -> authz
                        // 允许 CORS preflight OPTIONS 请求
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // <-- 关键点 2
                        // 允许公开访问上传的图片 (GET请求)
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        // 允许登录/注册
                        .requestMatchers("/api/auth/**").permitAll()
                        // 允许公开的 GET
                        .requestMatchers(HttpMethod.GET,
                                "/api/projects/**",
                                "/api/categories/**",
                                "/api/projects/{projectId}/comments"
                        ).permitAll()
                        // 允许登录用户访问自己的信息
                        .requestMatchers("/api/users/me", "/api/users/avatar").authenticated()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                // (5) 将 JWT 过滤器添加到 Spring Security 链中
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 4. 【Bean】: CORS 配置源
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许前端来源
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // 允许所有方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许所有 Headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允许携带凭证
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有 /api/** 路径应用此配置
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}