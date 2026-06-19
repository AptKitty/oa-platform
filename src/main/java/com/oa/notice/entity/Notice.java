package com.oa.notice.entity;

public class Notice {
    private Long id;
    private String title;
    private String content;
    private String contentHtml;
    private String attachment;
    private java.time.LocalDateTime scheduledTime;
        private Long publisherId;
    private String publisherName; // ?????(?????)
    private Integer isTop;
    private Integer status;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }
    public java.time.LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(java.time.LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public Integer getIsTop() { return isTop; }
    public void setIsTop(Integer isTop) { this.isTop = isTop; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
        public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
    public java.time.LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(java.time.LocalDateTime updateTime) { this.updateTime = updateTime; }
}

