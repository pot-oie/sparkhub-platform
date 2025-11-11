package com.pot.sparkhub.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Notification implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long recipientId;
    private String type;
    private String content;
    private String linkUrl;
    private boolean isRead;
    private LocalDateTime createTime;
    private Long senderId;
}