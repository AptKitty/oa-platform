package com.oa.workflow.entity;

import com.oa.common.BaseEntity;

public class FormField extends BaseEntity {
    private Long templateId;
    private String fieldName;
    private String fieldLabel;
    private String fieldType;
    private Integer isRequired;
    private String options;
    private Integer sortOrder;

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getFieldLabel() { return fieldLabel; }
    public void setFieldLabel(String fieldLabel) { this.fieldLabel = fieldLabel; }
    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }
    public Integer getIsRequired() { return isRequired; }
    public void setIsRequired(Integer isRequired) { this.isRequired = isRequired; }
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
