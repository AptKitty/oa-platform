package com.oa.statistics.dao;

import com.oa.statistics.entity.StatResultVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface StatDao {
    Map<String, Object> getOverviewStats();
    List<StatResultVO> getApprovalEfficiencyRanking(@Param("month") String month);
    List<StatResultVO> getDeptAttendanceComparison(@Param("year") int year, @Param("month") int month);
    List<StatResultVO> getLeaveTypeDistribution(@Param("year") int year, @Param("month") int month);
    List<StatResultVO> getMonthlyApprovalTrend(@Param("year") int year);
    List<StatResultVO> getDeptUserCount();
}