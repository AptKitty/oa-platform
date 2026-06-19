package com.oa.statistics.service;

import com.oa.statistics.dao.StatDao;
import com.oa.statistics.entity.StatResultVO;
import com.oa.common.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import java.util.List;
import java.util.Map;

public class StatService {

    public Map<String, Object> getOverview() {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(StatDao.class).getOverviewStats();
        }
    }

    public List<StatResultVO> getApprovalRanking(String month) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(StatDao.class).getApprovalEfficiencyRanking(month);
        }
    }

    public List<StatResultVO> getAttendanceComparison(int year, int month) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(StatDao.class).getDeptAttendanceComparison(year, month);
        }
    }

    public List<StatResultVO> getLeaveDistribution(int year, int month) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(StatDao.class).getLeaveTypeDistribution(year, month);
        }
    }

    public List<StatResultVO> getApprovalTrend(int year) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(StatDao.class).getMonthlyApprovalTrend(year);
        }
    }

    public List<StatResultVO> getDeptUserCount() {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(StatDao.class).getDeptUserCount();
        }
    }

    public Map<String, Object> getPendingTaskStats() {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(StatDao.class).getPendingTaskStats();
        }
    }
}