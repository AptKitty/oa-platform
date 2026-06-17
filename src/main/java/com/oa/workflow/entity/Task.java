package com.oa.workflow.entity;

/**
 * 审批任务实体
 * 对应数据库表 wf_task
 * 记录待审批任务：哪个流程实例的哪个节点分配给谁审批
 * 不继承 BaseEntity，因为该表没有 update_time 列，改用 complete_time
 *
 * @author 成员2
 */
public class Task {

    /** 任务ID(自增主键) */
    private Long id;

    /** 所属流程实例ID */
    private Long instanceId;

    /** 审批节点ID */
    private Long nodeId;

    /** 节点名称快照(节点名称后续可能修改，此处存快照) */
    private String nodeName;

    /** 审批人ID(分配给谁审批) */
    private Long assigneeId;

    /** 任务状态: PENDING=待处理, APPROVED=已通过, REJECTED=已驳回 */
    private String status;

    /** 任务创建时间 */
    private java.time.LocalDateTime createTime;

    /** 任务完成时间(审批通过或驳回的时刻) */
    private java.time.LocalDateTime completeTime;

    // ===== getters/setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
    public Long getNodeId() { return nodeId; }
    public void setNodeId(Long nodeID) { this.nodeId = nodeID; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.time.LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
    public java.time.LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(java.time.LocalDateTime updateTime) { this.completeTime = updateTime; }
}
