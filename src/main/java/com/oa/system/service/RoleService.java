package com.oa.system.service;

import com.oa.system.dao.RoleDao;
import com.oa.system.dao.MenuDao;
import com.oa.system.entity.*;
import com.oa.common.MyBatisUtil;
import java.util.List;

/**
 * 角色权限服务 - 系统管理模块
 * 模块负责人: 【组员A】
 */
public class RoleService {

  public List<Role> getAllRoles() { return MyBatisUtil.openSession().getMapper(RoleDao.class).findAll(); }
  public List<Role> getUserRoles(Long userId) { return MyBatisUtil.openSession().getMapper(RoleDao.class).findByUserId(userId); }
  public List<Menu> getUserMenus(Long userId) { return MyBatisUtil.openSession().getMapper(MenuDao.class).findByUserId(userId); }
  public void assignRoles(Long userId, List<Long> roleIds) {
    RoleDao dao = MyBatisUtil.openSession().getMapper(RoleDao.class);
    dao.deleteUserRoles(userId);
    for (Long rid : roleIds) dao.insertUserRole(userId, rid);
  }
}
