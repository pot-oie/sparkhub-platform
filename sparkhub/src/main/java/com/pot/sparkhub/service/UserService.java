package com.pot.sparkhub.service;

import com.github.pagehelper.PageInfo;
import com.pot.sparkhub.dto.UpdateEmailDTO;
import com.pot.sparkhub.dto.UpdatePasswordDTO;
import com.pot.sparkhub.dto.UserDTO;
import com.pot.sparkhub.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    /**
     * [Admin] 分页获取所有用户
     */
    PageInfo<User> getAllUsers(int pageNum, int pageSize);

    /**
     * [Admin] 管理用户角色 (添加或移除)
     * @param userId 目标用户ID
     * @param roleName 角色名称
     * @param isAdd true=添加, false=移除
     * @return 更新后的 User 实体
     */
    User manageUserRole(Long userId, String roleName, boolean isAdd);

    /**
     * [User] 更新当前登录用户的邮箱
     * @param emailDTO 包含新邮箱
     */
    void updateEmail(UpdateEmailDTO emailDTO);

    /**
     * [User] 更新当前登录用户的密码
     * @param passwordDTO 包含旧密码和新密码
     */
    void updatePassword(UpdatePasswordDTO passwordDTO);

    /**
     * [User] 更新当前用户的头像
     * @param avatarUrl 选定的头像 URL 路径
     * @return 更新后的 UserDTO
     */
    UserDTO updateUserAvatar(String avatarUrl);

    /**
     * [User] 获取当前登录用户的信息
     * @return 当前用户的 UserDTO
     */
    UserDTO getMyInfo();
}