package com.pot.sparkhub.mapper;

import com.pot.sparkhub.entity.ProjectReward;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RewardMapper {

    /**
     * 批量插入回报档位
     * (在 XML 中实现)
     */
    void insertRewardList(@Param("rewards") List<ProjectReward> rewards);

    /**
     * 根据项目 ID 删除所有回报
     */
    @Delete("DELETE FROM project_reward WHERE project_id = #{projectId}")
    void deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据ID查询回报
     */
    @Select("SELECT * FROM project_reward WHERE id = #{id}")
    ProjectReward findById(@Param("id") Long id);

    /**
     * [核心] 根据ID查询回报 (带悲观写锁)
     * (在 XML 中实现)
     */
    ProjectReward findByIdForUpdate(@Param("id") Long id);

    /**
     * 更新回报 (在 XML 中实现)
     * (用于扣减库存)
     */
    void update(ProjectReward reward);

    /**
     * [新增] 根据项目 ID 查找所有回报 (用于删除图片)
     */
    @Select("SELECT * FROM project_reward WHERE project_id = #{projectId}")
    List<ProjectReward> findByProjectId(@Param("projectId") Long projectId);
}