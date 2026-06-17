package com.oa.attendance.dao;

import com.oa.attendance.entity.ClockRecord;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AttendanceDao {
    int insertClockRecord(ClockRecord record);
    List<ClockRecord> findByUserIdAndDate(@Param("userId") Long userId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);
    @org.apache.ibatis.annotations.MapKey("date")
    List<Map<String, Object>> getMonthlyStats(@Param("year") int year, @Param("month") int month);
    long countTodayClock(@Param("userId") Long userId, @Param("start") LocalDateTime start);
}
