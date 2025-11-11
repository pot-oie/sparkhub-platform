package com.pot.sparkhub.service;

import com.pot.sparkhub.dto.CommentCreateDTO;
import com.pot.sparkhub.dto.CommentDetailDTO;
import java.util.List;

public interface ProjectCommentService {
    /**
     * [公开] 获取一个项目的所有评论
     * @param projectId 项目ID
     * @param sortBy 排序方式 ("time" 或 "hotness")
     */
    List<CommentDetailDTO> getCommentsByProject(Long projectId, String sortBy);

    /**
     * [需登录] 创建一条新评论
     */
    CommentDetailDTO createComment(Long projectId, CommentCreateDTO createDTO);
}