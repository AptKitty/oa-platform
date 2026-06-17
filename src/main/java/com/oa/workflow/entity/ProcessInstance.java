package com.oa.workflow.entity;

public class ProcessInstance {
    private Long id;
    private Long defId;
    private String defName;
    private Long templateId;
    private Long applicantId;
    private String formData;
    private String status;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDefId() { return defId; }
    public void setDefId(Long defId) { this.defId = defId; }
    public String getDefName() { return defName; }
    public void setDefName(String defName) { this.defName = defName; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public String getFormData() { return formData; }
    public void setFormData(String formData) { this.formData = formData; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
    public java.time.LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(java.time.LocalDateTime updateTime) { this.updateTime = updateTime; }
}
