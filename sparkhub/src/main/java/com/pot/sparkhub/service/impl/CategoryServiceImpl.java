package com.pot.sparkhub.service.impl;

import com.pot.sparkhub.dto.CategoryDTO;
import com.pot.sparkhub.entity.Category;
import com.pot.sparkhub.mapper.CategoryMapper;
import com.pot.sparkhub.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * @Cacheable:
     * value = "categories": 缓存的分组名 (对应你的设计 categories::all)。
     * key = "'all'": 缓存的 Key。
     * 效果: 第一次调用时, 会执行方法并查询数据库,
     * 然后将结果存入 Redis (Key: "sparkhub:cache:categories::all")。
     * 后续所有对该方法的调用 (只要 Key 相同) 都会直接从 Redis 读取,
     * 不再执行方法体, 也不再查询 MySQL。
     */
    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> getAllCategories() {
        // 这句日志只会在第一次调用时打印
        System.out.println("Executing: CategoryServiceImpl.getAllCategories() - Querying DB");
        return categoryMapper.findAll();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 权限
    @CacheEvict(value = "categories", allEntries = true) // 清除所有分类缓存
    public Category createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        categoryMapper.insert(category);
        return category;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 权限
    @CacheEvict(value = "categories", allEntries = true) // 清除所有分类缓存
    public Category updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        category.setName(categoryDTO.getName());
        categoryMapper.update(category);
        return category;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 权限
    @CacheEvict(value = "categories", allEntries = true) // 清除所有分类缓存
    public void deleteCategory(Long id) {
        // (实际项目中, 你需要检查该分类是否被项目引用)
        categoryMapper.deleteById(id);
    }
}