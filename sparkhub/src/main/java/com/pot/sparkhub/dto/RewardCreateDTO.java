package com.pot.sparkhub.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RewardCreateDTO {
    // 注意: DTO 中不包含 id 或 projectId, 这些由后端设置
    private String title;
    private String description;
    private BigDecimal amount;
    private Integer stock;
    private String imageUrl;
}