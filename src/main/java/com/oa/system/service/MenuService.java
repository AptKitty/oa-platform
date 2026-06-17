package com.oa.system.service;

import com.oa.system.dao.MenuDao;
import com.oa.system.entity.Menu;
import com.oa.common.MyBatisUtil;
import java.util.List;

/**
 * 菜单权限服务 - 系统管理模块
 */
public class MenuService {

    private MenuDao getDao() {
        return MyBatisUtil.openSession().getMapper(MenuDao.class);
    }

    public Menu findById(Long id) {
        return getDao().findById(id);
    }

    /** 获取所有菜单（用于角色权限分配） */
    public List<Menu> findAll() {
        return getDao().findAll();
    }

    /** 获取某角色拥有的菜单 */
    public List<Menu> findByRoleId(Long roleId) {
        return getDao().findByRoleId(roleId);
    }

    /** 获取某用户拥有的菜单（通过用户→角色→菜单关联） */
    public List<Menu> findByUserId(Long userId) {
        return getDao().findByUserId(userId);
    }

    public void add(Menu menu) {
        getDao().insert(menu);
    }

    public void update(Menu menu) {
        getDao().update(menu);
    }

    public void delete(Long id) {
        getDao().deleteById(id);
    }

    /** 给角色分配菜单权限 */
    public void assignMenus(Long roleId, List<Long> menuIds) {
        MenuDao dao = getDao();
        dao.deleteRoleMenus(roleId);
        for (Long menuId : menuIds) {
            dao.insertRoleMenu(roleId, menuId);
        }
    }
}
