package com.oa.notice.dao;

import com.oa.notice.entity.Notice;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface NoticeDao {
    int insert(Notice notice);
    int update(Notice notice);
    int deleteById(Long id);
    Notice findById(Long id);
    List<Notice> findAll(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    long count(String keyword);
    int insertReadRecord(@Param("noticeId") Long noticeId, @Param("userId") Long userId);
    int getReadCount(Long noticeId);
    List<Long> getReadUserIds(Long noticeId);
    /** 查询到期需要激活的定时公告 */
    List<Notice> findScheduledToActivate();
    List<java.util.Map<String, Object>> getReadUserNames(Long noticeId);
    List<java.util.Map<String, Object>> getUnreadUserNames(Long noticeId);
    int getActiveUserCount();
}


