-- ===================================================
-- OA协同办公平台 初始化数据
-- ===================================================

USE oa_platform;

-- ==================== 部门: 5个 ====================

INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, sort_order, status) VALUES
(1, 0, '总经办',     'ZJB',  1, 1),
(2, 0, '技术部',     'JSB',  2, 1),
(3, 0, '市场部',     'SCB',  3, 1),
(4, 0, '财务部',     'CWB',  4, 1),
(5, 0, '人事行政部', 'RSB',  5, 1);

-- ==================== 用户: admin/admin ====================
-- 密码 MD5("admin") = 21232f297a57a5a743894a0e4a801fc3

-- 补充审批人用户（对应 init_data.sql 中的 approver_id=2~6，密码均为 123456）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, position, status) VALUES
(2, 'zhangjl', 'e10adc3949ba59abbe56e057f20f883e', '张经理', '13800000002', 'zhangjl@oa.com', 2, '部门经理', 1),
(3, 'lizj',    'e10adc3949ba59abbe56e057f20f883e', '李总监', '13800000003', 'lizj@oa.com',   1, '技术总监', 1),
(4, 'wanghr',  'e10adc3949ba59abbe56e057f20f883e', '王HR',   '13800000004', 'wanghr@oa.com', 5, '人事专员', 1),
(5, 'zhaocw',  'e10adc3949ba59abbe56e057f20f883e', '赵财务', '13800000005', 'zhaocw@oa.com', 4, '财务专员', 1),
(6, 'sunxz',   'e10adc3949ba59abbe56e057f20f883e', '孙行政', '13800000006', 'sunxz@oa.com',  5, '行政专员', 1);
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, position, status) VALUES
(1, 'admin', '21232f297a57a5a743894a0e4a801fc3', '系统管理员', '13800000000', 'admin@oa.com', 1, '系统管理员', 1);

-- ==================== 角色: 5种 ====================

INSERT INTO sys_role (id, role_name, role_code, description, status) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '系统最高权限，管理所有模块', 1),
(2, '部门经理',   'DEPT_MANAGER', '部门管理、审批流程、考勤查看', 1),
(3, '普通员工',   'EMPLOYEE',     '发起审批、打卡、查看公告', 1),
(4, '人事专员',   'HR',           '考勤管理、人事行政', 1),
(5, '财务专员',   'FINANCE',      '财务审批、报销管理', 1);

-- ==================== 给 admin 分配超级管理员角色 ====================

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 给审批人用户分配角色（对应 init_data.sql 的审批节点）
INSERT INTO sys_user_role (user_id, role_id) VALUES
(2, 2),  -- 张经理 → 部门经理
(3, 2),  -- 李总监 → 部门经理
(4, 4),  -- 王HR   → 人事专员
(5, 5),  -- 赵财务 → 财务专员
(6, 4);  -- 孙行政 → 人事专员
-- ==================== 菜单权限: 全部 ====================
-- 一级菜单 (parent_id=0)

INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, icon, sort_order, status) VALUES
(1,  0, '系统管理', 'SYSTEM',      'MENU', '⚙', 1, 1),
(2,  0, '审批流程', 'WORKFLOW',    'MENU', '📋', 2, 1),
(3,  0, '考勤管理', 'ATTENDANCE',  'MENU', '🕐', 3, 1),
(4,  0, '公告消息', 'NOTICE',      'MENU', '📢', 4, 1),
(5,  0, '日程任务', 'SCHEDULE',    'MENU', '📅', 5, 1),
(6,  0, '行政管理', 'ADMIN',       'MENU', '🏢', 6, 1),
(7,  0, '统计大屏', 'STATISTICS',  'MENU', '📊', 7, 1),
(8,  0, '即时通讯', 'IM',          'MENU', '💬', 8, 1);

-- 系统管理 子菜单 + 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, icon, sort_order, status) VALUES
(11, 1, '用户管理',     'USER_MANAGE',      'MENU',   NULL, 1, 1),
(12, 1, '部门管理',     'DEPT_MANAGE',      'MENU',   NULL, 2, 1),
(13, 1, '角色权限',     'ROLE_PERMISSION',  'MENU',   NULL, 3, 1),
(101,11,'新增用户',     'USER_ADD',         'BUTTON', NULL, 1, 1),
(102,11,'编辑用户',     'USER_EDIT',        'BUTTON', NULL, 2, 1),
(103,11,'删除用户',     'USER_DELETE',      'BUTTON', NULL, 3, 1),
(104,12,'新增部门',     'DEPT_ADD',         'BUTTON', NULL, 1, 1),
(105,12,'编辑部门',     'DEPT_EDIT',        'BUTTON', NULL, 2, 1),
(106,12,'删除部门',     'DEPT_DELETE',      'BUTTON', NULL, 3, 1),
(107,13,'分配权限',     'ROLE_ASSIGN',      'BUTTON', NULL, 1, 1);

-- 审批流程 子菜单 + 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, icon, sort_order, status) VALUES
(21, 2, '发起审批',     'WF_START',         'MENU',   NULL, 1, 1),
(22, 2, '我的审批',     'WF_MY_APPROVAL',   'MENU',   NULL, 2, 1),
(23, 2, '流程定义',     'WF_DEFINITION',    'MENU',   NULL, 3, 1),
(201,21,'提交申请',     'WF_SUBMIT',        'BUTTON', NULL, 1, 1),
(202,22,'审批通过',     'WF_APPROVE',       'BUTTON', NULL, 1, 1),
(203,22,'审批拒绝',     'WF_REJECT',        'BUTTON', NULL, 2, 1);

-- 考勤管理 子菜单 + 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, icon, sort_order, status) VALUES
(31, 3, '打卡签到',     'ATT_CLOCK',        'MENU',   NULL, 1, 1),
(32, 3, '请假申请',     'ATT_LEAVE',        'MENU',   NULL, 2, 1),
(33, 3, '考勤统计',     'ATT_STAT',         'MENU',   NULL, 3, 1),
(301,31,'签到',         'ATT_CLOCK_IN',     'BUTTON', NULL, 1, 1),
(302,31,'签退',         'ATT_CLOCK_OUT',    'BUTTON', NULL, 2, 1);

-- 公告消息 子菜单 + 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, icon, sort_order, status) VALUES
(41, 4, '公告列表',     'NOTICE_LIST',      'MENU',   NULL, 1, 1),
(42, 4, '发布公告',     'NOTICE_PUBLISH',   'MENU',   NULL, 2, 1),
(43, 4, '我的消息',     'NOTICE_MY_MSG',    'MENU',   NULL, 3, 1),
(401,42,'发布',         'NOTICE_SUBMIT',    'BUTTON', NULL, 1, 1);

-- 日程任务 子菜单 + 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, icon, sort_order, status) VALUES
(51, 5, '日历视图',     'SCH_CALENDAR',     'MENU',   NULL, 1, 1),
(52, 5, '任务列表',     'SCH_TASK',         'MENU',   NULL, 2, 1),
(53, 5, '会议管理',     'SCH_MEETING',      'MENU',   NULL, 3, 1),
(501,52,'新增任务',     'SCH_TASK_ADD',     'BUTTON', NULL, 1, 1);

-- 行政管理 子菜单 + 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, icon, sort_order, status) VALUES
(61, 6, '会议室管理',   'ADM_ROOM',         'MENU',   NULL, 1, 1),
(62, 6, '资产管理',     'ADM_ASSET',        'MENU',   NULL, 2, 1),
(63, 6, '车辆管理',     'ADM_VEHICLE',      'MENU',   NULL, 3, 1);

-- 统计大屏 子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, icon, sort_order, status) VALUES
(71, 7, '数据概览',     'STAT_OVERVIEW',    'MENU',   NULL, 1, 1),
(72, 7, '审批统计',     'STAT_WF',          'MENU',   NULL, 2, 1),
(73, 7, '考勤统计',     'STAT_ATT',         'MENU',   NULL, 3, 1);

-- 即时通讯 子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, icon, sort_order, status) VALUES
(81, 8, '聊天窗口',     'IM_CHAT',          'MENU',   NULL, 1, 1),
(82, 8, '通讯录',       'IM_CONTACTS',      'MENU',   NULL, 2, 1);

-- ==================== 给超级管理员分配所有菜单权限 ====================

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;
