package com.pot.sparkhub.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 要操作的角色名称 (e.g., "ROLE_CREATOR", "ROLE_USER")
     */
    private String roleName;

    /**
     * 操作类型: true=添加角色, false=移除角色
     */
    private Boolean isAdd;
}