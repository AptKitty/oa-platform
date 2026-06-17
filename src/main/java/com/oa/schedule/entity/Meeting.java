package com.oa.schedule.entity;

public class Meeting {
    private Long id;
    private String title;
    private Long roomId;
    private Long hostId;
    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;
    private String description;
    private String status;
    private java.time.LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }
    public java.time.LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(java.time.LocalDateTime startTime) { this.startTime = startTime; }
    public java.time.LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(java.time.LocalDateTime endTime) { this.endTime = endTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
}
