package com.pot.sparkhub.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BackingDetailDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // --- 订单(Backing)信息 ---
    private Long id;
    private Long backingAmount; // 实际支持金额
    private Integer status;     // 订单状态 (0:待支付, 1:已支付, 2:已取消)
    private LocalDateTime createTime;

    // --- 关联的项目信息 (JOIN) ---
    private Long projectId;
    private String projectTitle;

    // --- 关联的回报信息 (JOIN) ---
    private Long rewardId;
    private String rewardTitle;
}