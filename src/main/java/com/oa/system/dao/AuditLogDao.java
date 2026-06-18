package com.oa.system.dao;

import com.oa.system.entity.AuditLog;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AuditLogDao {
    int insert(AuditLog log);
    List<AuditLog> findAll(@Param("offset") int offset, @Param("limit") int limit);
    long count();
}
