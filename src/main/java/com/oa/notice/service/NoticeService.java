package com.oa.notice.service;

import com.oa.notice.dao.NoticeDao;
import com.oa.notice.entity.Notice;
import com.oa.common.MyBatisUtil;
import com.oa.common.PageResult;
import org.apache.ibatis.session.SqlSession;
import java.util.List;

public class NoticeService {

    public void publish(Notice notice) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(NoticeDao.class).insert(notice); }
    }

    public void update(Notice notice) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(NoticeDao.class).update(notice); }
    }

    public void delete(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(NoticeDao.class).deleteById(id); }
    }

    public Notice findById(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(NoticeDao.class).findById(id); }
    }

    public PageResult<Notice> findByPage(String keyword, int page, int pageSize) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            NoticeDao dao = s.getMapper(NoticeDao.class);
            int offset = (page - 1) * pageSize;
            return new PageResult<>(dao.count(keyword), page, pageSize, dao.findAll(keyword, offset, pageSize));
        }
    }

    public void markRead(Long noticeId, Long userId) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(NoticeDao.class).insertReadRecord(noticeId, userId); }
    }

    public int getReadCount(Long noticeId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(NoticeDao.class).getReadCount(noticeId); }
    }

    public List<Long> getReadUserIds(Long noticeId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(NoticeDao.class).getReadUserIds(noticeId); }
    }

    public List<Notice> activateScheduledNotices() {
        try (SqlSession s = MyBatisUtil.openSession()) {
            List<Notice> notices = s.getMapper(NoticeDao.class).findScheduledToActivate();
            for (Notice n : notices) { n.setStatus(1); s.getMapper(NoticeDao.class).update(n); }
            return notices;
        }
    }

    public int getActiveUserCount() {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(NoticeDao.class).getActiveUserCount(); }
    }

    public List<java.util.Map<String, Object>> getReadUserNames(Long noticeId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(NoticeDao.class).getReadUserNames(noticeId); }
    }

    public List<java.util.Map<String, Object>> getUnreadUserNames(Long noticeId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(NoticeDao.class).getUnreadUserNames(noticeId); }
    }
}