package com.oa.attendance.service;

import com.oa.attendance.dao.AttendanceDao;
import com.oa.attendance.dao.LeaveDao;
import com.oa.attendance.entity.*;
import com.oa.common.MyBatisUtil;
import com.oa.common.BusinessException;
import java.time.LocalDateTime;

/**
 * 考勤服务 - 考勤管理模块
 * 模块负责人: 【组员C】
 */
public class AttendanceService {

    public void clockIn(Long userId, String ipAddress) {
        // TODO: 检查是否重复打卡
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        AttendanceDao dao = MyBatisUtil.openSession().getMapper(AttendanceDao.class);
        long count = dao.countTodayClock(userId, todayStart);
        if (count >= 2) throw new BusinessException("今日已打完卡");
        ClockRecord record = new ClockRecord();
        record.setUserId(userId);
        record.setClockType(count == 0 ? "IN" : "OUT");
        record.setClockTime(LocalDateTime.now());
        record.setIpAddress(ipAddress);
        dao.insertClockRecord(record);
    }

    public void applyLeave(LeaveRequest request) {
        // TODO: 校验请假额度、时间冲突，然后关联审批流
        MyBatisUtil.openSession().getMapper(LeaveDao.class).insert(request);
    }
}
