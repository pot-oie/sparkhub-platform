package com.pot.sparkhub.service;

import com.pot.sparkhub.dto.CategoryDTO;
import com.pot.sparkhub.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();

    Category createCategory(CategoryDTO categoryDTO);
    Category updateCategory(Long id, CategoryDTO categoryDTO);
    void deleteCategory(Long id);
}