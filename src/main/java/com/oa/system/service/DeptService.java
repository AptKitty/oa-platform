package com.oa.system.service;

import com.oa.system.dao.DeptDao;
import com.oa.system.entity.Dept;
import com.oa.common.MyBatisUtil;
import java.util.List;

/**
 * 部门服务 - 系统管理模块
 */
public class DeptService {

    private DeptDao getDao() {
        return MyBatisUtil.openSession().getMapper(DeptDao.class);
    }

    public Dept findById(Long id) {
        return getDao().findById(id);
    }

    /** 获取所有部门（平铺列表） */
    public List<Dept> findAll() {
        return getDao().findAll();
    }

    /** 获取指定部门的直接子部门 */
    public List<Dept> findByParentId(Long parentId) {
        return getDao().findByParentId(parentId);
    }

    public void add(Dept dept) {
        getDao().insert(dept);
    }

    public void update(Dept dept) {
        getDao().update(dept);
    }

    public void delete(Long id) {
        // 有子部门时不允许删除
        if (getDao().countByParentId(id) > 0) {
            throw new com.oa.common.BusinessException("该部门下存在子部门，无法删除");
        }
        getDao().deleteById(id);
    }
}
