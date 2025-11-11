package com.pot.sparkhub.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NotificationDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String content;
    private String type;
    private String linkUrl;
    private boolean isRead;
    private LocalDateTime createTime;

    // (来自 JOIN user 表)
    private Long senderId;
    private String senderUsername;
    private String senderAvatar;
}