package com.oa.attendance.service;

import com.oa.attendance.dao.AttendanceDao;
import com.oa.attendance.dao.LeaveDao;
import com.oa.attendance.entity.*;
import com.oa.common.MyBatisUtil;
import com.oa.common.BusinessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 考勤服务 - 打卡 + 请假业务逻辑
 * 模块负责人: 【组员3】
 * 设计说明：
 *  1.打卡分为两层：clockIn()，底层写入；checkIn() 前端入口带防重复+返回中文结果
 *  2.请假申请前校验额度+时间冲突，审批通过后调用deducLeaveQuota() 扣减
 */
public class AttendanceService {

    // 标准上下班时间（可后移到 Constants）
    private static final LocalTime STANDARD_START = LocalTime.of(9, 0);
    private static final LocalTime STANDARD_END   = LocalTime.of(18, 0);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ==================== 打卡相关 ==================== //

    /**
     * 底层打卡 — 写入一条打卡记录（不做防打卡重复判断）
     * 
     * @param userId    用户ID
     * @param clockType "IN" 上班 / "OUT" 下班
     * @param clockTime 打卡时间
     * @param ipAddress 客户端IP
     */
    public void clockIn(Long userId, String clockType, LocalDateTime clockTime, String ipAddress) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        AttendanceDao dao = MyBatisUtil.openSession().getMapper(AttendanceDao.class);
        
        //防重复，每天最多打卡两次
        long count = dao.countTodayClock(userId, todayStart);
        if (count >= 2) {
            throw new BusinessException("今日已打完卡");
            }
        ClockRecord record = new ClockRecord();
        record.setUserId(userId);
        record.setClockType(clockType);
        record.setClockTime(clockTime);
        record.setIpAddress(ipAddress);
        dao.insertClockRecord(record);
    }

    /**
     * 前端打卡入口：带完整的防重复逻辑，返回中文结果给 UI 展示
     *
     * @param userId    当前登录用户ID
     * @param clockType "上班" 或 "下班"（中文，来自 ClockPanel 按钮）
     * @param clockTime 打卡时间
     * @return 如 "成功" / "成功（迟到 15 分钟）" / "失败：今天已打过上班卡"
     */
    public String checkIn(Long userId, String clockType, LocalDateTime clockTime) {
        String typeCode = "上班".equals(clockType) ? "IN" : "OUT";
        
        try {
            // 第一层防重复：上班只能打一次 IN，下班只能打一次 OUT
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            AttendanceDao dao = MyBatisUtil.openSession().getMapper(AttendanceDao.class);
            List<ClockRecord> todayRecords = 
                    dao.findByUserIdAndDate(userId, todayStart,todayStart.plusDays(1));

            boolean hasIn  = todayRecords.stream().anyMatch(r -> "IN".equals(r.getClockType()));
            boolean hasOut = todayRecords.stream().anyMatch(r -> "OUT".equals(r.getClockType()));

            if ("IN".equals(typeCode) && hasIn)   return "失败：今天已打过上班卡";
            if ("OUT".equals(typeCode) && hasOut) return "失败：今天已打过下班卡";
            if ("OUT".equals(typeCode) && !hasIn) return "失败：请先打上班卡";

            //第二层防重复：通过底层方法写入（内部还会再校验一次）
            clockIn(userId, typeCode, clockTime, "127.0.0.1");

            // 迟到/早退判定
            if ("IN".equals(typeCode)) {
                long lateMin = java.time.Duration.
                        between(STANDARD_START, clockTime.toLocalTime()).toMinutes();
                return lateMin > 0 ? "成功（迟到 " + lateMin + " 分钟）" : "成功";
            } else {
                long earlyMin = java.time.Duration.
                        between(clockTime.toLocalTime(),STANDARD_END).toMinutes();
                return earlyMin > 0 ? "成功（早退 " + earlyMin + " 分钟）" : "成功";
            }
        } catch (BusinessException e) {
            return "失败：" + e.getMessage();
        }
    }

    /**
     * 查询今日打卡状态，返回给 ClockPanel 控制按钮可用性
     *
     * @return null     = 今日未打卡
     *         非null  = Map{"checkInTime": LocalTime, "checkOutTime": LocalTime}
     *                   其中 checkOutTime 可能为 null（只打了上班卡）
     */
    public Map<String, Object> getTodayRecord(Long userId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        AttendanceDao dao = MyBatisUtil.openSession().getMapper(AttendanceDao.class);
        List<ClockRecord> records =
                dao.findByUserIdAndDate(userId, todayStart, todayStart.plusDays(1));

        if (records.isEmpty()) return null;

        Map<String, Object> result = new HashMap<>();
        for (ClockRecord r : records) {
            if ("IN".equals(r.getClockType()))  {
                result.put("checkInTime",r.getClockTime().toLocalTime());
                }
            if ("OUT".equals(r.getClockType())) {
                result.put("checkOutTime",r.getClockTime().toLocalTime());
                }
        }
        return result;
    }

    /**
     * 最近 N 天打卡汇总，每天一行，用于打卡记录表格
     *
     * @return 每行: {attendanceDate, checkInTime, checkOutTime, status}
     *         status: "正常" / "迟到" / "早退" / "缺勤"
     */
    public List<Map<String, Object>> getRecentRecords(Long userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(days - 1).atStartOfDay();
        LocalDateTime end   = today.plusDays(1).atStartOfDay();

        AttendanceDao dao = MyBatisUtil.openSession().getMapper(AttendanceDao.class);
        List<ClockRecord> records = dao.findByUserIdAndDate(userId, start, end);

        // 初始化 N 天的空行（保证无打卡的日期也显示）
        Map<LocalDate, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Map<String, Object> row = new HashMap<>();
            row.put("attendanceDate", d);
            row.put("checkInTime", null);
            row.put("checkOutTime", null);
            row.put("status", "缺勤");  //默认缺勤
            grouped.put(d, row);
        }
        
        //填入实际打卡数据
        for (ClockRecord r : records) {
            LocalDate d = r.getClockTime().toLocalDate();
            Map<String, Object> row = grouped.get(d);
            if (row == null) continue;

            if ("IN".equals(r.getClockType())) {
                row.put("checkInTime", r.getClockTime().toLocalTime().format(TIME_FMT));
                LocalTime t = r.getClockTime().toLocalTime();
                row.put("status", t.isAfter(STANDARD_START) ? "迟到" : "正常");
            }

            if ("OUT".equals(r.getClockType())) {
                row.put("checkOutTime", r.getClockTime().toLocalTime().format(TIME_FMT));
                LocalTime t = r.getClockTime().toLocalTime();
               if (!"迟到".equals(row.get("status"))) {
                    row.put("status", t.isBefore(STANDARD_END) ? "早退" : "正常");
                }
            }
        }

        return new ArrayList<>(grouped.values());
    }

    /**
     * 月度统计（DAO 透传）
     */
    public List<Map<String, Object>> getMonthlyStats(int year, int month) {
        AttendanceDao dao = MyBatisUtil.openSession().getMapper(AttendanceDao.class);
        return dao.getMonthlyStats(year, month);
    }

    // ==================== 请假相关 ==================== //

    /**
     * 申请请假 — 校验额度 + 时间冲突，然后写入数据库
     *
     * @throws BusinessException 额度不足 / 时间冲突
     */
    public void applyLeave(LeaveRequest request) {
        LeaveDao dao = MyBatisUtil.openSession().getMapper(LeaveDao.class);

        // 校验请假额度
        int year = request.getStartTime().getYear();
        Map<String, Object> quota =
                dao.getQuotaByType(request.getUserId(), year, request.getLeaveType());

        if (quota == null || quota.isEmpty()) {
            throw new BusinessException("该假期类型暂无额度，请联系管理员初始化");
        }

        double remaining = ((Number) quota.get("remaining_days")).doubleValue();
        if (request.getDuration() > remaining) {
            throw new BusinessException(
                    "请假天数超出剩余额度（剩余 " + String.format("%.1f", remaining) + " 天）");
        }
        
        // 时间冲突检测：与已有有效请假不能重叠）
        List<LeaveRequest> existing = dao.findByUserId(request.getUserId(), 0, 1000);
        for (LeaveRequest lr : existing) {
            // 只检查未驳回/未取消的
            if ("REJECTED".equals(lr.getStatus()) || "CANCELLED".equals(lr.getStatus())) {
                continue;
            }
            boolean overlap = request.getStartTime().isBefore(lr.getEndTime())
                           && request.getEndTime().isAfter(lr.getStartTime());
            if (overlap) {
                throw new BusinessException("与已有的请假申请时间冲突（"
                        + lr.getStartTime().toLocalDate() + " ~ "
                        + lr.getEndTime().toLocalDate() + "）");
            }
        }

        // ③ 写入
        if (request.getStatus() == null) {
            request.setStatus("PENDING");
        }
        dao.insert(request);
    }

    /**
     * 查询用户当前年份所有类型的请假额度
     * @return 每行: {leave_type, total_days, used_days, remaining_days}
     */
    public List<Map<String, Object>> getLeaveQuota(Long userId, int year) {
        return MyBatisUtil.openSession().getMapper(LeaveDao.class).getQuota(userId, year);
    }

    /**
     * 扣减请假额度 — 审批通过后由组员2的 WorkflowService 回调
     * @return true=扣减成功, false=额度不足
     */
    public boolean deductLeaveQuota(Long userId, String leaveType, double days) {
        int year = LocalDate.now().getYear();
        int affected = MyBatisUtil.openSession().getMapper(LeaveDao.class)
                .deductQuota(userId, year, leaveType, days);
        return affected > 0;
    }

    /**
     * 分页查询用户请假历史
     */
    public List<LeaveRequest> getLeaveHistory(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return MyBatisUtil.openSession().getMapper(LeaveDao.class)
                .findByUserId(userId, offset, pageSize);
    }

    /**
     * 统计用户请假总数
     */
    public long countLeaveHistory(Long userId) {
        return MyBatisUtil.openSession().getMapper(LeaveDao.class).countByUserId(userId);
    }
}