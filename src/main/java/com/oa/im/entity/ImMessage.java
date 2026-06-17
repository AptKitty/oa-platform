package com.oa.im.entity;

/**
 * IM聊天消息 - 即时通讯模块（预留）
 * 功能：支持 TEXT/IMAGE/FILE 三种消息类型
 */
public class ImMessage {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String contentType;
    private String content;
    private java.time.LocalDateTime sendTime;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public java.time.LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(java.time.LocalDateTime sendTime) { this.sendTime = sendTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
