package com.oa.system.service;

import com.oa.system.dao.MenuDao;
import com.oa.system.entity.Menu;
import com.oa.common.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import java.util.List;

/**
 * 菜单权限服务 - 系统管理模块
 */
public class MenuService {

    public Menu findById(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(MenuDao.class).findById(id); }
    }

              /** 获取所有菜单（用于角色权限分配） */
    public List<Menu> findAll() {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(MenuDao.class).findAll(); }
    }

              /** 获取某角色拥有的菜单 */
    public List<Menu> findByRoleId(Long roleId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(MenuDao.class).findByRoleId(roleId); }
    }

       /** 获取某用户拥有的菜单（通过用户-角色-菜单关联） */
    public List<Menu> findByUserId(Long userId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(MenuDao.class).findByUserId(userId); }
    }

    public void add(Menu menu) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(MenuDao.class).insert(menu); }
    }

    public void update(Menu menu) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(MenuDao.class).update(menu); }
    }

    public void delete(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(MenuDao.class).deleteById(id); }
    }

              /** 给角色分配菜单权限 */
    public void assignMenus(Long roleId, List<Long> menuIds) {
        try (SqlSession s = MyBatisUtil.openSession(false)) {
            MenuDao dao = s.getMapper(MenuDao.class);
            dao.deleteRoleMenus(roleId);
            for (Long menuId : menuIds) dao.insertRoleMenu(roleId, menuId);
            s.commit();
        }
    }
}
