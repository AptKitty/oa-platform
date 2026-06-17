package com.oa.workflow.entity;

import com.oa.common.BaseEntity;

public class ProcessNode extends BaseEntity {
    private Long defId;
    private String nodeName;
    private String nodeType;
    private String approverType;
    private Long approverId;
    private String approverRole;
    private String conditionExpr;
    private Integer sortOrder;

    public Long getDefId() { return defId; }
    public void setDefId(Long defId) { this.defId = defId; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public String getApproverType() { return approverType; }
    public void setApproverType(String approverType) { this.approverType = approverType; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public String getApproverRole() { return approverRole; }
    public void setApproverRole(String approverRole) { this.approverRole = approverRole; }
    public String getConditionExpr() { return conditionExpr; }
    public void setConditionExpr(String conditionExpr) { this.conditionExpr = conditionExpr; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
