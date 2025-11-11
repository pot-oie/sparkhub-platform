package com.pot.sparkhub.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pot.sparkhub.dto.UpdateEmailDTO;
import com.pot.sparkhub.dto.UpdatePasswordDTO;
import com.pot.sparkhub.dto.UserDTO;
import com.pot.sparkhub.entity.Role;
import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.mapper.UserMapper;
import com.pot.sparkhub.security.JwtFilter;
import com.pot.sparkhub.service.FileService;
import com.pot.sparkhub.service.NotificationService;
import com.pot.sparkhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private FileService fileService;

    @Autowired
    private NotificationService notificationService;

    // 角色名称翻译 Map (用于通知)
    private static final Map<String, String> ROLE_NAME_MAP = Map.of(
            "ROLE_CREATOR", "发起者",
            "ROLE_ADMIN", "管理员",
            "ROLE_USER", "用户"
    );

    /**
     * [Admin]
     */
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 权限
    public PageInfo<User> getAllUsers(int pageNum, int pageSize) {
        // 1. 启动分页
        PageHelper.startPage(pageNum, pageSize);

        // 2. 查询 (XML中已实现)
        List<User> userList = userMapper.findAllWithRoles();

        // 3. 封装 (User 实体已通过 @JsonIgnore 忽略密码)
        return new PageInfo<>(userList);
    }

    @Override
    @Transactional // 确保操作原子性
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 仅限管理员
    public void manageUserRole(Long userId, String roleName, boolean isAdd) {

        // 1. 验证目标用户是否存在 (使用新的 findById)
        // 解决了 'findById' 无法解析的错误
        User targetUser = userMapper.findById(userId);
        if (targetUser == null) {
            // 抛出运行时异常，Spring 会将其包装为 HTTP 500 或由全局异常处理器处理
            throw new RuntimeException("目标用户不存在，ID: " + userId);
        }

        // 2. 查找目标角色 ID (使用你提供的 findRoleIdByName)
        // 解决了 'findRoleByName' 无法解析的错误
        Long roleId = userMapper.findRoleIdByName(roleName);
        if (roleId == null) {
            throw new RuntimeException("目标角色不存在: " + roleName);
        }

        // 3. 检查当前用户是否已经拥有该角色
        // 重新查询用户以确保角色信息是最新的
        // (注: findByUsername 会触发 XML 中的复杂查询，包含角色)
        List<Role> currentUserRoles = userMapper.findByUsername(targetUser.getUsername()).getRoles();

        boolean alreadyHasRole = currentUserRoles.stream()
                // 使用我们刚刚查到的 roleId 来进行比较
                .anyMatch(r -> Objects.equals(r.getId(), roleId));

        // 4. 执行操作
        if (isAdd) {
            // --- 添加角色 ---
            if (alreadyHasRole) {
                return; // 已经有该角色，直接返回
            }
            // 使用查到的 roleId 进行插入
            userMapper.insertUserRole(userId, roleId);

            // 5. 发送通知 (添加角色)
            String readableRoleName = ROLE_NAME_MAP.getOrDefault(roleName, roleName);
            notificationService.sendSystemNotification(
                    userId,
                    "ROLE_UPDATED",
                    String.format("恭喜！您已被授予 '%s' 权限。", readableRoleName),
                    "/profile"
            );
        } else {
            // --- 移除角色 ---
            if (!alreadyHasRole) {
                return; // 没有该角色，直接返回
            }
            // 使用查到的 roleId 进行删除
            userMapper.deleteUserRole(userId, roleId);

            // 5. 发送通知 (移除角色)
            String readableRoleName = ROLE_NAME_MAP.getOrDefault(roleName, roleName);
            notificationService.sendSystemNotification(
                    userId,
                    "ROLE_UPDATED",
                    String.format("您的 '%s' 权限已被移除。", readableRoleName),
                    "/profile"
            );
        }
    }

    /**
     * (辅助方法) 获取当前登录的用户
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new RuntimeException("用户未登录");
        }
        return (User) auth.getPrincipal();
    }

    /**
     * (辅助方法) 使当前用户的 Token 失效 (T人)
     * (这依赖于 JwtFilter 中的 Redis 检查逻辑)
     */
    private void invalidateCurrentUserToken(String username) {
        String redisKey = JwtFilter.JWT_REDIS_KEY_PREFIX + username;
        redisTemplate.delete(redisKey);
    }

    @Override
    @Transactional
    public void updateEmail(UpdateEmailDTO emailDTO) {
        User currentUser = getCurrentUser();
        String newEmail = emailDTO.getEmail();

        // 1. 检查新邮箱是否已被其他人占用
        User existingUser = userMapper.findByEmail(newEmail);
        if (existingUser != null && !existingUser.getId().equals(currentUser.getId())) {
            throw new RuntimeException("该邮箱已被占用");
        }

        // 2. 更新邮箱
        User userToUpdate = new User();
        userToUpdate.setId(currentUser.getId());
        userToUpdate.setEmail(newEmail);
        userMapper.updateUser(userToUpdate); //

        // 3. [安全] 强制用户下线 (T人)
        invalidateCurrentUserToken(currentUser.getUsername());
    }

    @Override
    @Transactional
    public void updatePassword(UpdatePasswordDTO passwordDTO) {
        User currentUser = getCurrentUser();

        // 1. 验证旧密码
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), currentUser.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }

        // 2. 更新密码
        User userToUpdate = new User();
        userToUpdate.setId(currentUser.getId());
        userToUpdate.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword())); //
        userMapper.updateUser(userToUpdate); //

        // 3. [安全] 强制用户下线 (T人)
        invalidateCurrentUserToken(currentUser.getUsername());
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()") // 任何登录用户
    public UserDTO updateUserAvatar(String avatarUrl) {
        // (安全校验) 确保前端发的路径是合法的 (防止路径注入)
        if (avatarUrl == null || !avatarUrl.startsWith("/uploads/")) {
            throw new RuntimeException("无效的头像路径。");
        }

        // 获取当前用户
        User user = getCurrentUser();

        // 更新数据库 (直接使用传入的 URL)
        userMapper.updateAvatar(user.getId(), avatarUrl);

        // 更新 SecurityContext (让当前会话也立即生效)
        user.setAvatar(avatarUrl);

        // 返回更新后的 DTO
        return getMyInfo();
    }

    @Override
    @PreAuthorize("isAuthenticated()") // 任何登录用户
    public UserDTO getMyInfo() {
        User user = getCurrentUser(); // 从 SecurityContext 获取

        // 转换为 DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setAvatar(user.getAvatar());
        userDTO.setRoles(user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return userDTO;
    }
}