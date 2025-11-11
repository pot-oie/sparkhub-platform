package com.pot.sparkhub.service;

import com.github.pagehelper.PageInfo;
import com.pot.sparkhub.dto.NotificationDTO;

public interface NotificationService {

    // --- 写入方法 ---
    void sendSystemNotification(Long recipientId, String type, String content, String linkUrl);
    void sendUserNotification(Long recipientId, String type, String content, String linkUrl, Long senderId);

    // --- 读取方法 ---
    PageInfo<NotificationDTO> getNotificationsForUser(Long recipientId, String filter, int pageNum, int pageSize);
    long getUnreadCount(Long recipientId);

    // --- 更新方法 ---
    void markAsRead(Long notificationId, Long recipientId);
    void markAllAsRead(Long recipientId);
}