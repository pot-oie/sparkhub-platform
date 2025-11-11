package com.pot.sparkhub.mapper;

import com.pot.sparkhub.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category ORDER BY id")
    List<Category> findAll();

    @Insert("INSERT INTO category (name) VALUES (#{name})")
    void insert(Category category);

    @Update("UPDATE category SET name = #{name} WHERE id = #{id}")
    void update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(@Param("id") Long id);
}