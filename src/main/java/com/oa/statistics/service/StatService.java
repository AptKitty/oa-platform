package com.oa.statistics.service;

import com.oa.statistics.dao.StatDao;
import com.oa.statistics.entity.StatResultVO;
import com.oa.common.MyBatisUtil;
import java.util.List;
import java.util.Map;

public class StatService {

    private StatDao getDao() { return MyBatisUtil.openSession(true).getMapper(StatDao.class); }

    public Map<String, Object> getOverview() { return getDao().getOverviewStats(); }

    public List<StatResultVO> getApprovalRanking(String month) { return getDao().getApprovalEfficiencyRanking(month); }

    public List<StatResultVO> getAttendanceComparison(int year, int month) {
        return getDao().getDeptAttendanceComparison(year, month);
    }

    public List<StatResultVO> getLeaveDistribution(int year, int month) {
        return getDao().getLeaveTypeDistribution(year, month);
    }

    public List<StatResultVO> getApprovalTrend(int year) { return getDao().getMonthlyApprovalTrend(year); }

    public List<StatResultVO> getDeptUserCount() { return getDao().getDeptUserCount(); }
}