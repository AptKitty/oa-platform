package com.oa.attendance.dao;

import com.oa.attendance.entity.LeaveRequest;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 请假 DAO 接口
 * 对应 Mapper: mapper/attendance/LeaveMapper.xml
 * 组员3 负责
 */

public interface LeaveDao {

// ======== 原有方法：请假申请 CRUD ========

    int insert(LeaveRequest request);
    
    LeaveRequest findById(Long id);
    
    List<LeaveRequest> findByUserId(
        @Param("userId") Long userId, 
        @Param("offset") int offset, 
        @Param("limit") int limit);
    
    long countByUserId(Long userId);
    
    int updateStatus(
        @Param("id") Long id, 
        @Param("status") String status);

    // ======== 请假额度 ========

    // ======== 新增：请假额度管理（第一周补充）

/**
     * 查询某用户某年所有假期类型的额度
     * @return 每行包含 leave_type, total_days, used_days, remaining_days
     */
    List<java.util.Map<String, Object>> getQuota(
        @Param("userId") Long userId, 
        @Param("year") int year);

 /**
     * 按假期类型精确查询额度
     * @return 包含 total_days, used_days, remaining_days
     */
    java.util.Map<String, Object> getQuotaByType(
        @Param("userId") Long userId, 
        @Param("year") int year, 
        @Param("leaveType") String leaveType);

    /**
     * 扣减请假额度（审批通过后调用）
     * @return 影响行数，0=额度不足扣减失败
     */
    int deductQuota(
        @Param("userId") Long userId, 
        @Param("year") int year, 
        @Param("leaveType") String leaveType, 
        @Param("days") double days);
}
