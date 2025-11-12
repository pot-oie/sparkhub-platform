package com.pot.sparkhub.controller;

import com.github.pagehelper.PageInfo;
import com.pot.sparkhub.common.Result;
import com.pot.sparkhub.dto.ProjectAuditDTO;
import com.pot.sparkhub.dto.ProjectSummaryDTO;
import com.pot.sparkhub.dto.RoleUpdateDTO;
import com.pot.sparkhub.entity.Project;
import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.service.ProjectService;
import com.pot.sparkhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')") // <-- 关键: 整个控制器都需要 ADMIN 权限
public class AdminController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    /**
     * GET /api/admin/projects
     * 查看所有项目 (用于审核)
     */
    @GetMapping("/projects")
    public Result<PageInfo<ProjectSummaryDTO>> getProjectsForAdmin(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status
    ) {
        PageInfo<ProjectSummaryDTO> page = projectService.getProjectsForAdmin(pageNum, pageSize, status);
        return Result.success(page);
    }

    /**
     * PUT /api/admin/projects/{id}/status
     * 审核项目
     */
    @PutMapping("/projects/{id}/status")
    public Result<Project> auditProject(
            @PathVariable Long id,
            @RequestBody ProjectAuditDTO auditDTO
    ) {
        try {
            Project auditedProject = projectService.auditProject(id, auditDTO.getStatus());
            return Result.success(auditedProject);
        } catch (Exception e) {
            return Result.error(400, "审核失败: " + e.getMessage());
        }
    }

    /**
     * GET /api/admin/users
     * 查看所有用户列表
     */
    @GetMapping("/users")
    public Result<PageInfo<User>> getAllUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        // (警告: User 实体包含密码, 生产环境应返回 UserDTO)
        PageInfo<User> page = userService.getAllUsers(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * PUT /api/admin/users/{id}/role
     * 管理员修改用户角色 (添加或移除)
     */
    @PutMapping("/users/{id}/role")
    public Result<User> manageUserRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateDTO roleUpdateDTO
    ) {
        try {
            // 业务逻辑验证
            if (roleUpdateDTO.getRoleName() == null || roleUpdateDTO.getIsAdd() == null) {
                return Result.error(400, "请求参数不完整");
            }

            // 执行服务操作
            User updatedUser = userService.manageUserRole(
                    id,
                    roleUpdateDTO.getRoleName(),
                    roleUpdateDTO.getIsAdd()
            );

            return Result.success(updatedUser);
        } catch (Exception e) {
            return Result.error(400, "角色管理失败: " + e.getMessage());
        }
    }
}