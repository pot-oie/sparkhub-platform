package com.pot.sparkhub.service;

import com.pot.sparkhub.dto.BackingCreateDTO;
import com.pot.sparkhub.dto.BackingDetailDTO;
import com.pot.sparkhub.entity.Backing;

import java.util.List;

public interface BackingService {

    /**
     * 1. 创建订单 (第一步)
     */
    Backing createBacking(BackingCreateDTO createDTO);

    /**
     * 2. 执行虚拟支付 (第二步, 核心事务)
     */
    Backing executePayment(Long backingId);

    /**
     * 3. 获取我的订单列表
     */
    List<BackingDetailDTO> getMyBackings();
}