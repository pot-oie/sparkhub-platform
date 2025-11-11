package com.pot.sparkhub.service.impl;

import com.pot.sparkhub.dto.CommentCreateDTO;
import com.pot.sparkhub.dto.CommentDetailDTO;
import com.pot.sparkhub.entity.Project;
import com.pot.sparkhub.entity.ProjectComment;
import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.mapper.ProjectCommentMapper;
import com.pot.sparkhub.mapper.ProjectMapper;
import com.pot.sparkhub.service.NotificationService;
import com.pot.sparkhub.service.ProjectCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectCommentServiceImpl implements ProjectCommentService {

    @Autowired
    private ProjectCommentMapper commentMapper;

    @Autowired
    private ProjectMapper projectMapper; // 用于验证项目是否存在

    @Autowired
    private NotificationService notificationService;
    // 获取当前登录的用户 (辅助方法)
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new RuntimeException("用户未登录");
        }
        return (User) auth.getPrincipal();
    }

    /**
     * [安全] 获取当前用户, 如果未登录则返回 null
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return null; // 未登录
        }
        return (User) auth.getPrincipal();
    }

    /**
     * 获取评论 (构建树, 排序, 并设置 isLiked 状态)
     */
    @Override
    public List<CommentDetailDTO> getCommentsByProject(Long projectId, String sortBy) {

        // 1. 检查当前用户是否登录
        User currentUser = getAuthenticatedUser();

        // 2. 从 Mapper 获取所有评论的“扁平列表” (按时间 ASC)
        List<CommentDetailDTO> allComments = commentMapper.findByProjectId(projectId);
        if (allComments.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 【新增】如果用户已登录, 则批量查询点赞状态
        final Set<Long> likedCommentIds;

        if (currentUser != null) {
            // 3.1 提取所有评论 ID
            List<Long> commentIds = allComments.stream()
                    .map(CommentDetailDTO::getId)
                    .collect(Collectors.toList());

            // 3.2 批量查询数据库
            if (!commentIds.isEmpty()) {
                likedCommentIds = new HashSet<>(
                        commentMapper.findLikedCommentIdsByUser(currentUser.getId(), commentIds)
                );
            } else {
                likedCommentIds = Collections.emptySet();
            }
        } else {
            likedCommentIds = Collections.emptySet();
        }

        // 4. 将所有评论放入 Map, Key 是评论 ID
        Map<Long, CommentDetailDTO> commentMap = allComments.stream()
                .collect(Collectors.toMap(CommentDetailDTO::getId, comment -> {
                    comment.setReplies(new ArrayList<>());

                    // 4.1 【新增】设置 isLiked 状态
                    if (likedCommentIds.contains(comment.getId())) {
                        comment.setLiked(true);
                    }
                    // (如果未登录或未点赞, 默认为 false)

                    return comment;
                }));

        // 5. 遍历 Map, 构建“树”
        List<CommentDetailDTO> topLevelComments = new ArrayList<>();

        for (CommentDetailDTO comment : commentMap.values()) {
            if (comment.getParentId() == null) {
                topLevelComments.add(comment);
            } else {
                CommentDetailDTO parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    parent.getReplies().add(comment);
                }
            }
        }

        // 6. 对顶层评论进行排序
        if (Objects.equals(sortBy, "hotness")) {
            topLevelComments.sort(Comparator.comparing(CommentDetailDTO::getLikeCount).reversed());
        } else {
            topLevelComments.sort(Comparator.comparing(CommentDetailDTO::getCreateTime).reversed());
        }

        return topLevelComments;
    }

    /**
     * 创建评论 (保存 parentId)
     */
    @Override
    @Transactional
    public CommentDetailDTO createComment(Long projectId, CommentCreateDTO createDTO) {
        // 1. 获取当前登录用户
        User user = getCurrentUser();

        // 2. 验证项目是否存在
        Project project = projectMapper.findProjectById(projectId);
        if (project == null) {
            throw new RuntimeException("评论的项目不存在");
        }

        // 3. 创建实体
        ProjectComment comment = new ProjectComment();
        comment.setProjectId(projectId);
        comment.setUserId(user.getId());
        comment.setContent(createDTO.getContent());
        comment.setCreateTime(LocalDateTime.now());
        comment.setParentId(createDTO.getParentId());

        // 5. 插入数据库
        commentMapper.insert(comment); // ID 会被回填

        // 6. 发送通知
        String linkUrl = "/project/" + projectId + "#comment-" + comment.getId();

        if (createDTO.getParentId() == null) {
            // --- 6A. 顶级评论 ---
            // 通知项目发起者 (前提：不是自己评论自己的项目)
            if (!project.getCreatorId().equals(user.getId())) {
                notificationService.sendUserNotification(
                        project.getCreatorId(), // (接收者：项目发起者)
                        "NEW_COMMENT_ON_PROJECT",
                        String.format("用户 '%s' 评论了您的项目 '%s'。", user.getUsername(), project.getTitle()),
                        linkUrl,
                        user.getId() // (发送者：评论者)
                );
            }
        } else {
            // --- 6B. 回复评论 ---
            // 查询父评论
            ProjectComment parentComment = commentMapper.findById(createDTO.getParentId());
            if (parentComment != null) {
                // 通知父评论的作者 (前提：不是自己回复自己)
                if (!parentComment.getUserId().equals(user.getId())) {
                    notificationService.sendUserNotification(
                            parentComment.getUserId(), // (接收者：被回复的人)
                            "NEW_REPLY_TO_COMMENT",
                            String.format("用户 '%s' 回复了您的评论。", user.getUsername()),
                            linkUrl,
                            user.getId() // (发送者：回复者)
                    );
                }
            }
        }

        // 7. 构造 DTO 返回给前端
        CommentDetailDTO detailDTO = new CommentDetailDTO();
        detailDTO.setId(comment.getId());
        detailDTO.setContent(comment.getContent());
        detailDTO.setCreateTime(comment.getCreateTime());
        detailDTO.setUserId(user.getId());
        detailDTO.setUsername(user.getUsername());
        detailDTO.setAvatar(user.getAvatar());
        detailDTO.setParentId(comment.getParentId());
        detailDTO.setReplies(new ArrayList<>());
        detailDTO.setLikeCount(0);
        detailDTO.setLiked(false);

        return detailDTO;
    }
}