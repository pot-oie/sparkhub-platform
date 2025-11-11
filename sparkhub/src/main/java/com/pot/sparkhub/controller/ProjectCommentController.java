package com.pot.sparkhub.controller;

import com.pot.sparkhub.common.Result;
import com.pot.sparkhub.dto.CommentCreateDTO;
import com.pot.sparkhub.dto.CommentDetailDTO;
import com.pot.sparkhub.service.ProjectCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/comments")
public class ProjectCommentController {

    @Autowired
    private ProjectCommentService commentService;

    /**
     * GET /api/projects/{projectId}/comments
     * [公开] 获取项目的所有评论
     */
    @GetMapping
    public Result<List<CommentDetailDTO>> getComments(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "time") String sortBy // <-- [新增]
    ) {
        // List<CommentDetailDTO> comments = commentService.getCommentsByProject(projectId); // <-- 修改前
        List<CommentDetailDTO> comments = commentService.getCommentsByProject(projectId, sortBy); // <-- 修改后
        return Result.success(comments);
    }

    /**
     * POST /api/projects/{projectId}/comments
     * [需登录] 发表评论
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')") // 只有普通用户才能评论
    public Result<CommentDetailDTO> createComment(
            @PathVariable Long projectId,
            @RequestBody CommentCreateDTO createDTO
    ) {
        try {
            CommentDetailDTO newComment = commentService.createComment(projectId, createDTO);
            return Result.success(newComment);
        } catch (Exception e) {
            return Result.error(400, "评论失败: " + e.getMessage());
        }
    }
}