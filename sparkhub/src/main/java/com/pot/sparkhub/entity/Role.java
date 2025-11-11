package com.pot.sparkhub.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Data
public class Role implements GrantedAuthority {
    private Long id;
    private String name;

    @Override
    public String getAuthority() {
        // 返回角色名 (e.g., "ROLE_USER")
        return name;
    }
}