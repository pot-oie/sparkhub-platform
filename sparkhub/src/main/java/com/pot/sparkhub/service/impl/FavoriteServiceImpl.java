package com.pot.sparkhub.service.impl;

import com.pot.sparkhub.dto.ProjectSummaryDTO;
import com.pot.sparkhub.entity.Project;
import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.entity.UserFavorite;
import com.pot.sparkhub.mapper.FavoriteMapper;
import com.pot.sparkhub.mapper.ProjectMapper;
import com.pot.sparkhub.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ProjectMapper projectMapper; // 用于检查项目是否存在

    // 获取当前登录的用户
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new RuntimeException("用户未登录");
        }
        return (User) auth.getPrincipal();
    }

    @Override
    @Transactional
    @CacheEvict(value = "project", key = "#projectId")
    public void addFavorite(Long projectId) {
        User user = getCurrentUser();
        Long userId = user.getId();

        // 1. 检查项目是否存在
        Project project = projectMapper.findProjectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        // 2. 检查是否已被收藏 (防止重复插入)
        if (isFavorite(userId, projectId)) {
            // 已经收藏, 直接返回, 保持幂等性
            return;
        }

        // 3. 插入新收藏
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setProjectId(projectId);
        favorite.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(favorite);
    }

    @Override
    @Transactional
    @CacheEvict(value = "project", key = "#projectId")
    public void removeFavorite(Long projectId) {
        User user = getCurrentUser();
        favoriteMapper.deleteByUserAndProject(user.getId(), projectId);
    }

    @Override
    public List<ProjectSummaryDTO> getMyFavorites() {
        User user = getCurrentUser();
        return favoriteMapper.findFavoritesByUserId(user.getId());
    }

    @Override
    public boolean isFavorite(Long userId, Long projectId) {
        return favoriteMapper.findByUserAndProject(userId, projectId) != null;
    }
}