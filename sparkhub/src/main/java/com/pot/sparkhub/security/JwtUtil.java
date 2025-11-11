package com.pot.sparkhub.security;

import com.pot.sparkhub.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    // 从 application.properties 中读取密钥
    @Value("${sparkhub.jwt.secret}")
    private String secret;

    // 从 application.properties 中读取过期时间 (毫秒)
    @Value("${sparkhub.jwt.expiration}")
    private Long expiration;

    // 获取密钥
    private SecretKey getSigningKey() {
        // 使用 Keys.hmacShaKeyFor 来生成一个安全的密钥
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 1. 从 Token 中提取用户名
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // 2. 从 Token 中提取过期时间
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // 3. 提取指定的 Claim
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 4. 提取 Token 中的所有 Claims
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 5. 检查 Token 是否过期
    private Boolean isTokenExpired(String token) {
        final Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    // 6. 为指定用户生成 Token
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // 6.1. 添加 "roles" (角色)
        // (将 GrantedAuthority 转换为简单的字符串列表, e.g., "ROLE_USER")
        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());
        claims.put("roles", roles);

        // 6.2. 添加 "email" (邮箱)
        // (UserDetailsServiceImpl 确保了 userDetails 是一个 User 实例)
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            claims.put("email", user.getEmail());
        }

        // 6.3. "sub" (用户名) 会在 doGenerateToken 中设置
        return doGenerateToken(claims, userDetails.getUsername());
    }

    // 7. 生成 Token 的核心逻辑
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject) // subject 存用户名
                .issuedAt(createdDate)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    // 8. 验证 Token 是否有效
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        // 检查 Token 中的用户名是否与 UserDetails 匹配, 并且 Token 未过期
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}