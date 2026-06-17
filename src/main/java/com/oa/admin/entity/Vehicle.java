package com.oa.admin.entity;

public class Vehicle {
    private Long id;
    private String plateNumber;
    private String vehicleModel;
    private Integer seats;
    private String status;
    private java.time.LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
}
