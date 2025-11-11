package com.pot.sparkhub.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pot.sparkhub.dto.ProjectCreateDTO;
import com.pot.sparkhub.dto.ProjectDetailDTO;
import com.pot.sparkhub.dto.ProjectSummaryDTO;
import com.pot.sparkhub.dto.ProjectUpdateDTO;
import com.pot.sparkhub.entity.Project;
import com.pot.sparkhub.entity.ProjectReward;
import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.mapper.ProjectMapper;
import com.pot.sparkhub.mapper.RewardMapper;
import com.pot.sparkhub.service.NotificationService;
import com.pot.sparkhub.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private RewardMapper rewardMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ProjectCleaner projectCleaner;

    // 状态: 1 = 众筹中
    private static final Integer STATUS_ACTIVE = 1;
    // 状态: 0 = 审核中
    private static final Integer STATUS_AUDITING = 0;
    // 状态: 3 = 众筹失败
    private static final Integer STATUS_FAILED = 3;

    @Override
    @Cacheable(value = "projectList", key = "'public:' + #pageNum + ':' + #pageSize")
    public PageInfo<ProjectSummaryDTO> getPublicProjects(int pageNum, int pageSize) {
        // 1. 启动分页 (PageHelper)
        PageHelper.startPage(pageNum, pageSize);

        // 2. 查询 (只查询 "众筹中" 的项目)
        List<ProjectSummaryDTO> list = projectMapper.findProjectSummaries(STATUS_ACTIVE);

        // 3. PageHelper 会返回一个 PageInfo 对象, 包含列表和所有分页信息
        return new PageInfo<>(list);
    }

    @Override
    @Cacheable(value = "project", key = "#id")
    public ProjectDetailDTO getProjectDetail(Long id, Long currentUserId) { // <-- 1. 修改签名

        // 2. 将 currentUserId 传递给 Mapper
        ProjectDetailDTO detail = projectMapper.findProjectDetailById(id, currentUserId);

        if (detail == null) {
            throw new RuntimeException("项目不存在 (ID: " + id + ")");
        }

        // 3. 安全检查
        // 公开接口不应返回 "审核中" 的项目,
        // 除非查看者是项目创建者本人 (currentUserId 匹配 creatorId) 或管理员
        if (STATUS_AUDITING.equals(detail.getStatus())) {

            // 3.1 检查当前用户是否为 Admin
            boolean isAdmin = false;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
                isAdmin = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            }

            // 3.2 检查是否为创建者
            boolean isOwner = (currentUserId != null && currentUserId.equals(detail.getCreatorId()));

            // 3.3 只有在 *既不是* 创建者, *也不是* 管理员时, 才抛出异常
            if (!isOwner && !isAdmin) {
                throw new RuntimeException("项目不存在或正在审核中");
            }
            // 如果是创建者本人, 则允许查看
        }
        // --- ----------------- ---

        return detail;
    }

    @Override
    @Transactional // 开启事务: 项目和回报必须同时成功
    public Project createProject(ProjectCreateDTO createDTO) {
        // 1. 获取当前登录的用户 (发起者)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User creator = (User) auth.getPrincipal();

        // 2. 检查回报档位
        if (createDTO.getRewards() == null || createDTO.getRewards().isEmpty()) {
            throw new RuntimeException("项目必须至少包含一个回报档位");
        }

        // 3. 回报总金额 vs 目标金额
        BigDecimal totalAmountIfSoldOut = createDTO.getRewards().stream()
                .map(rewardDTO ->
                        rewardDTO.getAmount().multiply(BigDecimal.valueOf(rewardDTO.getStock()))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 校验逻辑：所有回报档位全部售罄，总金额必须大于或等于目标金额。
        if (totalAmountIfSoldOut.compareTo(createDTO.getGoalAmount()) < 0) {
            throw new RuntimeException("回报档位设置不合理：即使所有回报全部售罄，总筹集金额 ("
                    + totalAmountIfSoldOut.setScale(2, RoundingMode.HALF_UP).toString() + " 元) 也无法达到目标金额 ("
                    + createDTO.getGoalAmount().setScale(2, RoundingMode.HALF_UP).toString() + " 元)。请调整库存或金额。");
        }

        // 4. 映射 DTO -> Project 实体
        Project project = new Project();
        project.setCreatorId(creator.getId()); // 设置发起者
        project.setCategoryId(createDTO.getCategoryId());
        project.setTitle(createDTO.getTitle());
        project.setDescription(createDTO.getDescription());
        project.setCoverImage(createDTO.getCoverImage());
        project.setGoalAmount(createDTO.getGoalAmount());
        project.setEndTime(createDTO.getEndTime());

        // 5. 设置后端控制的字段
        project.setStatus(STATUS_AUDITING); // 0: 审核中
        project.setCurrentAmount(BigDecimal.ZERO);
        project.setCreateTime(LocalDateTime.now());

        // 6. 插入 project 表 (MyBatis 会回填 ID)
        projectMapper.insertProject(project);
        Long newProjectId = project.getId(); // 获取新ID

        // 7. 检查并映射回报档位 DTOs -> Entities
        if (createDTO.getRewards() == null || createDTO.getRewards().isEmpty()) {
            throw new RuntimeException("项目必须至少包含一个回报档位");
        }

        List<ProjectReward> rewards = createDTO.getRewards().stream()
                .map(rewardDTO -> {
                    ProjectReward reward = new ProjectReward();
                    reward.setProjectId(newProjectId); // 关联到刚创建的项目
                    reward.setTitle(rewardDTO.getTitle());
                    reward.setDescription(rewardDTO.getDescription());
                    reward.setAmount(rewardDTO.getAmount());
                    reward.setStock(rewardDTO.getStock());
                    reward.setImageUrl(rewardDTO.getImageUrl());
                    return reward;
                }).collect(Collectors.toList());

        // 8. 批量插入 project_reward 表
        rewardMapper.insertRewardList(rewards);

        // 9. 发送通知
        notificationService.sendSystemNotification(
                creator.getId(),
                "PROJECT_CREATED",
                String.format("您的项目 '%s' 已成功创建，正在等待审核。", project.getTitle()),
                "/my-projects" // (跳转到“我的项目”列表页)
        );

        return project;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_CREATOR') and @projectSecurity.isOwner(authentication, #id)")
    @Transactional // 开启事务
    @Caching(evict = {
            // 1. 精确清除项目详情缓存
            @CacheEvict(value = "project", key = "#id"),
            // 2. 范围清除项目列表缓存 (因为状态可能已改变)
            @CacheEvict(value = "projectList", allEntries = true)
    })
    public Project updateProject(Long id, ProjectUpdateDTO updateDTO) {
        // 1. 安全检查 (由 @PreAuthorize 搞定)

        // 2. 获取项目当前状态
        Project project = projectMapper.findProjectById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        // 3. 业务逻辑: 只有 "审核中"(0) 或 "失败"(3) 的项目才能修改
        if (project.getStatus() == 1 || project.getStatus() == 2) {
            throw new RuntimeException("项目正在众筹或已成功, 无法修改");
        }

        // 4. 映射 DTO -> Entity (只更新允许的字段)
        project.setCategoryId(updateDTO.getCategoryId());
        project.setTitle(updateDTO.getTitle());
        project.setDescription(updateDTO.getDescription());
        project.setCoverImage(updateDTO.getCoverImage());
        project.setEndTime(updateDTO.getEndTime());
        project.setGoalAmount(BigDecimal.valueOf(updateDTO.getGoalAmount()));

        // 5. 关键: 每次修改后, 状态必须重置回 "审核中"
        project.setStatus(STATUS_AUDITING); // 0: 审核中

        // 6. 执行更新 (使用 ProjectMapper.xml 中的动态 UPDATE)
        projectMapper.updateProject(project);

        // 7. 更新回报档位 (删除旧的, 插入新的)

        // 7.1 检查回报
        if (updateDTO.getRewards() == null || updateDTO.getRewards().isEmpty()) {
            throw new RuntimeException("项目必须至少包含一个回报档位");
        }

        // 7.2 删除旧的回报
        rewardMapper.deleteByProjectId(id);

        // 7.3 映射 DTOs -> Entities (复用 createProject 的逻辑)
        List<ProjectReward> rewards = updateDTO.getRewards().stream()
                .map(rewardDTO -> {
                    ProjectReward reward = new ProjectReward();
                    reward.setProjectId(id); // [注意] 关联到 *现有* 项目 ID
                    reward.setTitle(rewardDTO.getTitle());
                    reward.setDescription(rewardDTO.getDescription());
                    reward.setAmount(rewardDTO.getAmount());
                    reward.setStock(rewardDTO.getStock());
                    reward.setImageUrl(rewardDTO.getImageUrl());
                    return reward;
                }).collect(Collectors.toList());

        // 7.4 批量插入新的 project_reward 表
        rewardMapper.insertRewardList(rewards);

        // 8. 发送通知
        notificationService.sendSystemNotification(
                project.getCreatorId(),
                "PROJECT_UPDATED",
                String.format("您的项目 '%s' 已修改并重新提交审核。", project.getTitle()),
                "/my-projects"
        );

        return project;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_CREATOR')") // 权限: 必须是发起者
    public List<ProjectSummaryDTO> getMyProjects() {
        // 1. 获取当前登录用户的 ID, 逻辑同 createProject
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User creator = (User) auth.getPrincipal();
        Long currentUserId = creator.getId();

        // 2. 调用 Mapper 查询 (假设你有一个 findProjectSummariesByCreatorId 方法)
        // (注意: 这会返回所有状态的项目, 包括审核中、失败等, 这正是我们想要的)
        List<ProjectSummaryDTO> list = projectMapper.findProjectSummariesByCreatorId(currentUserId);

        return list;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 权限
    public PageInfo<ProjectSummaryDTO> getProjectsForAdmin(int pageNum, int pageSize, Integer status) {
        // 1. 启动分页
        PageHelper.startPage(pageNum, pageSize);

        // 2. 传入 status，由 Mapper 负责根据 status 是否为 null 来决定是否添加 WHERE 条件
        List<ProjectSummaryDTO> list = projectMapper.findProjectSummaries(status);

        return new PageInfo<>(list);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 权限
    @Caching(evict = {
            // 审核通过/拒绝, 都会影响项目详情和列表
            @CacheEvict(value = "project", key = "#id"),
            @CacheEvict(value = "projectList", allEntries = true)
    })
    public Project auditProject(Long id, Integer newStatus) {
        // 1. 验证项目
        Project project = projectMapper.findProjectById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        // 2. 验证状态
        if (newStatus == null || newStatus < 0 || newStatus > 3) {
            throw new RuntimeException("无效的状态码");
        }

        if (STATUS_FAILED.equals(newStatus)) {
            // --- 调用 ProjectCleaner 组件 ---

            // 1. 执行删除操作 (通过代理调用，@Transactional 生效)
            projectCleaner.deleteProjectAndRelatedData(id, project.getCoverImage());

            // 2. 发送通知
            notificationService.sendSystemNotification(
                    project.getCreatorId(),
                    "PROJECT_REJECTED",
                    String.format("您的项目 '%s' 未能通过审核。", project.getTitle()),
                    "/my-projects"
            );

            // 前端会接收到 200 OK，并显示一个友好的消息
            project.setStatus(STATUS_FAILED); // 标记状态为失败 (虽然已被删除)
            return project;
        }

        // --- 审核通过 ---
        // 3. 更新状态
        project.setStatus(newStatus);

        // 4. 持久化 (使用动态 UPDATE)
        projectMapper.updateProject(project);

        // 5. 发送通知
        if (STATUS_ACTIVE.equals(newStatus)) {
            // 审核通过
            notificationService.sendSystemNotification(
                    project.getCreatorId(),
                    "PROJECT_APPROVED",
                    String.format("恭喜！您的项目 '%s' 已通过审核，开始众筹。", project.getTitle()),
                    "/project/" + project.getId()
            );
        }
        return project;
    }
}