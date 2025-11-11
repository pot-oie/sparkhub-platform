package com.pot.sparkhub.controller;

import com.github.pagehelper.PageInfo;
import com.pot.sparkhub.common.Result;
import com.pot.sparkhub.dto.NotificationDTO;
import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()") // 所有通知接口都需要登录
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * (辅助方法) 获取当前登录的用户
     * 用于保证所有操作只针对当前用户
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new RuntimeException("用户未登录");
        }
        return (User) auth.getPrincipal();
    }

    /**
     * GET /api/notifications/unread-count
     * 获取未读通知总数 (用于“红点”)
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount() {
        User currentUser = getCurrentUser();
        long count = notificationService.getUnreadCount(currentUser.getId());
        Map<String, Long> response = Collections.singletonMap("count", count);
        return Result.success(response);
    }

    /**
     * GET /api/notifications
     * 获取当前登录用户的通知列表 (分页 + 过滤)
     */
    @GetMapping
    public Result<PageInfo<NotificationDTO>> getMyNotifications(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            // filter 可选值: "all", "unread", "system", "interaction"
            @RequestParam(required = false) String filter) {

        User currentUser = getCurrentUser();
        PageInfo<NotificationDTO> notifications = notificationService.getNotificationsForUser(
                currentUser.getId(), filter, pageNum, pageSize
        );
        return Result.success(notifications);
    }

    /**
     * POST /api/notifications/{id}/read
     * 将单条通知标记为已读
     */
    @PostMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        // Service 层会负责检查权限 (通知是否属于当前用户)
        notificationService.markAsRead(id, currentUser.getId());
        return Result.success();
    }

    /**
     * POST /api/notifications/read-all
     * 将所有通知标记为已读
     */
    @PostMapping("/read-all")
    public Result<?> markAllAsRead() {
        User currentUser = getCurrentUser();
        notificationService.markAllAsRead(currentUser.getId());
        return Result.success();
    }
}