package com.oa.admin.entity;

public class AssetRecord {
    private Long id;
    private Long assetId;
    private Long userId;
    private String action;
    private java.time.LocalDateTime actionTime;
    private String remark;
    private java.time.LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public java.time.LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(java.time.LocalDateTime actionTime) { this.actionTime = actionTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
}
