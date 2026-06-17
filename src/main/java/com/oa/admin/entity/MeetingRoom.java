package com.oa.admin.entity;

public class MeetingRoom {
    private Long id;
    private String roomName;
    private String location;
    private Integer capacity;
    private Integer hasProjector;
    private Integer hasMicrophone;
    private Integer status;
    private java.time.LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getHasProjector() { return hasProjector; }
    public void setHasProjector(Integer hasProjector) { this.hasProjector = hasProjector; }
    public Integer getHasMicrophone() { return hasMicrophone; }
    public void setHasMicrophone(Integer hasMicrophone) { this.hasMicrophone = hasMicrophone; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
}
