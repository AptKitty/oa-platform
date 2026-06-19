package com.oa.system.service;

import com.oa.system.dao.DeptDao;
import com.oa.system.entity.Dept;
import com.oa.common.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import java.util.List;

/**
 * 部门服务 - 系统管理模块
 */
public class DeptService {

    public Dept findById(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(DeptDao.class).findById(id); }
    }

             /** 获取所有部门（平铺列表） */
    public List<Dept> findAll() {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(DeptDao.class).findAll(); }
    }

             /** 获取所有部门（平铺列表） */
    public List<Dept> findByParentId(Long parentId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(DeptDao.class).findByParentId(parentId); }
    }

    public void add(Dept dept) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(DeptDao.class).insert(dept); }
    }

    public void update(Dept dept) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(DeptDao.class).update(dept); }
    }

    public void delete(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            DeptDao dao = s.getMapper(DeptDao.class);
            if (dao.countByParentId(id) > 0) {
                throw new com.oa.common.BusinessException("该部门下存在子部门，无法删除");
            }
            dao.deleteById(id);
        }
    }
}
