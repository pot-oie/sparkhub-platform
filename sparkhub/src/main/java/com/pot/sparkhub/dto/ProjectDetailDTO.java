package com.pot.sparkhub.dto;

import com.pot.sparkhub.entity.ProjectReward;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

// 用于 /api/projects/{id} 详情页的 DTO
@Data
@EqualsAndHashCode(callSuper = true) // 继承 ProjectSummaryDTO
public class ProjectDetailDTO extends ProjectSummaryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // --- 详情页独有的信息 ---
    private String description; // 完整描述

    // --- 嵌套对象: 关联的回报档位 ---
    private List<ProjectReward> rewards;

    // --- 额外字段: 创建者用户ID ---
    private Long creatorId;

    /**
     * 所有已支付的支持者的 UserID 列表
     */
    private List<Long> backerIds;

    // --- 额外字段: 当前用户是否已收藏该项目 ---
    private Boolean isFavorite;

    // --- 额外字段: 分类ID ---
    private Long categoryId;
}