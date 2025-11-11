package com.pot.sparkhub.service;

import com.pot.sparkhub.dto.ProjectSummaryDTO;
import java.util.List;

public interface FavoriteService {

    /**
     * 添加项目到收藏
     * @param projectId 项目ID
     */
    void addFavorite(Long projectId);

    /**
     * 从收藏中移除项目
     * @param projectId 项目ID
     */
    void removeFavorite(Long projectId);

    /**
     * 获取当前用户的收藏列表
     * @return 收藏的项目列表 (DTO)
     */
    List<ProjectSummaryDTO> getMyFavorites();

    /**
     * (辅助) 检查项目是否已被收藏
     */
    boolean isFavorite(Long userId, Long projectId);
}