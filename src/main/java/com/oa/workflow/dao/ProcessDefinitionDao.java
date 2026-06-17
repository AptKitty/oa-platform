package com.oa.workflow.dao;

import com.oa.workflow.entity.*;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ProcessDefinitionDao {
    ProcessDefinition findById(Long id);
    List<ProcessDefinition> findAll();
    List<ProcessNode> findNodesByDefId(Long defId);
    int insert(ProcessDefinition def);
    int update(ProcessDefinition def);
    int deleteById(Long id);
    int insertNode(ProcessNode node);
    int deleteNodesByDefId(Long defId);
}
