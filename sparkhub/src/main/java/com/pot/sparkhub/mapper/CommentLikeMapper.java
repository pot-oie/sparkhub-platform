package com.pot.sparkhub.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface CommentLikeMapper {

    /**
     * 检查点赞是否已存在
     */
    @Select("SELECT COUNT(*) FROM project_comment_like WHERE user_id = #{userId} AND comment_id = #{commentId}")
    boolean exists(@Param("userId") Long userId, @Param("commentId") Long commentId);

    /**
     * 插入点赞
     */
    @Insert("INSERT INTO project_comment_like (user_id, comment_id, create_time) VALUES (#{userId}, #{commentId}, NOW())")
    void insert(@Param("userId") Long userId, @Param("commentId") Long commentId);

    /**
     * 删除点赞
     */
    @Delete("DELETE FROM project_comment_like WHERE user_id = #{userId} AND comment_id = #{commentId}")
    void delete(@Param("userId") Long userId, @Param("commentId") Long commentId);

    /**
     * [核心] 增加评论的点赞数
     */
    @Update("UPDATE project_comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    void incrementCommentLikeCount(@Param("commentId") Long commentId);

    /**
     * [核心] 减少评论的点赞数
     */
    @Update("UPDATE project_comment SET like_count = GREATEST(0, like_count - 1) WHERE id = #{commentId}")
    void decrementCommentLikeCount(@Param("commentId") Long commentId);
}