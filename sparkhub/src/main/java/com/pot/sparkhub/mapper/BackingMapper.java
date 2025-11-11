package com.pot.sparkhub.mapper;

import com.pot.sparkhub.dto.BackingDetailDTO;
import com.pot.sparkhub.entity.Backing;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BackingMapper {

    /**
     * 1. 插入新订单 (在 XML 中实现)
     * (需要回填主键 ID)
     */
    void insert(Backing backing);

    /**
     * 2. 根据ID查询订单 (用于支付)
     */
    @Select("SELECT * FROM backing WHERE id = #{id}")
    Backing findById(@Param("id") Long id);

    /**
     * 3. [核心] 根据ID查询订单 (带悲观写锁)
     * (在 XML 中实现)
     */
    Backing findByIdForUpdate(@Param("id") Long id);

    /**
     * 4. 更新订单 (在 XML 中实现)
     * (用于更新状态)
     */
    void update(Backing backing);

    /**
     * 5. 查询我的订单列表 (在 XML 中实现)
     * (JOIN project 和 project_reward)
     */
    List<BackingDetailDTO> findBackingsByUserId(@Param("userId") Long userId);
}