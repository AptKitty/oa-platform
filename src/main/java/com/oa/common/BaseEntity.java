package com.oa.common;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类 - 所有实体类继承此类
 */
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
