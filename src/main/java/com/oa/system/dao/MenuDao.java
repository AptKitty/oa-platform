package com.oa.system.dao;

import com.oa.system.entity.Menu;
import java.util.List;

public interface MenuDao {
    Menu findById(Long id);
    List<Menu> findAll();
    List<Menu> findByRoleId(Long roleId);
    List<Menu> findByUserId(Long userId);
    int insert(Menu menu);
    int update(Menu menu);
    int deleteById(Long id);
    int insertRoleMenu(@org.apache.ibatis.annotations.Param("roleId") Long roleId,
                       @org.apache.ibatis.annotations.Param("menuId") Long menuId);
    int deleteRoleMenus(Long roleId);
}
