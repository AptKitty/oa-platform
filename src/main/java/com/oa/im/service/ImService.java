package com.oa.im.service;

import com.oa.common.MyBatisUtil;
import com.oa.im.dao.ImDao;
import com.oa.im.entity.Conversation;
import com.oa.im.entity.ImMessage;
import org.apache.ibatis.session.SqlSession;
import java.util.List;

public class ImService {

    public void sendMessage(Long conversationId, Long senderId, String content) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            ImMessage msg = new ImMessage();
            msg.setConversationId(conversationId);
            msg.setSenderId(senderId);
            msg.setContentType("TEXT");
            msg.setContent(content);
            s.getMapper(ImDao.class).insertMessage(msg);
        }
    }

    public Long getOrCreatePrivateConversation(Long user1, Long user2) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            ImDao dao = s.getMapper(ImDao.class);
            Long convId = dao.findPrivateConversation(user1, user2);
            if (convId == null) {
                Conversation conv = new Conversation();
                conv.setConversationType("PRIVATE");
                dao.createConversation(conv);
                convId = conv.getId();
                dao.addMember(convId, user1);
                dao.addMember(convId, user2);
            }
            return convId;
        }
    }

    public List<ImMessage> getHistory(Long conversationId, int page, int pageSize) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(ImDao.class).findMessagesByConversation(conversationId, (page - 1) * pageSize, pageSize);
        }
    }

    public List<Conversation> getUserConversations(Long userId) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(ImDao.class).findConversationsByUserId(userId);
        }
    }

    public int getUnreadCount(Long userId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(ImDao.class).getUnreadCount(userId); }
    }
}