package com.oa.notice.service;

import com.oa.notice.dao.NoticeDao;
import com.oa.notice.entity.Notice;
import com.oa.common.MyBatisUtil;
import com.oa.common.PageResult;
import java.util.List;

public class NoticeService {

    private NoticeDao getDao() { return MyBatisUtil.openSession().getMapper(NoticeDao.class); }

    public void publish(Notice notice) { getDao().insert(notice); }

    public void update(Notice notice) { getDao().update(notice); }

    public void delete(Long id) { getDao().deleteById(id); }

    public Notice findById(Long id) { return getDao().findById(id); }

    public PageResult<Notice> findByPage(String keyword, int page, int pageSize) {
        NoticeDao dao = getDao();
        int offset = (page - 1) * pageSize;
        return new PageResult<>(dao.count(keyword), page, pageSize, dao.findAll(keyword, offset, pageSize));
    }

    public void markRead(Long noticeId, Long userId) { getDao().insertReadRecord(noticeId, userId); }

    public int getReadCount(Long noticeId) { return getDao().getReadCount(noticeId); }

    public List<Long> getReadUserIds(Long noticeId) { return getDao().getReadUserIds(noticeId); }
}