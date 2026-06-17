package com.oa.system.dao;

import com.oa.system.entity.Role;
import java.util.List;

public interface RoleDao {
    Role findById(Long id);
    List<Role> findAll();
    List<Role> findByUserId(Long userId);
    int insert(Role role);
    int update(Role role);
    int deleteById(Long id);
    int insertUserRole(@org.apache.ibatis.annotations.Param("userId") Long userId,
                       @org.apache.ibatis.annotations.Param("roleId") Long roleId);
    int deleteUserRoles(Long userId);
    java.util.List<Long> findUserIdsByRoleCode(String roleCode);
}
