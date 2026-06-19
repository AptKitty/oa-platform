package com.oa.notice.service;

import com.oa.notice.dao.MessageDao;
import com.oa.notice.entity.Message;
import com.oa.common.MyBatisUtil;
import com.oa.common.PageResult;
import org.apache.ibatis.session.SqlSession;
import java.util.List;

public class MessageService {

    public void send(Message msg) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(MessageDao.class).insert(msg); }
    }

    public void markRead(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(MessageDao.class).markAsRead(id); }
    }

    public void markAllRead(Long userId) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(MessageDao.class).markAllAsRead(userId); }
    }

    public PageResult<Message> findByReceiver(Long receiverId, Integer isRead, int page, int pageSize) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            MessageDao dao = s.getMapper(MessageDao.class);
            int offset = (page - 1) * pageSize;
            return new PageResult<>(dao.countByReceiverId(receiverId, isRead), page, pageSize,
                    dao.findByReceiverId(receiverId, isRead, offset, pageSize));
        }
    }

    public int getUnreadCount(Long userId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(MessageDao.class).getUnreadCount(userId); }
    }
}