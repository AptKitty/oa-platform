package com.oa.workflow.dao;

import com.oa.workflow.entity.*;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface FormTemplateDao {
    FormTemplate findById(Long id);
    List<FormTemplate> findAll();
    List<FormField> findFieldsByTemplateId(Long templateId);
    int insert(FormTemplate template);
    int update(FormTemplate template);
    int deleteById(Long id);
    int insertField(FormField field);
    int deleteFieldsByTemplateId(Long templateId);
}
