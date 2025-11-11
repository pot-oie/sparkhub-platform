package com.pot.sparkhub.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsServiceImpl; // 自动注入 UserDetailsServiceImpl

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate; // 注入 RedisTemplate

    // 定义 Redis Key 的前缀
    public static final String JWT_REDIS_KEY_PREFIX = "sparkhub:jwt:";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // 1. 检查 Header 是否存在, 并且是否以 "Bearer " 开头
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // 截取 "Bearer " 后的 Token
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token 已过期");
            } catch (Exception e) {
                logger.error("JWT Token 解析失败");
            }
        }

        // 2. 检查用户名是否获取成功, 并且 SecurityContext 中目前没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 3. 【核心】检查 Redis 中是否存在该 Token (实现登出和T人)
            String redisKey = JWT_REDIS_KEY_PREFIX + username;
            String tokenInRedis = redisTemplate.opsForValue().get(redisKey);

            if (tokenInRedis != null && tokenInRedis.equals(jwt)) {
                // 4. Redis 中存在此 Token, 开始验证
                UserDetails userDetails = this.userDetailsServiceImpl.loadUserByUsername(username);

                // 5. 验证 Token 是否有效 (用户名匹配且未过期)
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // 6. 创建认证凭证
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 7. 将凭证存入 SecurityContext, 标记为已认证
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } else {
                // 如果 Redis 中不存在 (可能已登出或被T), 即使 Token 签名正确也不予通过
                logger.warn("JWT Token 在 Redis 中不存在或不匹配, 认证失败");
            }
        }

        // 8. 放行请求, 进入下一个过滤器
        filterChain.doFilter(request, response);
    }
}