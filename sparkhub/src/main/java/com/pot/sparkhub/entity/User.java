package com.pot.sparkhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
public class User implements UserDetails {
    private Long id;
    private String username;
    @JsonIgnore // 序列化时忽略密码
    private String password;
    private String email;
    private String avatar;
    private LocalDateTime createTime;

    // 关键: 用户的角色列表
    private List<Role> roles;

    // ---------- UserDetails 接口实现 ----------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回角色列表
        return roles;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    // 账户是否未过期 (我们这里默认都不过期)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 账户是否未锁定
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 凭证(密码)是否未过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 账户是否启用
    @Override
    public boolean isEnabled() {
        return true;
    }
}