package com.oa.system.service;

import com.oa.system.dao.UserDao;
import com.oa.system.entity.User;
import com.oa.common.MyBatisUtil;
import com.oa.common.PageResult;
import org.apache.ibatis.session.SqlSession;
import java.util.List;

/**
 * 用户服务 - 系统管理模块
 * 模块负责人: 【组员A】
 */
public class UserService {

    public User login(String username, String password) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            User user = s.getMapper(UserDao.class).findByUsername(username);
            if (user == null || !user.getPassword().equals(com.oa.common.MD5Util.md5(password))) {
                throw new com.oa.common.BusinessException("用户名或密码错误");
            }
            if (user.getStatus() == 0) {
                throw new com.oa.common.BusinessException("账号已被禁用");
            }
            return user;
        }
    }

    public User findById(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(UserDao.class).findById(id); }
    }

    public PageResult<User> findByPage(String keyword, Long deptId, Integer status, int page, int pageSize) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            UserDao dao = s.getMapper(UserDao.class);
            int offset = (page - 1) * pageSize;
            List<User> rows = dao.findByCondition(keyword, deptId, status, offset, pageSize);
            long total = dao.countByCondition(keyword, deptId, status);
            return new PageResult<>(total, page, pageSize, rows);
        }
    }

    public void add(User user) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(UserDao.class).insert(user); }
    }
    public void update(User user) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(UserDao.class).update(user); }
    }
    public void delete(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(UserDao.class).deleteById(id); }
    }
    public void resetPassword(Long id, String newPwd) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(UserDao.class).updatePassword(id, newPwd); }
    }
}
