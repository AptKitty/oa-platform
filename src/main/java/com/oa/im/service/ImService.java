package com.oa.im.service;

import com.oa.common.MyBatisUtil;
import com.oa.im.dao.ImDao;
import com.oa.im.entity.Conversation;
import com.oa.im.entity.ImMessage;
import java.util.List;

/**
 * IM服务 - 即时通讯模块（预留骨架）
 *
 * 当前为单机轮询版本（预留）。
 * 完整版建议改造为 WebSocket 实时推送：
 *   - 服务端: org.java-websocket (已引入依赖)
 *   - websocket/ 目录下创建 ImWebSocketServer.java
 *   - GUI 通过 WebSocket 接收实时消息而非轮询
 */
public class ImService {

    /**
     * TODO: 增量开发时改为连接池注入
     */
    private ImDao getDao() {
        return MyBatisUtil.openSession().getMapper(ImDao.class);
    }

    public void sendMessage(Long conversationId, Long senderId, String content) {
        ImMessage msg = new ImMessage();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setContentType("TEXT");
        msg.setContent(content);
        getDao().insertMessage(msg);
    }

    public Long getOrCreatePrivateConversation(Long user1, Long user2) {
        Long convId = getDao().findPrivateConversation(user1, user2);
        if (convId == null) {
            Conversation conv = new Conversation();
            conv.setConversationType("PRIVATE");
            getDao().createConversation(conv);
            convId = conv.getId();
            getDao().addMember(convId, user1);
            getDao().addMember(convId, user2);
        }
        return convId;
    }

    public List<ImMessage> getHistory(Long conversationId, int page, int pageSize) {
        return getDao().findMessagesByConversation(conversationId, (page - 1) * pageSize, pageSize);
    }

    public List<Conversation> getUserConversations(Long userId) {
        return getDao().findConversationsByUserId(userId);
    }

    public int getUnreadCount(Long userId) {
        return getDao().getUnreadCount(userId);
    }
}
