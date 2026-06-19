package com.oa.system.service;

import com.oa.system.dao.AuditLogDao;
import com.oa.system.entity.AuditLog;
import com.oa.common.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import com.oa.common.Constants;

public class AuditLogService {

    public void log(String module, String action, String target, String detail) {
        try {
            AuditLog log = new AuditLog();
            log.setUserId(Constants.getCurrentUserId());
            log.setUsername(Constants.getCurrentUsername());
            log.setModule(module);
            log.setAction(action);
            log.setTarget(target);
            log.setDetail(detail);
            log.setIpAddress("127.0.0.1");
            try (SqlSession s = MyBatisUtil.openSession()) {
                s.getMapper(AuditLogDao.class).insert(log);
            }
        } catch (Exception ignored) { }
    }
}
