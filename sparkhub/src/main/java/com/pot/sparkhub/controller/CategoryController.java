package com.pot.sparkhub.controller;

import com.pot.sparkhub.common.Result;
import com.pot.sparkhub.dto.CategoryDTO;
import com.pot.sparkhub.entity.Category;
import com.pot.sparkhub.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * GET /api/categories
     * (公开接口, 无需权限)
     */
    @GetMapping
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * POST /api/categories
     * (Admin 权限)
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Category> createCategory(@RequestBody CategoryDTO categoryDTO) {
        try {
            Category newCategory = categoryService.createCategory(categoryDTO);
            return Result.success(newCategory);
        } catch (Exception e) {
            return Result.error(400, "创建失败: " + e.getMessage());
        }
    }

    /**
     * PUT /api/categories/{id}
     * (Admin 权限)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Category> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        try {
            Category updatedCategory = categoryService.updateCategory(id, categoryDTO);
            return Result.success(updatedCategory);
        } catch (Exception e) {
            return Result.error(400, "更新失败: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/categories/{id}
     * (Admin 权限)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, "删除失败: " + e.getMessage());
        }
    }
}