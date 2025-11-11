package com.pot.sparkhub.entity;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProjectReward implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private BigDecimal amount;
    private Integer stock;
    private String imageUrl;
}