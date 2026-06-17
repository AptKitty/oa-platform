package com.oa.schedule.service;

import com.oa.schedule.dao.ScheduleDao;
import com.oa.schedule.entity.*;
import com.oa.common.MyBatisUtil;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleService {

    private ScheduleDao getDao() { return MyBatisUtil.openSession().getMapper(ScheduleDao.class); }

    public void addEvent(CalendarEvent e) { getDao().insertEvent(e); }
    public void updateEvent(CalendarEvent e) { getDao().updateEvent(e); }
    public void deleteEvent(Long id) { getDao().deleteEvent(id); }
    public List<CalendarEvent> getUserEvents(Long userId, LocalDateTime start, LocalDateTime end) {
        return getDao().findEventsByUser(userId, start, end);
    }

    public void createMeeting(Meeting m, List<Long> participantIds) {
        ScheduleDao dao = getDao();
        dao.insertMeeting(m);
        for (Long uid : participantIds) dao.insertMeetingParticipant(m.getId(), uid);
    }

    public List<Meeting> findMeetingsByTime(LocalDateTime start, LocalDateTime end) {
        return getDao().findMeetingsByTime(start, end);
    }

    public void addTask(Task t) { getDao().insertTask(t); }
    public void updateTaskStatus(Long id, String status) { getDao().updateTaskStatus(id, status); }
    public List<Task> getTasksByAssignee(Long assigneeId, String status) {
        return getDao().findTasksByAssignee(assigneeId, status);
    }
}