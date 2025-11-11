package com.pot.sparkhub.mapper;

import com.pot.sparkhub.dto.NotificationDTO;
import com.pot.sparkhub.entity.Notification;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NotificationMapper {

    /**
     * 插入一条新通知
     */
    @Insert("INSERT INTO notification (recipient_id, type, content, link_url, sender_id, is_read, create_time) " +
            "VALUES (#{recipientId}, #{type}, #{content}, #{linkUrl}, #{senderId}, #{isRead}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Notification notification);

    /**
     * [复杂查询] 获取一个用户的所有通知 (在 XML 中实现)
     * (支持 JOIN 发送者信息 和 动态过滤)
     */
    List<NotificationDTO> findNotificationsByRecipientId(
            @Param("recipientId") Long recipientId,
            @Param("filter") String filter
    );

    /**
     * 查询未读通知总数
     */
    @Select("SELECT COUNT(*) FROM notification WHERE recipient_id = #{recipientId} AND is_read = false")
    long countUnreadByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * 将单条通知标记为已读 (安全)
     * (必须同时检查 id 和 recipient_id，防止用户恶意修改别人的通知)
     */
    @Update("UPDATE notification SET is_read = true " +
            "WHERE id = #{notificationId} AND recipient_id = #{recipientId}")
    int markAsRead(@Param("notificationId") Long notificationId, @Param("recipientId") Long recipientId);

    /**
     * 将所有未读通知标记为已读
     */
    @Update("UPDATE notification SET is_read = true " +
            "WHERE recipient_id = #{recipientId} AND is_read = false")
    int markAllAsRead(@Param("recipientId") Long recipientId);
}