package com.oa.workflow.dao;

import com.oa.workflow.entity.Task;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 审批任务 DAO 接口
 * 操作 wf_task 表，管理审批任务的生命周期
 * 每个方法对应 TaskMapper.xml 中的一条 SQL 语句
 *
 * @author 成员2
 */
public interface TaskDao {

    /**
     * 新增审批任务
     * 在 submitProcess() 发起审批和 approve() 前进到下一节点时调用
     *
     * @param task 任务对象(需填充 instanceId/nodeId/nodeName/assigneeId/status)
     * @return 受影响行数
     */
    int insert(Task task);

    /**
     * 根据流程实例和审批人查找待处理任务
     * approve()/reject() 中用于验证该审批人是否有权操作此流程
     * SQL 会同时过滤 status='PENDING'，已处理的任务查不到
     *
     * @param instanceId 流程实例ID
     * @param assigneeId 审批人用户ID
     * @return 匹配的任务，null 表示无权限或已处理
     */
    Task findByInstanceAndAssignee(@Param("instanceId") Long instanceId,
                                   @Param("assigneeId") Long assigneeId);

    /**
     * 更新任务状态及完成时间
     *
     * @param id 任务ID
     * @param status 新状态(APPROVED/REJECTED)
     * @param completeTime 完成时间(通常填 LocalDateTime.now())
     * @return 受影响行数
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("completeTime") java.time.LocalDateTime completeTime);

    /**
     * 统计某节点下还有多少待处理任务
     * approve() 中会签(SIGN)判断：count=0 表示全部通过，可前进
     *
     * @param nodeId 节点ID
     * @return 待处理任务数量
     */
    int countPendingByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 查询某流程实例下的所有任务
     * 用于或签(OR_SIGN)/会签(SIGN)时查看所有审批人的状态
     *
     * @param instanceId 流程实例ID
     * @return 该实例的所有任务列表
     */
    List<Task> findByInstanceId(Long instanceId);
}
