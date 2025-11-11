package com.pot.sparkhub.mapper;

import com.pot.sparkhub.entity.Role;
import com.pot.sparkhub.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户 (包含角色信息)
     * (这个复杂查询我们在 XML 中实现)
     */
    User findByUsername(@Param("username") String username);

    /**
     * 动态更新用户信息 (只更新非 null 字段)
     */
    void updateUser(User user);

    /**
     * 根据邮箱查询用户 (用于注册检查)
     */
    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(@Param("email") String email);

    /**
     * 根据 ID 查询用户
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    /**
     * 插入新用户
     * (这个也在XML中实现, 以便获取返回的主键id)
     */
    void insert(User user);

    /**
     * 插入用户和角色的关联
     */
    @Insert("INSERT INTO user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 移除用户的特定角色
     */
    @Delete("DELETE FROM user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    void deleteUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * (辅助) 根据角色名(e.g., "ROLE_USER")查找角色ID
     */
    @Select("SELECT id FROM role WHERE name = #{name}")
    Long findRoleIdByName(@Param("name") String name);

    /**
     * [Admin] 查询所有用户 (带角色), 在 XML 中实现
     */
    List<User> findAllWithRoles();

    /**
     * (嵌套查询) 根据用户ID查找其所有角色
     */
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 头像更新
     */
    @Update("UPDATE user SET avatar = #{avatarUrl} WHERE id = #{userId}")
    void updateAvatar(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);
}