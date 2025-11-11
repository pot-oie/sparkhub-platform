package com.pot.sparkhub.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDetailDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String content;
    private LocalDateTime createTime;

    // --- 评论者信息 (来自 JOIN) ---
    private Long userId;
    private String username;
    private String avatar;

    // --- 父评论ID (若为顶级评论则为 null) ---
    private Long parentId;
    // --- 回复的子评论 ---
    private List<CommentDetailDTO> replies;
    // --- 点赞数 ---
    private Integer likeCount;

    private boolean isLiked = false;
}