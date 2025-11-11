package com.pot.sparkhub.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Project implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long creatorId;
    private Long categoryId;
    private String title;
    private String description;
    private String coverImage;
    private BigDecimal goalAmount;
    private BigDecimal currentAmount;
    private LocalDateTime endTime;
    private Integer status;// (0:审核中, 1:众筹中, 2:成功, 3:失败)
    private LocalDateTime createTime;
}