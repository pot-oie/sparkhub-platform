package com.pot.sparkhub.security;

import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 调用 Mapper, 从数据库中根据用户名查询用户
        User user = userMapper.findByUsername(username);

        // 2. 检查用户是否存在
        if (user == null) {
            throw new UsernameNotFoundException("用户 " + username + " 不存在");
        }

        // 3. 检查用户的角色列表 (在 UserMapper.xml 中已完成)
        // (如果用户存在但没有角色, 也可以在这里抛出异常或赋予默认角色)
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new UsernameNotFoundException("用户 " + username + " 没有任何角色");
        }

        // 4. 返回查询到的 User 对象 (它实现了 UserDetails 接口)
        return user;
    }
}