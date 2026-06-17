package com.oa.im.dao;

import com.oa.im.entity.Conversation;
import com.oa.im.entity.ImMessage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * IM数据访问接口 - 即时通讯模块（预留）
 *
 * 增量实现指南：
 * 1. 实现此接口的 SQL XML mapper
 * 2. 在 mybatis-config.xml 注册 mapper/im/ImMapper.xml
 * 3. 添加 WebSocket 服务端，实现消息实时推送
 * 4. 在 GUI 中添加聊天面板（ui/panel/ChatPanel.java）
 */
public interface ImDao {

    // 会话管理
    Conversation findConversationById(Long id);
    List<Conversation> findConversationsByUserId(Long userId);
    Long findPrivateConversation(@Param("user1") Long user1, @Param("user2") Long user2);
    int createConversation(Conversation conversation);
    int addMember(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    int removeMember(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    List<Long> getMemberIds(Long conversationId);

    // 消息管理
    int insertMessage(ImMessage message);
    List<ImMessage> findMessagesByConversation(@Param("conversationId") Long conversationId,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit);
    int markAsDelivered(List<Long> messageIds);
    int markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    int getUnreadCount(@Param("userId") Long userId);
}
