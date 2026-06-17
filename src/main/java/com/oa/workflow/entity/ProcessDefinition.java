package com.oa.workflow.entity;

import com.oa.common.BaseEntity;

public class ProcessDefinition extends BaseEntity {
    private String defName;
    private String defCode;
    private Long templateId;
    private String description;
    private Integer status;

    public String getDefName() { return defName; }
    public void setDefName(String defName) { this.defName = defName; }
    public String getDefCode() { return defCode; }
    public void setDefCode(String defCode) { this.defCode = defCode; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
