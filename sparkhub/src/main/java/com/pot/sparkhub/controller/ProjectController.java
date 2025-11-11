package com.pot.sparkhub.controller;

import com.github.pagehelper.PageInfo;
import com.pot.sparkhub.common.Result;
import com.pot.sparkhub.dto.ProjectCreateDTO;
import com.pot.sparkhub.dto.ProjectDetailDTO;
import com.pot.sparkhub.dto.ProjectSummaryDTO;
import com.pot.sparkhub.dto.ProjectUpdateDTO;
import com.pot.sparkhub.entity.Project;
import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    // GET /api/projects
    // (例如: /api/projects?pageNum=1&pageSize=5)
    @GetMapping
    public Result<PageInfo<ProjectSummaryDTO>> getProjects(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        PageInfo<ProjectSummaryDTO> page = projectService.getPublicProjects(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * GET /api/projects/my
     * 获取我发起的项目 (需要 ROLE_CREATOR)
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('ROLE_CREATOR')") // 权限: 必须是发起者
    public Result<List<ProjectSummaryDTO>> getMyProjects() {
        // (注意: DTO 类型假设为 ProjectSummaryDTO，
        //  如果你的 service.getMyProjects() 返回其他类型, 请相应修改)
        List<ProjectSummaryDTO> myProjects = projectService.getMyProjects();
        return Result.success(myProjects);
    }

    // GET /api/projects/{id}
    // (添加 :\\d+ 正则表达式, 确保 id 只匹配数字)
    @GetMapping("/{id:\\d+}")
    public Result<ProjectDetailDTO> getProjectById(
            @PathVariable Long id,
            Authentication authentication // <-- 1. 添加此参数
    ) {
        try {
            // --- 2. 新增: 获取当前用户 ID ---
            Long currentUserId = null;
            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication.getPrincipal() instanceof String)) {
                User currentUser = (User) authentication.getPrincipal();
                currentUserId = currentUser.getId();
            }
            // --- ----------------------- ---

            // 3. 将 currentUserId 传递给 service
            ProjectDetailDTO detail = projectService.getProjectDetail(id, currentUserId);

            return Result.success(detail);
        } catch (Exception e) {
            return Result.error(404, e.getMessage()); // 404 Not Found
        }
    }

    /**
     * POST /api/projects
     * 创建一个新项目 (需要 ROLE_CREATOR)
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_CREATOR')") // 权限: 必须是发起者
    public Result<Project> createProject(@RequestBody ProjectCreateDTO createDTO) {
        try {
            Project newProject = projectService.createProject(createDTO);
            return Result.success(newProject);
        } catch (Exception e) {
            return Result.error(400, "项目创建失败: " + e.getMessage()); // 400 Bad Request
        }
    }

    /**
     * PUT /api/projects/{id}
     * 修改一个项目 (需要 ROLE_CREATOR 且是所有者)
     */
    @PutMapping("/{id:\\d+}")
    // (注意: 完整的权限检查放在 Service 层, Controller 层可以只做角色检查)
    @PreAuthorize("hasRole('ROLE_CREATOR')")
    public Result<Project> updateProject(@PathVariable Long id, @RequestBody ProjectUpdateDTO updateDTO) {
        try {
            // 安全检查 (含所有权) 在 service.updateProject 内部通过 @PreAuthorize 执行
            Project updatedProject = projectService.updateProject(id, updateDTO);
            return Result.success(updatedProject);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return Result.error(403, "权限不足: " + e.getMessage()); // 403 Forbidden
        } catch (RuntimeException e) {
            return Result.error(400, "项目更新失败: " + e.getMessage()); // 400 Bad Request
        }
    }
}