-- ===================================================
-- OA协同办公平台 数据库 DDL
-- 创建时间: 2026-06-09
-- ===================================================

CREATE DATABASE IF NOT EXISTS oa_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE oa_platform;

-- ==================== 系统管理模块 ====================

-- 部门表
CREATE TABLE sys_dept (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       DEFAULT 0  COMMENT '父部门ID',
    dept_name   VARCHAR(100) NOT NULL   COMMENT '部门名称',
    dept_code   VARCHAR(50)  NOT NULL   COMMENT '部门编码',
    leader_id   BIGINT       DEFAULT NULL COMMENT '负责人ID',
    sort_order  INT          DEFAULT 0  COMMENT '排序',
    status      TINYINT      DEFAULT 1  COMMENT '状态 1启用 0禁用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '部门表';

-- 用户表
CREATE TABLE sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录名',
    password    VARCHAR(128) NOT NULL       COMMENT '密码(MD5加密)',
    real_name   VARCHAR(50)  NOT NULL       COMMENT '真实姓名',
    phone       VARCHAR(20)  DEFAULT NULL   COMMENT '手机号',
    email       VARCHAR(100) DEFAULT NULL   COMMENT '邮箱',
    dept_id     BIGINT       DEFAULT NULL   COMMENT '所属部门ID',
    position    VARCHAR(50)  DEFAULT NULL   COMMENT '岗位',
    hire_date   DATE         DEFAULT NULL   COMMENT '入职日期',
    avatar      VARCHAR(255) DEFAULT NULL   COMMENT '头像路径',
    status      TINYINT      DEFAULT 1      COMMENT '状态 1正常 0禁用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

-- 角色表
CREATE TABLE sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(50)  NOT NULL       COMMENT '角色名称',
    role_code   VARCHAR(50)  NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) DEFAULT NULL   COMMENT '描述',
    status      TINYINT      DEFAULT 1      COMMENT '状态',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '角色表';

-- 用户-角色关联表
CREATE TABLE sys_user_role (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT '用户角色关联';

-- 权限/菜单表
CREATE TABLE sys_menu (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT      DEFAULT 0   COMMENT '父菜单ID',
    menu_name   VARCHAR(50) NOT NULL    COMMENT '菜单名称',
    menu_code   VARCHAR(100) DEFAULT NULL COMMENT '菜单编码/权限标识',
    menu_type   VARCHAR(10) NOT NULL    COMMENT '菜单类型 MENU/BUTTON',
    icon        VARCHAR(50) DEFAULT NULL COMMENT '图标',
    sort_order  INT         DEFAULT 0   COMMENT '排序',
    status      TINYINT     DEFAULT 1   COMMENT '状态',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '菜单权限表';

-- 角色-菜单关联表
CREATE TABLE sys_role_menu (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) COMMENT '角色菜单关联';

-- ==================== 审批流程引擎 ====================

-- 审批表单模板
CREATE TABLE wf_form_template (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL  COMMENT '模板名称',
    template_code VARCHAR(50)  NOT NULL UNIQUE COMMENT '模板编码',
    description  VARCHAR(500) DEFAULT NULL COMMENT '描述',
    category     VARCHAR(50)  NOT NULL  COMMENT '分类 请假/报销/出差/加班/用章/采购/自定义',
    status       TINYINT      DEFAULT 1 COMMENT '状态',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '审批表单模板';

-- 表单字段定义
CREATE TABLE wf_form_field (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id  BIGINT       NOT NULL  COMMENT '所属模板ID',
    field_name   VARCHAR(50)  NOT NULL  COMMENT '字段名',
    field_label  VARCHAR(100) NOT NULL  COMMENT '字段标签',
    field_type   VARCHAR(30)  NOT NULL  COMMENT '字段类型 TEXT/NUMBER/DATE/SELECT/ATTACHMENT/TEXTAREA',
    is_required  TINYINT      DEFAULT 1 COMMENT '是否必填',
    options      VARCHAR(500) DEFAULT NULL COMMENT '选项(JSON,用于SELECT类型)',
    sort_order   INT          DEFAULT 0 COMMENT '排序',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP
) COMMENT '表单字段定义';

-- 审批流程定义
CREATE TABLE wf_process_definition (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    def_name     VARCHAR(100) NOT NULL  COMMENT '流程名称',
    def_code     VARCHAR(50)  NOT NULL UNIQUE COMMENT '流程编码',
    template_id  BIGINT       NOT NULL  COMMENT '关联表单模板ID',
    description  VARCHAR(500) DEFAULT NULL COMMENT '描述',
    status       TINYINT      DEFAULT 1 COMMENT '状态 1启用 0停用',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '审批流程定义';

-- 审批节点定义
CREATE TABLE wf_process_node (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    def_id        BIGINT      NOT NULL  COMMENT '流程定义ID',
    node_name     VARCHAR(100) NOT NULL COMMENT '节点名称',
    node_type     VARCHAR(30)  NOT NULL COMMENT '节点类型 APPROVE/CC/SIGN/OR_SIGN/CONDITION',
    approver_type VARCHAR(30)  NOT NULL COMMENT '审批人类型 SPECIFIC_USER/DEPT_LEADER/ROLE',
    approver_id   BIGINT      DEFAULT NULL COMMENT '指定审批人ID(SPECIFIC_USER时)',
    approver_role VARCHAR(50) DEFAULT NULL COMMENT '指定角色(DEPT_LEADER/ROLE时)',
    condition_expr VARCHAR(500) DEFAULT NULL COMMENT '条件表达式(CONDITION节点)',
    sort_order    INT         DEFAULT 0 COMMENT '排序',
    create_time   DATETIME    DEFAULT CURRENT_TIMESTAMP
) COMMENT '审批节点定义';

-- 审批流程实例
CREATE TABLE wf_process_instance (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    def_id       BIGINT       NOT NULL  COMMENT '流程定义ID',
    def_name     VARCHAR(100) NOT NULL  COMMENT '流程名称快照',
    template_id  BIGINT       NOT NULL  COMMENT '表单模板ID',
    applicant_id BIGINT       NOT NULL  COMMENT '申请人ID',
    form_data    TEXT         DEFAULT NULL COMMENT '表单数据JSON',
    status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PENDING/APPROVING/PASSED/REJECTED/CANCELLED',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '审批流程实例';

-- 待审批任务
CREATE TABLE wf_task (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id     BIGINT      NOT NULL  COMMENT '流程实例ID',
    node_id         BIGINT      NOT NULL  COMMENT '当前节点ID',
    node_name       VARCHAR(100) NOT NULL COMMENT '节点名称',
    assignee_id     BIGINT      NOT NULL  COMMENT '当前审批人ID',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    create_time     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    complete_time   DATETIME    DEFAULT NULL
) COMMENT '审批任务';

-- 审批记录
CREATE TABLE wf_approval_record (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id  BIGINT       NOT NULL COMMENT '流程实例ID',
    node_id      BIGINT       NOT NULL COMMENT '节点ID',
    node_name    VARCHAR(100) NOT NULL COMMENT '节点名称快照',
    approver_id  BIGINT       NOT NULL COMMENT '审批人ID',
    action       VARCHAR(20)  NOT NULL COMMENT '操作 APPROVE/REJECT/TRANSFER',
    comment      VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    attachments  VARCHAR(1000) DEFAULT NULL COMMENT '附件路径(JSON数组)',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP
) COMMENT '审批记录';

-- ==================== 考勤管理模块 ====================

-- 打卡记录
CREATE TABLE att_clock_record (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL  COMMENT '用户ID',
    clock_type  VARCHAR(10) NOT NULL  COMMENT 'IN/OUT',
    clock_time  DATETIME    NOT NULL  COMMENT '打卡时间',
    ip_address  VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, clock_time)
) COMMENT '打卡记录';

-- 请假申请（关联审批流）
CREATE TABLE att_leave_request (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL COMMENT '申请人ID',
    instance_id BIGINT       NOT NULL COMMENT '关联审批实例ID',
    leave_type  VARCHAR(20)  NOT NULL COMMENT '年假/事假/病假/婚假/产假/其他',
    start_time  DATETIME     NOT NULL COMMENT '开始时间',
    end_time    DATETIME     NOT NULL COMMENT '结束时间',
    duration    DECIMAL(5,1) NOT NULL COMMENT '请假天数',
    reason      VARCHAR(500) DEFAULT NULL COMMENT '请假原因',
    status      VARCHAR(20)  DEFAULT 'PENDING' COMMENT '状态',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) COMMENT '请假申请';

-- 请假额度表
CREATE TABLE att_leave_quota (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    year          INT          NOT NULL COMMENT '年份',
    leave_type    VARCHAR(20)  NOT NULL COMMENT '假期类型',
    total_days    DECIMAL(5,1) NOT NULL COMMENT '总额度',
    used_days     DECIMAL(5,1) DEFAULT 0 COMMENT '已用天数',
    UNIQUE KEY uk_user_year_type (user_id, year, leave_type)
) COMMENT '请假额度';

-- ==================== 公告消息模块 ====================

-- 公告表
CREATE TABLE ntc_notice (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL  COMMENT '标题',
    content      TEXT         NOT NULL  COMMENT '内容(富文本HTML)',
    publisher_id BIGINT       NOT NULL  COMMENT '发布人ID',
    is_top       TINYINT      DEFAULT 0 COMMENT '是否置顶',
      scheduled_time DATETIME    DEFAULT NULL COMMENT '定时发布时间',
      attachment     VARCHAR(500) DEFAULT NULL COMMENT '附件路径(多个用逗号分隔)',
    status       TINYINT      DEFAULT 1 COMMENT '状态 1发布 0草稿',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '公告表';

-- 公告已读记录
CREATE TABLE ntc_notice_read (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_id  BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    read_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_notice_user (notice_id, user_id)
) COMMENT '公告已读记录';

-- 站内消息
CREATE TABLE ntc_message (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id   BIGINT       NOT NULL  COMMENT '发送者ID(0=系统)',
    receiver_id BIGINT       NOT NULL  COMMENT '接收者ID',
    title       VARCHAR(200) NOT NULL  COMMENT '标题',
    content     TEXT         NOT NULL  COMMENT '内容',
    msg_type    VARCHAR(30)  DEFAULT 'NOTICE' COMMENT 'NOTICE/APPROVAL/CHAT/SYSTEM',
    is_read     TINYINT      DEFAULT 0 COMMENT '是否已读',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) COMMENT '站内消息';

-- ==================== 日程任务模块 ====================

-- 日历事件
CREATE TABLE sch_calendar_event (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL  COMMENT '所属用户ID',
    title       VARCHAR(200) NOT NULL  COMMENT '事件标题',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    start_time  DATETIME     NOT NULL  COMMENT '开始时间',
    end_time    DATETIME     NOT NULL  COMMENT '结束时间',
    event_type  VARCHAR(20)  DEFAULT 'PERSONAL' COMMENT 'PERSONAL/MEETING/TASK',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '日历事件';

-- 会议
CREATE TABLE sch_meeting (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL  COMMENT '会议标题',
    room_id      BIGINT       DEFAULT NULL COMMENT '会议室ID',
    host_id      BIGINT       NOT NULL  COMMENT '发起人ID',
    start_time   DATETIME     NOT NULL  COMMENT '开始时间',
    end_time     DATETIME     NOT NULL  COMMENT '结束时间',
    description  VARCHAR(500) DEFAULT NULL COMMENT '会议描述',
    status       VARCHAR(20)  DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED/CANCELLED/FINISHED',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP
) COMMENT '会议表';

-- 会议参与人
CREATE TABLE sch_meeting_participant (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    UNIQUE KEY uk_meeting_user (meeting_id, user_id)
) COMMENT '会议参与人';

-- 任务
CREATE TABLE sch_task (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL  COMMENT '任务标题',
    description  VARCHAR(500) DEFAULT NULL COMMENT '描述',
    assigner_id  BIGINT       NOT NULL  COMMENT '分配人ID',
    assignee_id  BIGINT       NOT NULL  COMMENT '执行人ID',
    due_date     DATE         DEFAULT NULL COMMENT '截止日期',
    priority     VARCHAR(10)  DEFAULT 'NORMAL' COMMENT 'LOW/NORMAL/HIGH/URGENT',
    status       VARCHAR(20)  DEFAULT 'TODO' COMMENT 'TODO/IN_PROGRESS/DONE/CANCELLED',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '任务表';

-- ==================== 行政管理模块 ====================

-- 会议室
CREATE TABLE adm_meeting_room (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_name    VARCHAR(100) NOT NULL  COMMENT '会议室名称',
    location     VARCHAR(200) DEFAULT NULL COMMENT '位置',
    capacity     INT          NOT NULL  COMMENT '容纳人数',
    has_projector TINYINT     DEFAULT 0 COMMENT '有无投影仪',
    has_microphone TINYINT    DEFAULT 0 COMMENT '有无话筒',
    status       TINYINT      DEFAULT 1 COMMENT '状态 1可用 0不可用',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP
) COMMENT '会议室';

-- 会议室预约
CREATE TABLE adm_room_booking (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id     BIGINT      NOT NULL  COMMENT '会议室ID',
    meeting_id  BIGINT      DEFAULT NULL COMMENT '关联会议ID',
    user_id     BIGINT      NOT NULL  COMMENT '预约人ID',
    start_time  DATETIME    NOT NULL  COMMENT '开始时间',
    end_time    DATETIME    NOT NULL  COMMENT '结束时间',
    purpose     VARCHAR(500) DEFAULT NULL COMMENT '用途',
    status      VARCHAR(20) DEFAULT 'BOOKED' COMMENT 'BOOKED/CANCELLED',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP
) COMMENT '会议室预约';

-- 资产
CREATE TABLE adm_asset (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_name   VARCHAR(100) NOT NULL  COMMENT '资产名称',
    asset_code   VARCHAR(50)  NOT NULL UNIQUE COMMENT '资产编码',
    category     VARCHAR(50)  NOT NULL  COMMENT '分类',
    model        VARCHAR(100) DEFAULT NULL COMMENT '型号',
    dept_id      BIGINT       DEFAULT NULL COMMENT '所属部门ID',
    keeper_id    BIGINT       DEFAULT NULL COMMENT '保管人ID',
    status       VARCHAR(20)  DEFAULT 'IDLE' COMMENT 'IDLE/IN_USE/SCRAPPED',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '资产表';

-- 资产领用归还记录
CREATE TABLE adm_asset_record (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id    BIGINT      NOT NULL  COMMENT '资产ID',
    user_id     BIGINT      NOT NULL  COMMENT '领用人ID',
    action      VARCHAR(20) NOT NULL  COMMENT 'BORROW/RETURN',
    action_time DATETIME    NOT NULL  COMMENT '操作时间',
    remark      VARCHAR(300) DEFAULT NULL,
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP
) COMMENT '资产领用归还记录';

-- 车辆
CREATE TABLE adm_vehicle (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_number  VARCHAR(20)  NOT NULL UNIQUE COMMENT '车牌号',
    vehicle_model VARCHAR(100) DEFAULT NULL COMMENT '车型',
    seats         INT          DEFAULT 5 COMMENT '座位数',
    status        VARCHAR(20)  DEFAULT 'IDLE' COMMENT 'IDLE/IN_USE/MAINTENANCE',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP
) COMMENT '车辆表';

-- 车辆使用记录
CREATE TABLE adm_vehicle_record (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id  BIGINT      NOT NULL  COMMENT '车辆ID',
    user_id     BIGINT      NOT NULL  COMMENT '使用人ID',
    start_time  DATETIME    NOT NULL  COMMENT '出车时间',
    end_time    DATETIME    DEFAULT NULL COMMENT '归队时间',
    destination VARCHAR(200) DEFAULT NULL COMMENT '目的地',
    purpose     VARCHAR(300) DEFAULT NULL COMMENT '用途',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP
) COMMENT '车辆使用记录';

-- ==================== 即时通讯模块（预留） ====================

-- 会话表
CREATE TABLE im_conversation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_type VARCHAR(20) NOT NULL COMMENT 'PRIVATE/GROUP',
    group_name      VARCHAR(100) DEFAULT NULL COMMENT '群名称(GROUP时)',
    creator_id      BIGINT       DEFAULT NULL COMMENT '创建者ID',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP
) COMMENT '会话表(IM预留)';

-- 会话成员
CREATE TABLE im_conversation_member (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    join_time       DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_conv_user (conversation_id, user_id)
) COMMENT '会话成员(IM预留)';

-- 聊天消息
-- ==================== 系统审计日志 ====================

CREATE TABLE sys_audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL       COMMENT '操作人ID',
    username    VARCHAR(50)  DEFAULT NULL   COMMENT '操作人用户名',
    module      VARCHAR(50)  NOT NULL       COMMENT '操作模块',
    action      VARCHAR(50)  NOT NULL       COMMENT '操作动作',
    target      VARCHAR(200) DEFAULT NULL   COMMENT '操作目标',
    detail      VARCHAR(500) DEFAULT NULL   COMMENT '操作详情',
    ip_address  VARCHAR(50)  DEFAULT NULL   COMMENT '操作IP',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) COMMENT '系统审计日志';

CREATE TABLE im_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT      NOT NULL COMMENT '会话ID',
    sender_id       BIGINT      NOT NULL COMMENT '发送者ID',
    content_type    VARCHAR(20) DEFAULT 'TEXT' COMMENT 'TEXT/IMAGE/FILE',
    content         TEXT        NOT NULL  COMMENT '消息内容',
    send_time       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    status          VARCHAR(20) DEFAULT 'SENT' COMMENT 'SENT/DELIVERED/READ'
) COMMENT '聊天消息(IM预留)';


