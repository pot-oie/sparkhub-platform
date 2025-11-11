package com.pot.sparkhub.service.impl;

import com.pot.sparkhub.entity.ProjectComment;
import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.mapper.CommentLikeMapper;
import com.pot.sparkhub.mapper.ProjectCommentMapper;
import com.pot.sparkhub.service.CommentLikeService;
import com.pot.sparkhub.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentLikeServiceImpl implements CommentLikeService {

    @Autowired
    private CommentLikeMapper likeMapper;
    @Autowired
    private ProjectCommentMapper commentMapper;
    @Autowired
    private NotificationService notificationService;

    // (辅助方法) 获取当前登录的用户
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new RuntimeException("用户未登录");
        }
        return (User) auth.getPrincipal();
    }

    @Override
    @Transactional // 必须是事务
    public void likeComment(Long commentId) {
        User user = getCurrentUser();
        Long userId = user.getId();

        // 1. 检查是否已点赞 (幂等性)
        if (likeMapper.exists(userId, commentId)) {
            return; // 已经点赞, 无需操作
        }

        // 2. 插入点赞记录
        likeMapper.insert(userId, commentId);

        // 3. 增加评论总数
        likeMapper.incrementCommentLikeCount(commentId);

        // 4. 发送通知
        // 4.1 查询被点赞的评论
        ProjectComment likedComment = commentMapper.findById(commentId);

        // 4.2 检查是否有效 且 不是自己点赞自己
        if (likedComment != null && !likedComment.getUserId().equals(userId)) {
            notificationService.sendUserNotification(
                    likedComment.getUserId(), // (接收者：评论作者)
                    "NEW_LIKE_ON_COMMENT",
                    String.format("用户 '%s' 点赞了您的评论。", user.getUsername()),
                    // (跳转链接：项目ID + 评论锚点)
                    "/project/" + likedComment.getProjectId() + "#comment-" + likedComment.getId(),
                    userId // (发送者：点赞的人)
            );
        }
    }

    @Override
    @Transactional // 必须是事务
    public void unlikeComment(Long commentId) {
        User user = getCurrentUser();
        Long userId = user.getId();

        // 1. 检查是否已点赞
        if (!likeMapper.exists(userId, commentId)) {
            return; // 没点过赞, 无需操作
        }

        // 2. 删除点赞记录
        likeMapper.delete(userId, commentId);

        // 3. 减少评论总数
        likeMapper.decrementCommentLikeCount(commentId);
    }
}