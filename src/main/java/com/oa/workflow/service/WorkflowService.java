package com.oa.workflow.service;

import com.oa.workflow.dao.*;
import com.oa.workflow.entity.*;
import com.oa.common.MyBatisUtil;
import com.oa.common.Constants;
import com.oa.common.BusinessException;
import org.apache.ibatis.session.SqlSession;
import java.util.List;

/**
 * 审批流程引擎服务 - 核心模块
 * 模块负责人: 【组员B】 ← 最难点，需把控
 */
public class WorkflowService {

    /**
     * 提交流程实例（发起审批）
     */
    public void submitProcess(Long defId, Long applicantId, String formDataJson) {
        SqlSession session = MyBatisUtil.openSession(false);
        try {
            ProcessDefinitionDao defDao = session.getMapper(ProcessDefinitionDao.class);
            ProcessInstanceDao instanceDao = session.getMapper(ProcessInstanceDao.class);

            ProcessDefinition def = defDao.findById(defId);
            if (def == null) throw new BusinessException("流程定义不存在");

            ProcessInstance instance = new ProcessInstance();
            instance.setDefId(defId);
            instance.setDefName(def.getDefName());
            instance.setTemplateId(def.getTemplateId());
            instance.setApplicantId(applicantId);
            instance.setFormData(formDataJson);
            instance.setStatus(Constants.APPROVAL_STATUS_PENDING);
            instanceDao.insert(instance);

            // TODO: 创建第一个节点的审批任务(ProcessInstanceDao中实现)
            // 1. 查询第一个 ProcessNode
            // 2. 根据节点类型(APPROVE/CC/SIGN/OR_SIGN/CONDITION)创建审批人任务
            // 3. 如果是CONDITION节点，根据formDataJson评估条件选择下一个节点

            session.commit();
        } catch (Exception e) {
            session.rollback();
            throw new BusinessException("发起审批失败: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    /**
     * 审批通过
     */
    public void approve(Long instanceId, Long approverId, String comment) {
        // TODO: 实现审批通过逻辑
        // 1. 验证当前审批人是否匹配
        // 2. 记录审批记录
        // 3. 移动到下一个节点，若无下一节点则流程结束(status=PASSED)
        // 4. 若是会签/或签节点需特殊处理
    }

    /**
     * 审批驳回
     */
    public void reject(Long instanceId, Long approverId, String comment) {
        // TODO: 实现驳回逻辑（状态变为REJECTED）
    }

    public List<ProcessInstance> getMyApplications(Long userId, String status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return MyBatisUtil.openSession().getMapper(ProcessInstanceDao.class)
                .findByApplicantId(userId, status, offset, pageSize);
    }

    public List<ProcessInstance> getPendingApprovals(Long approverId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return MyBatisUtil.openSession().getMapper(ProcessInstanceDao.class)
                .findPendingByApproverId(approverId, offset, pageSize);
    }
}
