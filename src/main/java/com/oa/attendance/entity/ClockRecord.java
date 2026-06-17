package com.oa.attendance.entity;

public class ClockRecord {
    private Long id;
    private Long userId;
    private String clockType;
    private java.time.LocalDateTime clockTime;
    private String ipAddress;
    private java.time.LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getClockType() { return clockType; }
    public void setClockType(String clockType) { this.clockType = clockType; }
    public java.time.LocalDateTime getClockTime() { return clockTime; }
    public void setClockTime(java.time.LocalDateTime clockTime) { this.clockTime = clockTime; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
}
