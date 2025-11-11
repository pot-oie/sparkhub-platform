package com.pot.sparkhub.service.impl;

import com.pot.sparkhub.dto.BackingCreateDTO;
import com.pot.sparkhub.dto.BackingDetailDTO;
import com.pot.sparkhub.entity.Backing;
import com.pot.sparkhub.entity.Project;
import com.pot.sparkhub.entity.ProjectReward;
import com.pot.sparkhub.entity.User;
import com.pot.sparkhub.mapper.BackingMapper;
import com.pot.sparkhub.mapper.ProjectMapper;
import com.pot.sparkhub.mapper.RewardMapper;
import com.pot.sparkhub.service.BackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class BackingServiceImpl implements BackingService {

    @Autowired
    private BackingMapper backingMapper;

    @Autowired
    private RewardMapper rewardMapper;

    @Autowired
    private ProjectMapper projectMapper;

    // 订单状态 (常量)
    private static final Integer STATUS_PENDING = 0;
    private static final Integer STATUS_PAID = 1;
    private static final Integer STATUS_CANCELED = 2;

    // 项目状态 (常量)
    private static final Integer PROJECT_STATUS_ACTIVE = 1; // 众筹中

    // 获取当前登录的用户
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new RuntimeException("用户未登录");
        }
        return (User) auth.getPrincipal();
    }

    /**
     * 1. 创建订单 (步骤1)
     */
    @Override
    @Transactional // 保证查询和创建的原子性
    public Backing createBacking(BackingCreateDTO createDTO) {
        User user = getCurrentUser();
        Long rewardId = createDTO.getRewardId();

        // 1. 验证回报
        ProjectReward reward = rewardMapper.findById(rewardId);
        if (reward == null) {
            throw new RuntimeException("回报档位不存在");
        }

        // 2. 验证项目
        Project project = projectMapper.findProjectById(reward.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        // 3. 业务逻辑检查: 项目是否还在"众筹中"?
        if (!Objects.equals(project.getStatus(), PROJECT_STATUS_ACTIVE)) {
            throw new RuntimeException("项目未处于众筹中状态");
        }

        // 4. 业务逻辑检查: 项目是否已过截止日期?
        if (LocalDateTime.now().isAfter(project.getEndTime())) {
            throw new RuntimeException("项目已过众筹截止日期");
        }

        // (注意: 库存检查在"支付"那一步才锁定和扣减)

        // 5. 创建 Backing 实体
        Backing backing = new Backing();
        backing.setBackerId(user.getId());
        backing.setProjectId(project.getId());
        backing.setRewardId(rewardId);
        backing.setBackingAmount(reward.getAmount()); // 金额以回报档位为准
        backing.setStatus(STATUS_PENDING); // 0: 待支付
        backing.setCreateTime(LocalDateTime.now());

        // 6. 插入数据库
        backingMapper.insert(backing); // ID 将被回填
        return backing;
    }

    /**
     * 2. 执行虚拟支付 (步骤2 - 核心事务)
     */
    @Override
    @Transactional // (!!!!) 必须开启事务, 保证所有操作原子性
    @Caching(evict = {
            // 支付成功, 必须清除缓存
            @CacheEvict(value = "project", key = "#result.projectId"),
            @CacheEvict(value = "projectList", allEntries = true)
    })
    public Backing executePayment(Long backingId) {
        User user = getCurrentUser();

        // --- 1. 锁定订单 ---
        Backing backing = backingMapper.findByIdForUpdate(backingId);

        // 验证 1: 订单是否存在
        if (backing == null) {
            throw new RuntimeException("订单不存在");
        }
        // 验证 2: 订单所有权
        if (!Objects.equals(backing.getBackerId(), user.getId())) {
            throw new AccessDeniedException("无权操作此订单");
        }
        // 验证 3: 订单状态是否为 "待支付"
        if (!Objects.equals(backing.getStatus(), STATUS_PENDING)) {
            throw new RuntimeException("订单状态异常 (可能已支付或已取消)");
        }

        // --- 2. 锁定回报档位 (检查库存) ---
        ProjectReward reward = rewardMapper.findByIdForUpdate(backing.getRewardId());
        if (reward == null) {
            throw new RuntimeException("回报档位不存在");
        }

        // 验证 4: 库存 (如果 stock 不为 null)
        if (reward.getStock() != null) {
            if (reward.getStock() <= 0) {
                throw new RuntimeException("回报档位已售罄");
            }
            // 扣减库存
            reward.setStock(reward.getStock() - 1);
            rewardMapper.update(reward); // 更新库存
        }

        // --- 3. 锁定项目 (增加金额) ---
        Project project = projectMapper.findProjectByIdForUpdate(backing.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        // 验证 5: 再次确认项目状态 (防止在创建订单和支付之间, 项目状态被改变)
        if (!Objects.equals(project.getStatus(), PROJECT_STATUS_ACTIVE) ||
                LocalDateTime.now().isAfter(project.getEndTime())) {
            throw new RuntimeException("项目已结束或状态已改变, 支付失败");
        }

        // 核心操作 1: 增加项目当前金额
        project.setCurrentAmount(project.getCurrentAmount().add(backing.getBackingAmount()));
        projectMapper.updateProject(project); // (使用第五步的动态 update)

        // 核心操作 2: 更新订单状态
        backing.setStatus(STATUS_PAID); // 1: 已支付
        backingMapper.update(backing);

        return backing;
    }

    /**
     * 3. 获取我的订单列表
     */
    @Override
    public List<BackingDetailDTO> getMyBackings() {
        User user = getCurrentUser();
        return backingMapper.findBackingsByUserId(user.getId());
    }
}