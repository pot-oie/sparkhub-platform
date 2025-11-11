package com.pot.sparkhub.service.impl;

import com.pot.sparkhub.entity.ProjectReward;
import com.pot.sparkhub.mapper.*;
import com.pot.sparkhub.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component // 必须被 Spring 托管
public class ProjectCleaner {

    // 注入所有需要的依赖
    @Autowired private ProjectMapper projectMapper;
    @Autowired private RewardMapper rewardMapper;
    @Autowired private FileService fileService;

    /**
     * [核心方法]
     * 统一删除项目及所有关联数据
     * 现在这个方法是 public 的, 并且在独立的 Bean 中，@Transactional 绝对生效。
     */
    @Transactional
    public void deleteProjectAndRelatedData(Long projectId, String coverImageUrl) {
        // --- 1. 删除所有关联图片 ---

        // 1.1 查找所有回报，以获取其图片URL
        List<ProjectReward> rewards = rewardMapper.findByProjectId(projectId);

        // 1.2 循环删除回报图片
        for (ProjectReward reward : rewards) {
            try {
                fileService.deleteFile(reward.getImageUrl());
            } catch (Exception e) {
                System.err.println("WARN: 无法删除回报图片文件: " + reward.getImageUrl() + ". 原因: " + e.getMessage());
            }
        }

        // 1.3 删除项目封面图
        try {
            fileService.deleteFile(coverImageUrl);
        } catch (Exception e) {
            System.err.println("WARN: 无法删除项目封面图: " + coverImageUrl + ". 原因: " + e.getMessage());
        }

        // --- 2. 数据库清理 ---
        // 2.1 删除项目实体 (必须先于依赖它的表删除)
        projectMapper.deleteProjectById(projectId);

        // 2.2 删除回报档位
        rewardMapper.deleteByProjectId(projectId);

        // 2.3 删除项目关联的图片
        fileService.deleteFile(coverImageUrl);
    }
}