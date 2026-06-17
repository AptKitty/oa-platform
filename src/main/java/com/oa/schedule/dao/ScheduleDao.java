package com.oa.schedule.dao;

import com.oa.schedule.entity.CalendarEvent;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleDao {
    int insertEvent(CalendarEvent event);
    int updateEvent(CalendarEvent event);
    int deleteEvent(Long id);
    CalendarEvent findEventById(Long id);
    List<CalendarEvent> findEventsByUser(@Param("userId") Long userId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
    int insertMeeting(com.oa.schedule.entity.Meeting meeting);
    int insertMeetingParticipant(@Param("meetingId") Long meetingId, @Param("userId") Long userId);
    List<com.oa.schedule.entity.Meeting> findMeetingsByTime(@Param("start") LocalDateTime start,
                                                              @Param("end") LocalDateTime end);
    List<com.oa.schedule.entity.Task> findTasksByAssignee(@Param("assigneeId") Long assigneeId,
                                                            @Param("status") String status);
    int insertTask(com.oa.schedule.entity.Task task);
    int updateTaskStatus(@Param("id") Long id, @Param("status") String status);
}
