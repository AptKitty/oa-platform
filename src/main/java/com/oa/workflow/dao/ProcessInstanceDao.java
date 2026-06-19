package com.oa.workflow.dao;

import com.oa.workflow.entity.ProcessInstance;
import com.oa.workflow.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ProcessInstanceDao {
    ProcessInstance findById(Long id);
    List<ProcessInstance> findByApplicantId(@Param("applicantId") Long applicantId,
                                            @Param("status") String status,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);
    long countByApplicantId(@Param("applicantId") Long applicantId, @Param("status") String status);
    List<ProcessInstance> findPendingByApproverId(@Param("approverId") Long approverId,
                                                   @Param("offset") int offset,
                                                   @Param("limit") int limit);
    long countPendingByApproverId(Long approverId);
    int insert(ProcessInstance instance);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int insertApprovalRecord(ApprovalRecord record);
    List<ApprovalRecord> findRecordsByInstanceId(Long instanceId);
    int deleteById(Long id);
}
