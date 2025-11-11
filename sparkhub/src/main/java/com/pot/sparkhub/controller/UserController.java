package com.pot.sparkhub.controller;

import com.pot.sparkhub.common.Result;
import com.pot.sparkhub.dto.AvatarUpdateDTO;
import com.pot.sparkhub.dto.UpdateEmailDTO;
import com.pot.sparkhub.dto.UpdatePasswordDTO;
import com.pot.sparkhub.dto.UserDTO;
import com.pot.sparkhub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ROLE_USER')") // [安全] 整个 Controller 都需要用户登录
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * PUT /api/users/profile/email
     * 更新当前用户的邮箱
     */
    @PutMapping("/profile/email")
    public Result<?> updateUserEmail(@Valid @RequestBody UpdateEmailDTO emailDTO) {
        userService.updateEmail(emailDTO);
        return Result.success();
    }

    /**
     * PUT /api/users/profile/password
     * 更新当前用户的密码
     */
    @PutMapping("/profile/password")
    public Result<?> updateUserPassword(@Valid @RequestBody UpdatePasswordDTO passwordDTO) {
        userService.updatePassword(passwordDTO);
        return Result.success();
    }

    /**
     * GET /api/users/me
     * 获取当前登录用户的信息 (用于前端刷新页面)
     */
    @GetMapping("/me")
    public Result<UserDTO> getMyInfo() {
        return Result.success(userService.getMyInfo());
    }

    /**
     * PUT /api/users/avatar
     * 更新当前用户的头像 (从预设列表中选择)
     */
    @PostMapping("/avatar")
    public Result<UserDTO> updateUserAvatar(@RequestBody AvatarUpdateDTO avatarUpdateDTO) {
        try {
            // 从 DTO 中获取 URL 字符串
            UserDTO updatedUser = userService.updateUserAvatar(avatarUpdateDTO.getAvatarUrl());
            return Result.success(updatedUser);
        } catch (Exception e) {
            return Result.error(500, "头像更新失败: " + e.getMessage());
        }
    }
}