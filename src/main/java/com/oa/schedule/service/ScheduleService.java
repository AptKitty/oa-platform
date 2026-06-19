package com.oa.schedule.service;

import com.oa.schedule.dao.ScheduleDao;
import com.oa.schedule.entity.*;
import com.oa.common.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleService {

    public void addEvent(CalendarEvent e) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(ScheduleDao.class).insertEvent(e); }
    }

    public void updateEvent(CalendarEvent e) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(ScheduleDao.class).updateEvent(e); }
    }

    public void deleteEvent(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(ScheduleDao.class).deleteEvent(id); }
    }

    public List<CalendarEvent> getUserEvents(Long userId, LocalDateTime start, LocalDateTime end) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(ScheduleDao.class).findEventsByUser(userId, start, end);
        }
    }

    public void createMeeting(Meeting m, List<Long> participantIds) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            ScheduleDao dao = s.getMapper(ScheduleDao.class);
            dao.insertMeeting(m);
            for (Long uid : participantIds) dao.insertMeetingParticipant(m.getId(), uid);
        }
    }

    public List<Meeting> findMeetingsByTime(LocalDateTime start, LocalDateTime end) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(ScheduleDao.class).findMeetingsByTime(start, end);
        }
    }

    public void addTask(Task t) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(ScheduleDao.class).insertTask(t); }
    }

    public void updateTaskStatus(Long id, String status) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(ScheduleDao.class).updateTaskStatus(id, status); }
    }

    public void updateTask(Task t) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(ScheduleDao.class).updateTask(t); }
    }

    public List<Task> getTasksByAssignee(Long assigneeId, String status) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(ScheduleDao.class).findTasksByAssignee(assigneeId, status);
        }
    }
}