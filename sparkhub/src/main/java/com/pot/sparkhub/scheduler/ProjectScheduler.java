package com.pot.sparkhub.scheduler;

import com.pot.sparkhub.entity.Project;
import com.pot.sparkhub.mapper.ProjectMapper;
import com.pot.sparkhub.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ProjectScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProjectScheduler.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private NotificationService notificationService;

    // 项目状态 (从 ProjectServiceImpl 复制)
    private static final Integer STATUS_ACTIVE = 1;     // 众筹中
    private static final Integer STATUS_SUCCESSFUL = 2; // 众筹成功
    private static final Integer STATUS_FAILED = 3;     // 众筹失败

    /**
     * 定时任务：检查众筹到期的项目
     * "fixedRate = 3600000" = 每 1 小时 (3,600,000 毫秒) 执行一次
     * (你可以调整这个值，例如 60000 = 每分钟, 用于测试)
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void checkExpiredProjects() {
        log.info("定时任务：开始检查到期的众筹项目...");

        // 1. 查找所有“众筹中”的项目
        List<Project> activeProjects = projectMapper.findProjectsByStatus(STATUS_ACTIVE);
        if (activeProjects.isEmpty()) {
            log.info("定时任务：没有正在众筹的项目，任务结束。");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int updatedCount = 0;

        for (Project project : activeProjects) {
            // 2. 检查项目是否到期 (endTime 是否早于 now)
            if (project.getEndTime() != null && project.getEndTime().isBefore(now)) {

                // 3. 项目已到期, 判断成功或失败
                Integer newStatus;
                String notificationType;
                String notificationContent;

                // 比较 BigDecimal: currentAmount >= goalAmount
                if (project.getCurrentAmount().compareTo(project.getGoalAmount()) >= 0) {
                    // --- 成功 ---
                    newStatus = STATUS_SUCCESSFUL;
                    notificationType = "PROJECT_FUNDED";
                    notificationContent = String.format("太棒了！您的项目 '%s' 已成功达成众筹目标！", project.getTitle());
                } else {
                    // --- 失败 ---
                    newStatus = STATUS_FAILED;
                    notificationType = "PROJECT_FAILED";
                    notificationContent = String.format("很遗憾，您的项目 '%s' 未能在截止日期前达成目标。", project.getTitle());
                }

                // 4. 更新数据库中的项目状态
                project.setStatus(newStatus);
                projectMapper.updateProject(project); // (使用现有的 updateProject 更新状态)
                updatedCount++;
                log.info("项目 ID: {} 已到期, 状态更新为: {}", project.getId(), newStatus);

                // 5. 发送通知给项目发起者
                notificationService.sendSystemNotification(
                        project.getCreatorId(),
                        notificationType,
                        notificationContent,
                        "/project/" + project.getId() // 统一跳转到项目详情页
                );
            }
        }

        if (updatedCount > 0) {
            log.info("定时任务：共更新了 {} 个到期项目。", updatedCount);
        } else {
            log.info("定时任务：没有项目到期，任务结束。");
        }
    }
}