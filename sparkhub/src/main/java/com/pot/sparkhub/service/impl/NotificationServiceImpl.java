package com.pot.sparkhub.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pot.sparkhub.dto.NotificationDTO;
import com.pot.sparkhub.entity.Notification;
import com.pot.sparkhub.mapper.NotificationMapper;
import com.pot.sparkhub.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * [私有] 核心的发送方法
     */
    @Transactional
    public void sendNotification(Long recipientId, String type, String content, String linkUrl, Long senderId) {
        // (在真实项目中, 你还应该检查 recipientId != senderId, 防止自己给自己发通知)
        try {
            Notification notification = new Notification();
            notification.setRecipientId(recipientId);
            notification.setType(type);
            notification.setContent(content);
            notification.setLinkUrl(linkUrl);
            notification.setSenderId(senderId);
            notification.setRead(false); // 默认为未读
            notification.setCreateTime(LocalDateTime.now());

            notificationMapper.insert(notification);
        } catch (Exception e) {
            // [重要] 通知失败不应该中断主业务 (如评论、点赞)
            // 所以我们捕获异常, 只在控制台打印错误
            System.err.println("发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 公开方法 1：发送系统通知
     */
    @Override
    public void sendSystemNotification(Long recipientId, String type, String content, String linkUrl) {
        // senderId 为 null
        sendNotification(recipientId, type, content, linkUrl, null);
    }

    /**
     * 公开方法 2：发送互动通知
     */
    @Override
    public void sendUserNotification(Long recipientId, String type, String content, String linkUrl, Long senderId) {
        // 传入 senderId
        sendNotification(recipientId, type, content, linkUrl, senderId);
    }

    // --- Controller 调用的方法 ---

    /**
     * 获取通知列表 (分页 + 过滤)
     */
    @Override
    public PageInfo<NotificationDTO> getNotificationsForUser(Long recipientId, String filter, int pageNum, int pageSize) {
        // 1. 启动 PageHelper 分页
        PageHelper.startPage(pageNum, pageSize);

        // 2. 调用 Mapper (XML 中会处理 filter)
        List<NotificationDTO> notifications = notificationMapper.findNotificationsByRecipientId(recipientId, filter);

        // 3. 包装成 PageInfo 返回
        return new PageInfo<>(notifications);
    }

    /**
     * 获取未读总数
     */
    @Override
    public long getUnreadCount(Long recipientId) {
        return notificationMapper.countUnreadByRecipientId(recipientId);
    }

    /**
     * 标记为已读
     */
    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long recipientId) {
        // Mapper 中的 SQL 包含了 recipientId, 确保用户只能修改自己的通知
        notificationMapper.markAsRead(notificationId, recipientId);
    }

    /**
     * 标记全部为已读
     */
    @Override
    @Transactional
    public void markAllAsRead(Long recipientId) {
        notificationMapper.markAllAsRead(recipientId);
    }
}