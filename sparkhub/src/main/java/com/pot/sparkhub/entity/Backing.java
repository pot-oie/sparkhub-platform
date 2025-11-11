package com.pot.sparkhub.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Backing implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long backerId;
    private Long projectId;
    private Long rewardId;
    private BigDecimal backingAmount;
    private Integer status;
    private LocalDateTime createTime;
}