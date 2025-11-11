package com.pot.sparkhub.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProjectSummaryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String coverImage;
    private BigDecimal goalAmount;
    private BigDecimal currentAmount;
    private LocalDateTime createTime;
    private LocalDateTime endTime;
    private Integer status;

    // --- 通过 JOIN 获得的额外信息 ---
    private String creatorName; // 发起人用户名
    private String categoryName; // 分类名
}