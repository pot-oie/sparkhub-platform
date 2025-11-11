package com.pot.sparkhub.mapper;

import com.pot.sparkhub.dto.CommentDetailDTO;
import com.pot.sparkhub.entity.ProjectComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectCommentMapper {

    /**
     * 插入新评论
     */
    @Insert("INSERT INTO project_comment (project_id, user_id, content, create_time, parent_id) " +
            "VALUES (#{projectId}, #{userId}, #{content}, #{createTime}, #{parentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ProjectComment comment);

    /**
     * 根据 ID 查找评论实体
     * (这是 CommentLikeServiceImpl 和 ProjectCommentServiceImpl 所需的)
     */
    @Select("SELECT * FROM project_comment WHERE id = #{id}")
    ProjectComment findById(@Param("id") Long id);

    /**
     * 查询一个项目的所有评论 (JOIN 用户信息)
     * (在 XML 中实现)
     */
    List<CommentDetailDTO> findByProjectId(@Param("projectId") Long projectId);

    /**
     * [批量查询] 查找一个用户在指定评论 ID 列表中的所有点赞
     * @param userId     当前用户 ID
     * @param commentIds 评论 ID 列表
     * @return 该用户已点赞的评论 ID 列表
     */
    List<Long> findLikedCommentIdsByUser(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);
}