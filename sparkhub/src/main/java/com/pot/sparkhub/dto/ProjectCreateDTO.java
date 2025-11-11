package com.pot.sparkhub.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectCreateDTO {
    // 1. 项目主体信息
    private Long categoryId;
    private String title;
    private String description;
    private String coverImage;
    private BigDecimal goalAmount;
    private LocalDateTime endTime;

    // 2. 嵌套的回报档位列表
    private List<RewardCreateDTO> rewards;
}