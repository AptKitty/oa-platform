package com.oa.notice.dao;

import com.oa.notice.entity.Message;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MessageDao {
    int insert(Message message);
    int markAsRead(@Param("id") Long id);
    int markAllAsRead(Long receiverId);
    List<Message> findByReceiverId(@Param("receiverId") Long receiverId,
                                    @Param("isRead") Integer isRead,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);
    long countByReceiverId(@Param("receiverId") Long receiverId, @Param("isRead") Integer isRead);
    int getUnreadCount(Long receiverId);
}
