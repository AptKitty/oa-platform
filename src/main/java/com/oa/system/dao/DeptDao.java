package com.oa.system.dao;

import com.oa.system.entity.Dept;
import java.util.List;

public interface DeptDao {
    Dept findById(Long id);
    List<Dept> findAll();
    List<Dept> findByParentId(Long parentId);
    int insert(Dept dept);
    int update(Dept dept);
    int deleteById(Long id);
    long countByParentId(Long parentId);
}
