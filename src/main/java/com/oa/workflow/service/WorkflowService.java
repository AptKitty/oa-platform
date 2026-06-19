package com.oa.workflow.service;

import com.oa.workflow.dao.*;
import com.oa.workflow.entity.*;
import com.oa.common.MyBatisUtil;
import com.oa.common.Constants;
import com.oa.common.BusinessException;
import com.oa.attendance.service.AttendanceService;
import com.oa.attendance.dao.LeaveDao;
import com.oa.attendance.entity.LeaveRequest;
import com.oa.notice.service.MessageService;
import com.oa.notice.entity.Message;
import com.oa.system.dao.UserDao;
import com.oa.system.dao.DeptDao;
import com.oa.system.dao.RoleDao;
import com.oa.system.entity.User;
import com.oa.system.entity.Dept;
import org.apache.ibatis.session.SqlSession;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
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

            // ③ 遍历节点列表，跳过 CC（抄送）节点
            // CC 节点不创建待办任务，直接插入审批记录后自动跳过
            List<ProcessNode> nodes = defDao.findNodesByDefId(defId);
            int nodeIndex = 0;
            ProcessNode firstNode = nodes.get(nodeIndex);
            while (nodeIndex < nodes.size() && "CC".equals(firstNode.getNodeType())) {
                // 插入抄送记录（无需人工操作）
                ApprovalRecord ccRecord = new ApprovalRecord();
                ccRecord.setInstanceId(instance.getId());
                ccRecord.setNodeId(firstNode.getId());
                ccRecord.setNodeName(firstNode.getNodeName());
                ccRecord.setApproverId(firstNode.getApproverId() != null ? firstNode.getApproverId() : 0L);
                ccRecord.setAction("CC");
                ccRecord.setComment("系统自动抄送");
                instanceDao.insertApprovalRecord(ccRecord);

                nodeIndex++;
                if (nodeIndex >= nodes.size()) {
                    // 全部节点都是 CC，流程直接通过
                    instanceDao.updateStatus(instance.getId(), Constants.APPROVAL_STATUS_PASSED);
                    session.commit();
                    return;
                }
                firstNode = nodes.get(nodeIndex);
            }

            // 处理 CONDITION（条件分支）节点：根据表单数据自动求值路由
            while (nodeIndex < nodes.size() && "CONDITION".equals(firstNode.getNodeType())) {
                boolean condResult = evaluateCondition(firstNode.getConditionExpr(), formDataJson);
                if (condResult) {
                    // 条件成立 → 找下一个非CC非CONDITION节点
                    nodeIndex++;
                    if (nodeIndex >= nodes.size()) {
                        instanceDao.updateStatus(instance.getId(), Constants.APPROVAL_STATUS_PASSED);
                        session.commit();
                        return;
                    }
                    firstNode = nodes.get(nodeIndex);
                    // 跳过紧跟的 CC 节点
                    while (nodeIndex < nodes.size() && "CC".equals(firstNode.getNodeType())) {
                        ApprovalRecord ccR = new ApprovalRecord();
                        ccR.setInstanceId(instance.getId());
                        ccR.setNodeId(firstNode.getId());
                        ccR.setNodeName(firstNode.getNodeName());
                        ccR.setApproverId(firstNode.getApproverId() != null ? firstNode.getApproverId() : 0L);
                        ccR.setAction("CC");
                        ccR.setComment("系统自动抄送");
                        instanceDao.insertApprovalRecord(ccR);
                        nodeIndex++;
                        if (nodeIndex >= nodes.size()) {
                            instanceDao.updateStatus(instance.getId(), Constants.APPROVAL_STATUS_PASSED);
                            session.commit();
                            return;
                        }
                        firstNode = nodes.get(nodeIndex);
                    }
                } else {
                    // 条件不成立 → 跳过整个分支，找下一个 CONDITION 节点或结束
                    nodeIndex++;
                    while (nodeIndex < nodes.size()
                            && !"CONDITION".equals(nodes.get(nodeIndex).getNodeType())) {
                        nodeIndex++;
                    }
                    if (nodeIndex >= nodes.size()) {
                        instanceDao.updateStatus(instance.getId(), Constants.APPROVAL_STATUS_PASSED);
                        session.commit();
                        return;
                    }
                    firstNode = nodes.get(nodeIndex);
                }
            }

            // 兜底：落在 CC 或 CONDITION 上时流程通过
            if (nodeIndex >= nodes.size()) {
                instanceDao.updateStatus(instance.getId(), Constants.APPROVAL_STATUS_PASSED);
                session.commit();
                return;
            }

            // ④ 创建第一个实际审批节点的审批任务
            TaskDao taskDao = session.getMapper(TaskDao.class);
            Task task = new Task();
            task.setInstanceId(instance.getId());        // 关联刚创建的实例
            task.setNodeId(firstNode.getId());
            task.setNodeName(firstNode.getNodeName());   // 快照节点名称
            task.setStatus("PENDING");

            // 根据审批人类型解析所有审批人，为每个审批人创建任务
            // SPECIFIC_USER → 单人或多人（SIGN/OR_SIGN 时创建多个任务）
            // DEPT_LEADER    → 查申请人的部门负责人
            // ROLE           → 查拥有该角色的所有用户
            List<Long> approverIds = resolveApproverIds(firstNode, applicantId, session);
            for (Long assigneeId : approverIds) {
                Task t = new Task();
                t.setInstanceId(instance.getId());
                t.setNodeId(firstNode.getId());
                t.setNodeName(firstNode.getNodeName());
                t.setAssigneeId(assigneeId);
                t.setStatus("PENDING");
                taskDao.insert(t);
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
    public void approve(Long instanceId, Long approverId, String comment, String attachments) {
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
            record.setAttachments(attachments);
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
            if (Constants.NODE_TYPE_SIGN.equals(currentNode.getNodeType()) || Constants.NODE_TYPE_OR_SIGN.equals(currentNode.getNodeType())) {
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
                sendApproveResultNotification(instance, "已通过");
                handlePostApproval(instance);
                return;
            }

            // ⑧ 处理下一个节点（跳过 CC 抄送节点）
            while (nextNode != null && "CC".equals(nextNode.getNodeType())) {
                // CC 节点自动抄送，不创建待办任务
                ApprovalRecord ccRecord = new ApprovalRecord();
                ccRecord.setInstanceId(instanceId);
                ccRecord.setNodeId(nextNode.getId());
                ccRecord.setNodeName(nextNode.getNodeName());
                ccRecord.setApproverId(nextNode.getApproverId() != null ? nextNode.getApproverId() : 0L);
                ccRecord.setAction("CC");
                ccRecord.setComment("系统自动抄送");
                instanceDao.insertApprovalRecord(ccRecord);

                // 跳到下一个节点（以 CC 节点为起点继续找）
                currentNode = nextNode;
                nextNode = null;
                for (ProcessNode n : nodes) {
                    if (n.getSortOrder() > currentNode.getSortOrder()) {
                        nextNode = n;
                        break;
                    }
                }
            }

            // 没有下一个非 CC 节点 → 流程通过
            if (nextNode == null) {
                instanceDao.updateStatus(instanceId, Constants.APPROVAL_STATUS_PASSED);
                session.commit();
                sendApproveResultNotification(instance, "已通过");
                handlePostApproval(instance);
                return;
            }

            // 处理 CONDITION（条件分支）：自动求值决定走不走这个分支
            while (nextNode != null && "CONDITION".equals(nextNode.getNodeType())) {
                ProcessInstance inst = instanceDao.findById(instanceId);
                boolean condResult = evaluateCondition(nextNode.getConditionExpr(), inst.getFormData());
                if (condResult) {
                    // 条件成立 → 找下一个非CC非CONDITION节点
                    currentNode = nextNode;
                    nextNode = null;
                    for (ProcessNode n : nodes) {
                        if (n.getSortOrder() > currentNode.getSortOrder()) {
                            nextNode = n;
                            break;
                        }
                    }
                    // 跳过紧跟的 CC 节点
                    while (nextNode != null && "CC".equals(nextNode.getNodeType())) {
                        ApprovalRecord ccR = new ApprovalRecord();
                        ccR.setInstanceId(instanceId);
                        ccR.setNodeId(nextNode.getId());
                        ccR.setNodeName(nextNode.getNodeName());
                        ccR.setApproverId(nextNode.getApproverId() != null ? nextNode.getApproverId() : 0L);
                        ccR.setAction("CC");
                        ccR.setComment("系统自动抄送");
                        instanceDao.insertApprovalRecord(ccR);
                        currentNode = nextNode;
                        nextNode = null;
                        for (ProcessNode n : nodes) {
                            if (n.getSortOrder() > currentNode.getSortOrder()) {
                                nextNode = n;
                                break;
                            }
                        }
                    }
                } else {
                    // 条件不成立 → 跳过整个分支，找下一个 CONDITION 节点或结束
                    currentNode = nextNode;
                    nextNode = null;
                    for (ProcessNode n : nodes) {
                        if (n.getSortOrder() > currentNode.getSortOrder()) {
                            if ("CONDITION".equals(n.getNodeType())) {
                                nextNode = n;
                                break;
                            }
                            currentNode = n;
                        }
                    }
                    if (nextNode == null) {
                        instanceDao.updateStatus(instanceId, Constants.APPROVAL_STATUS_PASSED);
                        session.commit();
                sendApproveResultNotification(instance, "已通过");
                handlePostApproval(instance);
                        return;
                    }
                }
            }

            // 兜底检查
            if (nextNode == null) {
                instanceDao.updateStatus(instanceId, Constants.APPROVAL_STATUS_PASSED);
                session.commit();
                sendApproveResultNotification(instance, "已通过");
                handlePostApproval(instance);
                return;
            }

            // 有下一个非 CC 节点 → 创建审批任务
            Task nextTask = new Task();
            nextTask.setInstanceId(instanceId);
            nextTask.setNodeId(nextNode.getId());
            nextTask.setNodeName(nextNode.getNodeName());
            nextTask.setStatus("PENDING");
            // 解析审批人，为每个审批人创建任务
            List<Long> nextApproverIds = resolveApproverIds(nextNode, instance.getApplicantId(), session);
            for (Long assigneeId : nextApproverIds) {
                Task t = new Task();
                t.setInstanceId(instanceId);
                t.setNodeId(nextNode.getId());
                t.setNodeName(nextNode.getNodeName());
                t.setAssigneeId(assigneeId);
                t.setStatus("PENDING");
                taskDao.insert(t);
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
    public void reject(Long instanceId, Long approverId, String comment, String attachments) {
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
            record.setAttachments(attachments);
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
     * Revoke application
     */
    public void revoke(Long instanceId, Long applicantId) {
        SqlSession session = MyBatisUtil.openSession(false);
        try {
            ProcessInstanceDao instanceDao = session.getMapper(ProcessInstanceDao.class);
            TaskDao taskDao = session.getMapper(TaskDao.class);

            ProcessInstance instance = instanceDao.findById(instanceId);
            if (instance == null) throw new BusinessException("Application not found");
            if (!instance.getApplicantId().equals(applicantId)) throw new BusinessException("Can only revoke own applications");

            if (!Constants.APPROVAL_STATUS_PENDING.equals(instance.getStatus())
                && !Constants.APPROVAL_STATUS_APPROVING.equals(instance.getStatus())) {
                throw new BusinessException("Current status does not allow revocation");
            }

            instanceDao.updateStatus(instanceId, Constants.APPROVAL_STATUS_CANCELLED);

            List<Task> tasks = taskDao.findByInstanceId(instanceId);
            for (Task t : tasks) {
                if ("PENDING".equals(t.getStatus())) {
                    taskDao.updateStatus(t.getId(), "CANCELLED", LocalDateTime.now());
                }
            }

            ApprovalRecord record = new ApprovalRecord();
            record.setInstanceId(instanceId);
            record.setNodeId(0L);  // ?????????
            record.setNodeName("Applicant Revoke");
            record.setApproverId(applicantId);
            record.setAction("REVOKE");
            record.setComment("Applicant voluntarily revoked");
            instanceDao.insertApprovalRecord(record);

            session.commit();
        } catch (BusinessException e) {
            session.rollback();
            throw e;
        } catch (Exception e) {
            session.rollback();
            throw new BusinessException("Revoke failed: " + e.getMessage());
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

    /**
     * 审批通过后置处理：检查是否为请假模板，自动扣减请假额度
     */
    
    /** 发送审批通知给所有审批人 */
    private void sendApprovalNotifications(ProcessInstance instance, List<Long> approverIds, SqlSession session) {
        try {
            MessageService messageService = new MessageService();
            for (Long approverId : approverIds) {
                Message msg = new Message();
                msg.setSenderId(0L); // 系统发送
                msg.setReceiverId(approverId);
                msg.setTitle("新的审批待办");
                msg.setContent("您有一项待审批的申请：【" + instance.getDefName() + "】，请及时处理。");
                msg.setMsgType("APPROVAL");
                messageService.send(msg);
            }
        } catch (Exception e) {
            System.err.println("发送审批通知失败: " + e.getMessage());
        }
    }

    /** 发送审批结果通知给申请人 */
    private void sendApproveResultNotification(ProcessInstance instance, String result) {
        try {
            MessageService messageService = new MessageService();
            Message msg = new Message();
            msg.setSenderId(0L); // 系统发送
            msg.setReceiverId(instance.getApplicantId());
            msg.setTitle("审批结果通知");
            msg.setContent("您的申请【" + instance.getDefName() + "】已" + result + "，请查看详情。");
            msg.setMsgType("APPROVAL");
            messageService.send(msg);
        } catch (Exception e) {
            System.err.println("发送审批结果通知失败: " + e.getMessage());
        }
    }

    private void handlePostApproval(ProcessInstance instance) {
        try {
            FormTemplateDao templateDao = MyBatisUtil.openSession().getMapper(FormTemplateDao.class);
            FormTemplate template = templateDao.findById(instance.getTemplateId());
            if (template == null || !"LEAVE".equals(template.getTemplateCode())) return;

            // 从 form_data JSON 中取请假类型和天数
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> formData = mapper.readValue(instance.getFormData(), Map.class);
            String leaveType = formData.get("leaveType") != null ? formData.get("leaveType").toString() : null;
            Object daysObj = formData.get("days");
            if (leaveType == null || daysObj == null) return;

            double days = Double.parseDouble(daysObj.toString());
            AttendanceService attendanceService = new AttendanceService();
            attendanceService.deductLeaveQuota(instance.getApplicantId(), leaveType, days);
        } catch (Exception e) {
            System.err.println("请假额度扣减失败: " + e.getMessage());
        }
    }

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
    /**
     * 求值条件表达式：解析 "field op value" 格式（如 "amount > 1000"），
     * 从 formDataJson 中取出字段值与条件值比较。
     * 支持的操作符：>  <  >=  <=  ==  !=
     */
    private boolean evaluateCondition(String conditionExpr, String formDataJson) {
        if (conditionExpr == null || conditionExpr.trim().isEmpty()) return true;
        try {
            String[] parts = conditionExpr.trim().split("\\s+");
            if (parts.length != 3) throw new BusinessException("条件表达式格式错误: " + conditionExpr);
            String fieldName = parts[0];
            String operator  = parts[1];
            String condValue = parts[2];

            // 解析 JSON 获取表单数据
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> formData = mapper.readValue(formDataJson, Map.class);
            Object fieldValue = formData.get(fieldName);
            if (fieldValue == null) return false;

            double fieldNum = Double.parseDouble(fieldValue.toString());
            double condNum  = Double.parseDouble(condValue);

            switch (operator) {
                case ">":  return fieldNum > condNum;
                case "<":  return fieldNum < condNum;
                case ">=": return fieldNum >= condNum;
                case "<=": return fieldNum <= condNum;
                case "==": return fieldNum == condNum;
                case "!=": return fieldNum != condNum;
                default: throw new BusinessException("不支持的操作符: " + operator);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("条件表达式求值失败: " + conditionExpr + ", " + e.getMessage());
        }
    }

    /**
     * 根据审批人类型解析审批人ID列表
     * SPECIFIC_USER → 返回节点的 approverId（单人）
     * DEPT_LEADER    → 查询申请人的部门，返回部门负责人ID
     * ROLE           → 查询拥有该角色的所有用户ID
     */
    private List<Long> resolveApproverIds(ProcessNode node, Long applicantId, SqlSession session) {
        String type = node.getApproverType();
        if ("SPECIFIC_USER".equals(type)) {
            return Collections.singletonList(node.getApproverId());
        }
        if ("DEPT_LEADER".equals(type)) {
            UserDao userDao = session.getMapper(UserDao.class);
            DeptDao deptDao = session.getMapper(DeptDao.class);
            User applicant = userDao.findById(applicantId);
            if (applicant == null || applicant.getDeptId() == null) {
                throw new BusinessException("未找到申请人部门信息");
            }
            Dept dept = deptDao.findById(applicant.getDeptId());
            if (dept == null || dept.getLeaderId() == null) {
                throw new BusinessException("未找到部门负责人");
            }
            return Collections.singletonList(dept.getLeaderId());
        }
        if ("ROLE".equals(type)) {
            RoleDao roleDao = session.getMapper(RoleDao.class);
            List<Long> userIds = roleDao.findUserIdsByRoleCode(node.getApproverRole());
            if (userIds == null || userIds.isEmpty()) {
                throw new BusinessException("未找到拥有角色 " + node.getApproverRole() + " 的用户");
            }
            return userIds;
        }
        throw new BusinessException("暂不支持该审批人类型: " + type);
    }

    public List<ProcessInstance> getPendingApprovals(Long approverId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return MyBatisUtil.openSession().getMapper(ProcessInstanceDao.class)
                .findPendingByApproverId(approverId, offset, pageSize);
    }
}




