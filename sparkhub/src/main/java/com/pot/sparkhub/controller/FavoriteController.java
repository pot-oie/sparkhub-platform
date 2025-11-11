package com.pot.sparkhub.controller;

import com.pot.sparkhub.common.Result;
import com.pot.sparkhub.dto.ProjectSummaryDTO;
import com.pot.sparkhub.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@PreAuthorize("hasRole('ROLE_USER')") // 关键: 整个模块都需要 "ROLE_USER" 权限
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * POST /api/favorites/{projectId}
     * 添加收藏
     */
    @PostMapping("/{projectId}")
    public Result<?> addFavorite(@PathVariable Long projectId) {
        try {
            favoriteService.addFavorite(projectId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * DELETE /api/favorites/{projectId}
     * 取消收藏
     */
    @DeleteMapping("/{projectId}")
    public Result<?> removeFavorite(@PathVariable Long projectId) {
        favoriteService.removeFavorite(projectId);
        return Result.success();
    }

    /**
     * GET /api/favorites/my
     * 查看我的收藏列表
     */
    @GetMapping("/my")
    public Result<List<ProjectSummaryDTO>> getMyFavorites() {
        List<ProjectSummaryDTO> favorites = favoriteService.getMyFavorites();
        return Result.success(favorites);
    }
}