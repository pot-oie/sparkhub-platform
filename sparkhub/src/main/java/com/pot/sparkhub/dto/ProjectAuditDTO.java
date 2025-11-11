package com.pot.sparkhub.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ProjectAuditDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 0:审核中, 1:众筹中, 2:成功, 3:失败
    private Integer status;
}