package com.oa.notice.service;

import com.oa.notice.dao.MessageDao;
import com.oa.notice.entity.Message;
import com.oa.common.MyBatisUtil;
import com.oa.common.PageResult;
import java.util.List;

public class MessageService {

    private MessageDao getDao() { return MyBatisUtil.openSession().getMapper(MessageDao.class); }

    public void send(Message msg) { getDao().insert(msg); }

    public void markRead(Long id) { getDao().markAsRead(id); }

    public void markAllRead(Long userId) { getDao().markAllAsRead(userId); }

    public PageResult<Message> findByReceiver(Long receiverId, Integer isRead, int page, int pageSize) {
        MessageDao dao = getDao();
        int offset = (page - 1) * pageSize;
        List<Message> rows = dao.findByReceiverId(receiverId, isRead, offset, pageSize);
        long total = dao.countByReceiverId(receiverId, isRead);
        return new PageResult<>(total, page, pageSize, rows);
    }

    public int getUnreadCount(Long userId) { return getDao().getUnreadCount(userId); }
}