package com.pot.sparkhub.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ProjectComment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long projectId;
    private Long userId;
    private String content;
    private LocalDateTime createTime;
    private Long parentId; // 父评论ID, 若为顶级评论则为 null
    private Integer likeCount;
}