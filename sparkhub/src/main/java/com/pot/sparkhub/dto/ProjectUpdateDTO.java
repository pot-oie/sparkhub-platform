package com.pot.sparkhub.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectUpdateDTO {
    // 出于安全和业务逻辑考虑，我们只允许修改这些字段
    // 目标金额、发起人等不应被修改
    private Long categoryId;
    private String title;
    private String description;
    private String coverImage;
    private LocalDateTime endTime;
    private Double goalAmount;
    private List<RewardCreateDTO> rewards;
}