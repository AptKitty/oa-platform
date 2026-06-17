package com.oa.system.service;

import com.oa.system.dao.UserDao;
import com.oa.system.entity.User;
import com.oa.common.MyBatisUtil;
import com.oa.common.PageResult;
import java.util.List;

/**
 * 用户服务 - 系统管理模块
 * 模块负责人: 【组员A】
 */
public class UserService {

    private UserDao getDao() {
        return MyBatisUtil.openSession().getMapper(UserDao.class);
    }

    public User login(String username, String password) {
        // TODO: MD5加密比对
        User user = getDao().findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new com.oa.common.BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new com.oa.common.BusinessException("账号已被禁用");
        }
        return user;
    }

    public User findById(Long id) { return getDao().findById(id); }

    public PageResult<User> findByPage(String keyword, Long deptId, Integer status, int page, int pageSize) {
        UserDao dao = getDao();
        int offset = (page - 1) * pageSize;
        List<User> rows = dao.findByCondition(keyword, deptId, status, offset, pageSize);
        long total = dao.countByCondition(keyword, deptId, status);
        return new PageResult<>(total, page, pageSize, rows);
    }

    public void add(User user) { getDao().insert(user); }
    public void update(User user) { getDao().update(user); }
    public void delete(Long id) { getDao().deleteById(id); }
    public void resetPassword(Long id, String newPwd) { getDao().updatePassword(id, newPwd); }
}
