package com.oa.schedule.entity;

public class CalendarEvent {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;
    private String eventType;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public java.time.LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(java.time.LocalDateTime startTime) { this.startTime = startTime; }
    public java.time.LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(java.time.LocalDateTime endTime) { this.endTime = endTime; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
    public java.time.LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(java.time.LocalDateTime updateTime) { this.updateTime = updateTime; }
}
