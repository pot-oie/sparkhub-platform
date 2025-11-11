package com.pot.sparkhub.mapper;

import com.pot.sparkhub.dto.ProjectSummaryDTO;
import com.pot.sparkhub.entity.UserFavorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    /**
     * 1. 检查收藏是否已存在
     */
    @Select("SELECT * FROM user_favorite WHERE user_id = #{userId} AND project_id = #{projectId}")
    UserFavorite findByUserAndProject(@Param("userId") Long userId, @Param("projectId") Long projectId);

    /**
     * 2. 插入新收藏
     */
    @Insert("INSERT INTO user_favorite (user_id, project_id, create_time) " +
            "VALUES (#{userId}, #{projectId}, #{createTime})")
    void insert(UserFavorite favorite);

    /**
     * 3. 删除收藏
     */
    @Delete("DELETE FROM user_favorite WHERE user_id = #{userId} AND project_id = #{projectId}")
    void deleteByUserAndProject(@Param("userId") Long userId, @Param("projectId") Long projectId);

    /**
     * 4. 查询我的收藏列表 (JOIN Project), 在 XML 中实现
     * (注意: 返回的是 ProjectSummaryDTO)
     */
    List<ProjectSummaryDTO> findFavoritesByUserId(@Param("userId") Long userId);
}