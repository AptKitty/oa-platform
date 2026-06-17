package com.oa.system.dao;

import com.oa.system.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户DAO接口
 */
public interface UserDao {

    User findById(Long id);

    User findByUsername(String username);

    List<User> findByCondition(@Param("keyword") String keyword,
                               @Param("deptId") Long deptId,
                               @Param("status") Integer status,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    long countByCondition(@Param("keyword") String keyword,
                          @Param("deptId") Long deptId,
                          @Param("status") Integer status);

    int insert(User user);

    int update(User user);

    int deleteById(Long id);

    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
