package com.pot.sparkhub.security;

import com.pot.sparkhub.entity.Project;
import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("projectSecurity") // 命名为 "projectSecurity" 以便在 @PreAuthorize 中调用
public class ProjectSecurity {

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * 检查当前登录用户是否是指定项目的所有者 (发起者)
     * @param authentication SecurityContext 中的认证信息
     * @param projectId      要检查的项目 ID
     * @return true 如果是所有者
     */
    public boolean isOwner(Authentication authentication, Long projectId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 1. 获取当前登录用户的 ID
        User currentUser = (User) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        // 2. 从数据库查询项目
        Project project = projectMapper.findProjectById(projectId);
        if (project == null) {
            // 项目不存在, (或者抛出异常)
            return false;
        }

        // 3. 比较 ID
        return project.getCreatorId().equals(currentUserId);
    }
}