package com.oa.attendance.dao;

import com.oa.attendance.entity.LeaveRequest;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface LeaveDao {
    int insert(LeaveRequest request);
    LeaveRequest findById(Long id);
    List<LeaveRequest> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    long countByUserId(Long userId);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
