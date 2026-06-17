package com.oa.im.dao;

import com.oa.im.entity.Conversation;
import com.oa.im.entity.ImMessage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ImDao {
    // 消息
    int insertMessage(ImMessage message);
    List<ImMessage> findMessagesByConversation(@Param("conversationId") Long conversationId,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit);
    int getUnreadCount(@Param("userId") Long userId);

    // 会话
    int createConversation(Conversation conversation);
    int addMember(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    Long findPrivateConversation(@Param("user1") Long user1, @Param("user2") Long user2);
    List<Conversation> findConversationsByUserId(@Param("userId") Long userId);
}
