package com.oa.workflow.service;

import com.oa.workflow.dao.*;
import com.oa.workflow.entity.*;
import com.oa.common.MyBatisUtil;
import com.oa.common.Constants;
import com.oa.common.BusinessException;
import org.apache.ibatis.session.SqlSession;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 审批流程引擎服务 - 核心模块
 * 
 * 负责审批流程的全部业务逻辑：
 * - submitProcess(): 发起审批，创建流程实例和首节点任务
 * - approve():      审批通过，验证权限、记录操作、会签判断、节点前进
 * - reject():       审批驳回，验证权限、记录操作、标记流程结束
 * - getMyApplications():    查询某人发起的申请列表
 * - getPendingApprovals():  查询某人的待审批列表
 *
 * 核心技术点：
 * 1. 事务管理：手动控制 commit/rollback，保证数据一致性
 * 2. 节点类型处理：APPROVE(单人批)/SIGN(会签)/OR_SIGN(或签)/CC(抄送)/CONDITION(条件分支)
 * 3. 审批人解析：SPECIFIC_USER(指定人)/DEPT_LEADER(部门负责人)/ROLE(角色)
 *
 * @author 成员2
 */
public class WorkflowService {

    /**
     * 发起审批流程
     * 
     * 流程：
     * 1. 校验流程定义存在
     * 2. 创建流程实例(状态=PENDING)
     * 3. 查询流程的第一个审批节点
     * 4. 根据节点类型和审批人类型创建待审批任务(wf_task)
     * 5. 更新实例状态为 APPROVING
     *
     * @param defId         流程定义ID
     * @param applicantId   申请人ID
     * @param formDataJson  表单数据(JSON格式，包含所有字段的键值对)
     */
    public void submitProcess(Long defId, Long applicantId, String formDataJson) {
        // 打开数据库会话，autoCommit=false 表示手动提交事务
        SqlSession session = MyBatisUtil.openSession(false);
        try {
            ProcessDefinitionDao defDao = session.getMapper(ProcessDefinitionDao.class);
            ProcessInstanceDao instanceDao = session.getMapper(ProcessInstanceDao.class);

            // ① 校验流程定义是否存在
            ProcessDefinition def = defDao.findById(defId);
            if (def == null) throw new BusinessException("流程定义不存在");

            // ② 创建流程实例（申请单）
            ProcessInstance instance = new ProcessInstance();
            instance.setDefId(defId);
            instance.setDefName(def.getDefName());      // 快照流程名称
            instance.setTemplateId(def.getTemplateId());
            instance.setApplicantId(applicantId);
            instance.setFormData(formDataJson);
            instance.setStatus(Constants.APPROVAL_STATUS_PENDING);
            instanceDao.insert(instance);   // insert后id会自动回填

            // ③ 查询第一个审批节点(节点列表已按sort_order ASC排序)
            List<ProcessNode> nodes = defDao.findNodesByDefId(defId);
            ProcessNode firstNode = nodes.get(0);

            // ④ 创建第一个节点的审批任务
            TaskDao taskDao = session.getMapper(TaskDao.class);
            Task task = new Task();
            task.setInstanceId(instance.getId());        // 关联刚创建的实例
            task.setNodeId(firstNode.getId());
            task.setNodeName(firstNode.getNodeName());   // 快照节点名称
            task.setStatus("PENDING");

            // 根据审批人类型分配任务（当前仅支持 SPECIFIC_USER）
            if ("SPECIFIC_USER".equals(firstNode.getApproverType())) {
                task.setAssigneeId(firstNode.getApproverId());
                taskDao.insert(task);
            } else {
                throw new BusinessException("暂不支持该审批人类型: " + firstNode.getApproverType());
            }

            // ⑤ 更新实例状态为审批中
            instanceDao.updateStatus(instance.getId(), Constants.APPROVAL_STATUS_APPROVING);

            // 提交事务：所有数据库操作同时生效
            session.commit();
        } catch (Exception e) {
            // 任何异常都回滚，保证数据一致性
            session.rollback();
            throw new BusinessException("发起审批失败: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    /**
     * 审批通过
     * 
     * 流程：
     * 1. 验证审批人是否有权操作此流程
     * 2. 更新任务状态为 APPROVED
     * 3. 插入审批记录
     * 4. 查询当前节点信息
     * 5. 会签判断(SIGN节点需全员通过才能前进)
     * 6. 查找下一个节点并创建新任务，无下一节点则标记 PASSED
     *
     * @param instanceId 流程实例ID
     * @param approverId 审批人ID
     * @param comment    审批意见
     */
    public void approve(Long instanceId, Long approverId, String comment) {
        SqlSession session = MyBatisUtil.openSession(false);
        try {
            ProcessInstanceDao instanceDao = session.getMapper(ProcessInstanceDao.class);
            ProcessDefinitionDao defDao = session.getMapper(ProcessDefinitionDao.class);
            TaskDao taskDao = session.getMapper(TaskDao.class);

            // ① 验证审批权限：查wf_task看此人是否被分配了此流程的待审批任务
            Task task = taskDao.findByInstanceAndAssignee(instanceId, approverId);
            if (task == null) throw new BusinessException("无审批权限或该流程已处理");

            // ② 更新任务状态为已通过，记录完成时间
            taskDao.updateStatus(task.getId(), "APPROVED", LocalDateTime.now());

            // ③ 插入审批记录(用于时间线展示)
            ApprovalRecord record = new ApprovalRecord();
            record.setInstanceId(instanceId);
            record.setNodeId(task.getNodeId());
            record.setNodeName(task.getNodeName());
            record.setApproverId(approverId);
            record.setAction(Constants.ACTION_APPROVE);
            record.setComment(comment);
            instanceDao.insertApprovalRecord(record);

            // ④ 查询流程实例和所有节点，找到当前节点
            ProcessInstance instance = instanceDao.findById(instanceId);
            List<ProcessNode> nodes = defDao.findNodesByDefId(instance.getDefId());
            
            ProcessNode currentNode = null;
            for (ProcessNode n : nodes) { 
                if (n.getId().equals(task.getNodeId())) {
                    currentNode = n;
                    break;
                }
            }

            // ⑤ 会签(SIGN)判断：查该节点是否还有未处理的任务
            // 如果还有人在等待审批则提交事务并返回，不前进到下一节点
            if (Constants.NODE_TYPE_SIGN.equals(currentNode.getNodeType())) {
                int pendingCount = taskDao.countPendingByNodeId(currentNode.getId());
                if (pendingCount > 0) {
                    session.commit();
                    return;  // 还有人没批，停住等
                }
            }

            // ⑥ 查找下一个节点(sortOrder比当前大的第一个)
            ProcessNode nextNode = null;
            for (ProcessNode n : nodes) { 
                if (n.getSortOrder() > currentNode.getSortOrder()) {
                    nextNode = n;
                    break;
                }
            }

            // ⑦ 没有下一节点 → 流程通过
            if (nextNode == null) {
                instanceDao.updateStatus(instanceId, Constants.APPROVAL_STATUS_PASSED);
                session.commit();
                return;
            }

            // ⑧ 有下一节点 → 创建新审批任务(逻辑同submitProcess的步骤④)
            Task nextTask = new Task();
            nextTask.setInstanceId(instanceId);
            nextTask.setNodeId(nextNode.getId());
            nextTask.setNodeName(nextNode.getNodeName());
            nextTask.setStatus("PENDING");
            if (nextNode.getApproverType().equals("SPECIFIC_USER")) {
                nextTask.setAssigneeId(nextNode.getApproverId());
                taskDao.insert(nextTask);
            } else {
                throw new BusinessException("暂不支持该审批人类型: " + nextNode.getApproverType());
            }
            session.commit();

        } catch (BusinessException e) {
            session.rollback();
            throw e;  // 业务异常原样抛出
        } catch (Exception e) {
            session.rollback();
            throw new BusinessException("审批失败: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    /**
     * 审批驳回
     * 
     * 流程(比 approve 简单，不需要节点前进判断)：
     * 1. 验证审批人权限
     * 2. 更新任务状态为 REJECTED
     * 3. 插入审批记录
     * 4. 更新流程实例状态为 REJECTED
     *
     * @param instanceId 流程实例ID
     * @param approverId 审批人ID
     * @param comment    驳回理由
     */
    public void reject(Long instanceId, Long approverId, String comment) {
        SqlSession session = MyBatisUtil.openSession(false);
        try {
            ProcessInstanceDao instanceDao = session.getMapper(ProcessInstanceDao.class);
            TaskDao taskDao = session.getMapper(TaskDao.class);

            // ① 验证审批权限
            Task task = taskDao.findByInstanceAndAssignee(instanceId, approverId);
            if (task == null) throw new BusinessException("无审批权限或该流程已处理");

            // ② 更新任务状态为已驳回
            taskDao.updateStatus(task.getId(), "REJECTED", LocalDateTime.now());

            // ③ 插入审批记录(驳回操作日志)
            ApprovalRecord record = new ApprovalRecord(); 
            record.setInstanceId(instanceId);
            record.setNodeId(task.getNodeId());
            record.setNodeName(task.getNodeName());
            record.setApproverId(approverId);
            record.setAction(Constants.ACTION_REJECT);
            record.setComment(comment);
            instanceDao.insertApprovalRecord(record);

            // ④ 更新流程实例状态为已驳回(流程结束)
            instanceDao.updateStatus(instanceId, Constants.APPROVAL_STATUS_REJECTED);

            session.commit();
        } catch (BusinessException e) {
            session.rollback();
            throw e;
        } catch (Exception e) {
            session.rollback();
            throw new BusinessException("审批驳回失败: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    /**
     * 查询某人发起的审批申请列表(分页)
     * @param userId   申请人ID
     * @param status   状态筛选(可为null查全部)
     * @param page     页码(从1开始)
     * @param pageSize 每页条数
     */
    public List<ProcessInstance> getMyApplications(Long userId, String status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return MyBatisUtil.openSession().getMapper(ProcessInstanceDao.class)
                .findByApplicantId(userId, status, offset, pageSize);
    }

    /**
     * 查询某人的待审批列表(分页)
     * 通过 JOIN wf_task 表筛选出当前分配给该用户的待处理任务
     * @param approverId 审批人ID
     * @param page       页码(从1开始)
     * @param pageSize   每页条数
     */
    public List<ProcessInstance> getPendingApprovals(Long approverId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return MyBatisUtil.openSession().getMapper(ProcessInstanceDao.class)
                .findPendingByApproverId(approverId, offset, pageSize);
    }
}
