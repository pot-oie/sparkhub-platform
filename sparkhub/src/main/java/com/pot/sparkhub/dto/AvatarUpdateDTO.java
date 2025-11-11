package com.pot.sparkhub.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AvatarUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 前端选择的头像 URL 路径
     * (例如: "/uploads/avatar1.png")
     */
    private String avatarUrl;
}