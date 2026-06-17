package com.oa.admin.entity;

public class Asset {
    private Long id;
    private String assetName;
    private String assetCode;
    private String category;
    private String model;
    private Long deptId;
    private Long keeperId;
    private String status;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Long getKeeperId() { return keeperId; }
    public void setKeeperId(Long keeperId) { this.keeperId = keeperId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
    public java.time.LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(java.time.LocalDateTime updateTime) { this.updateTime = updateTime; }
}
