package com.oa.system.service;

import com.oa.system.dao.RoleDao;
import com.oa.system.dao.MenuDao;
import com.oa.system.entity.*;
import com.oa.common.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import java.util.List;

/**
 * 角色权限服务 - 系统管理模块
 * 模块负责人: 【组员A】
 */
public class RoleService {

  public List<Role> getAllRoles() {
    try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(RoleDao.class).findAll(); }
  }
  public List<Role> getUserRoles(Long userId) {
    try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(RoleDao.class).findByUserId(userId); }
  }
  public List<Menu> getUserMenus(Long userId) {
    try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(MenuDao.class).findByUserId(userId); }
  }
  public java.util.Set<Long> findRoleIdsByUserId(Long userId) {
    try (SqlSession s = MyBatisUtil.openSession()) {
      return new java.util.HashSet<>(s.getMapper(RoleDao.class).findRoleIdsByUserId(userId));
    }
  }
  public void assignRoles(Long userId, List<Long> roleIds) {
    try (SqlSession s = MyBatisUtil.openSession(false)) {
      RoleDao dao = s.getMapper(RoleDao.class);
      dao.deleteUserRoles(userId);
      for (Long rid : roleIds) dao.insertUserRole(userId, rid);
      s.commit();
    }
  }
}
