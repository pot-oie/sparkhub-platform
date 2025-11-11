package com.pot.sparkhub.service;

import com.github.pagehelper.PageInfo;
import com.pot.sparkhub.dto.ProjectCreateDTO;
import com.pot.sparkhub.dto.ProjectDetailDTO;
import com.pot.sparkhub.dto.ProjectSummaryDTO;
import com.pot.sparkhub.dto.ProjectUpdateDTO;
import com.pot.sparkhub.entity.Project;

import java.util.List;

public interface ProjectService {

    /**
     * 分页获取公开的项目列表 (只显示 "众筹中" 的)
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageInfo<ProjectSummaryDTO> getPublicProjects(int pageNum, int pageSize);

    /**
     * 获取项目详情
     * @param id 项目ID
     * @param currentUserId 当前登录的用户ID (可为 null)
     * @return 项目详情 DTO
     */
    ProjectDetailDTO getProjectDetail(Long id, Long currentUserId);

    /**
     * 创建一个新项目
     * @param createDTO 项目创建 DTO
     * @return 创建后的 Project 实体
     */
    Project createProject(ProjectCreateDTO createDTO);

    /**
     * 更新一个项目
     * @param id        项目ID
     * @param updateDTO 项目更新 DTO
     * @return 更新后的 Project 实体
     */
    Project updateProject(Long id, ProjectUpdateDTO updateDTO);

    /**
     * [Creator] 获取当前登录用户创建的所有项目
     * @return 项目列表
     */
    List<ProjectSummaryDTO> getMyProjects();

    /**
     * [Admin] 分页获取所有项目 (用于审核)
     */
    PageInfo<ProjectSummaryDTO> getProjectsForAdmin(int pageNum, int pageSize, Integer status);

    /**
     * [Admin] 审核项目
     */
    Project auditProject(Long id, Integer newStatus);
}