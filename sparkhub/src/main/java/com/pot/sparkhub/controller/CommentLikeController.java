package com.pot.sparkhub.controller;

import com.pot.sparkhub.common.Result;
import com.pot.sparkhub.service.CommentLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments") // 注意: 这是一个新的根路径
@PreAuthorize("hasRole('ROLE_USER')") // 整个模块都需要登录
public class CommentLikeController {

    @Autowired
    private CommentLikeService likeService;

    /**
     * POST /api/comments/{commentId}/like
     * 点赞
     */
    @PostMapping("/{commentId}/like")
    public Result<?> likeComment(@PathVariable Long commentId) {
        try {
            likeService.likeComment(commentId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * DELETE /api/comments/{commentId}/like
     * 取消点赞
     */
    @DeleteMapping("/{commentId}/like")
    public Result<?> unlikeComment(@PathVariable Long commentId) {
        try {
            likeService.unlikeComment(commentId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}