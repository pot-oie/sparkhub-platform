package com.pot.sparkhub.mapper;

import com.pot.sparkhub.dto.ProjectDetailDTO;
import com.pot.sparkhub.dto.ProjectSummaryDTO;
import com.pot.sparkhub.entity.Project;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProjectMapper {

    /**
     * [复杂查询] 分页查询项目摘要列表
     * (在 XML 中实现)
     * @param status 过滤项目状态 (e.g., 1=众筹中)
     */
    List<ProjectSummaryDTO> findProjectSummaries(@Param("status") Integer status);

    List<ProjectSummaryDTO> findProjectSummariesByCreatorId(Long creatorId);

    /**
     * [复杂查询] 根据ID查询项目详情 (包含回报)
     * (在 XML 中实现)
     */
    ProjectDetailDTO findProjectDetailById(@Param("id") Long id, @Param("currentUserId") Long currentUserId);

    /**
     * 插入一个新项目 (在 XML 中实现)
     * (MyBatis 会自动将生成的主键 ID 回填到 project 对象的 id 属性中)
     */
    void insertProject(Project project);

    /**
     * 根据 ID 查找一个项目实体 (用于更新)
     */
    @Select("SELECT * FROM project WHERE id = #{id}")
    Project findProjectById(@Param("id") Long id);

    /**
     * 根据状态查找项目 (用于定时任务)
     * (这里返回完整的 Project 实体，因为我们需要所有字段来做判断)
     */
    @Select("SELECT * FROM project WHERE status = #{status}")
    List<Project> findProjectsByStatus(@Param("status") Integer status);

    /**
     * 更新一个项目 (在 XML 中实现)
     */
    void updateProject(Project project);

    /**
     * [核心] 根据ID查询项目 (带悲观写锁)
     * (在 XML 中实现)
     */
    Project findProjectByIdForUpdate(@Param("id") Long id);

    /**
     * [子查询] 根据项目ID, 查询所有 status=1 (已支付) 的支持者 UserID 列表
     * @param projectId 项目ID
     * @return UserID 列表
     */
    @Select("SELECT DISTINCT backer_id FROM backing WHERE project_id = #{projectId} AND status = 1")
    List<Long> findBackerIdsByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据 ID 删除项目实体
     */
    @Delete("DELETE FROM project WHERE id = #{id}")
    void deleteProjectById(@Param("id") Long id);
}