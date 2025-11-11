package com.pot.sparkhub.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BackingCreateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 用户创建订单时, 只需要提交他们选择的"回报ID"
    private Long rewardId;

    // (金额/项目ID/用户ID 都不需要前端提交,
    //  应由后端根据 rewardId 和当前登录用户自动查询, 防止数据篡改)
}