package com.pot.sparkhub.controller;

import com.pot.sparkhub.common.Result;
import com.pot.sparkhub.dto.BackingCreateDTO;
import com.pot.sparkhub.dto.BackingDetailDTO;
import com.pot.sparkhub.entity.Backing;
import com.pot.sparkhub.service.BackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backings")
@PreAuthorize("hasRole('ROLE_USER')") // 整个模块都需要 "ROLE_USER" 权限
public class BackingController {

    @Autowired
    private BackingService backingService;

    /**
     * 1. POST /api/backings
     * 创建支持订单 (状态: 待支付)
     */
    @PostMapping
    public Result<Backing> createBacking(@RequestBody BackingCreateDTO createDTO) {
        try {
            Backing newBacking = backingService.createBacking(createDTO);
            return Result.success(newBacking);
        } catch (Exception e) {
            return Result.error(400, "订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 2. POST /api/backings/{id}/pay
     * 模拟支付 (核心事务)
     */
    @PostMapping("/{id}/pay")
    public Result<Backing> payForBacking(@PathVariable Long id) {
        try {
            // 事务和安全检查都在 Service 层完成
            Backing paidBacking = backingService.executePayment(id);
            return Result.success(paidBacking);
        } catch (AccessDeniedException e) {
            return Result.error(403, "权限不足: " + e.getMessage());
        } catch (RuntimeException e) {
            // 400 Bad Request (例如: 已售罄, 已过期, 状态错误)
            return Result.error(400, "支付失败: " + e.getMessage());
        }
    }

    /**
     * 3. GET /api/backings/my
     * 查看我的支持订单列表
     */
    @GetMapping("/my")
    public Result<List<BackingDetailDTO>> getMyBackings() {
        List<BackingDetailDTO> backings = backingService.getMyBackings();
        return Result.success(backings);
    }
}