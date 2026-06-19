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
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, position, hire_date, status) VALUES
(2, 'zhangjl', 'e10adc3949ba59abbe56e057f20f883e', '张经理', '13800000002', 'zhangjl@oa.com', 2, '部门经理', '2024-03-15', 1),
(3, 'lizj',    'e10adc3949ba59abbe56e057f20f883e', '李总监', '13800000003', 'lizj@oa.com',   1, '技术总监', '2023-06-01', 1),
(4, 'wanghr',  'e10adc3949ba59abbe56e057f20f883e', '王HR',   '13800000004', 'wanghr@oa.com', 5, '人事专员', '2024-09-01', 1),
(5, 'zhaocw',  'e10adc3949ba59abbe56e057f20f883e', '赵财务', '13800000005', 'zhaocw@oa.com', 4, '财务专员', '2024-06-15', 1),
(6, 'sunxz',   'e10adc3949ba59abbe56e057f20f883e', '孙行政', '13800000006', 'sunxz@oa.com',  5, '行政专员', '2024-09-01', 1);
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, position, hire_date, status) VALUES
(1, 'admin', '21232f297a57a5a743894a0e4a801fc3', '系统管理员', '13800000000', 'admin@oa.com', 1, '系统管理员', '2024-01-01', 1);

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
(14, 1, '数据导入', 'IMPORT', 'MENU', NULL, 4, 1),
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
(24, 2, '我的申请', 'MY_APPLICATIONS', 'MENU', NULL, 4, 1),
(25, 2, '审批模板', 'FORM_TEMPLATE', 'MENU', NULL, 5, 1),
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
-- ==================== 给部门主管分配菜单权限 ====================
-- 流程审批(全部子菜单) + 考勤管理(全部) + 公告消息(全部) + 日程任务(全部) + 统计大屏(全部) + 即时通讯(全部)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 21), (2, 22), (2, 23), (2, 24), (2, 25),
(2, 31), (2, 32), (2, 33),
(2, 41), (2, 42),
(2, 51), (2, 52), (2, 53),
(2, 71), (2, 72), (2, 73),
(2, 81), (2, 82);

-- ==================== 给普通员工分配菜单权限 ====================
-- 流程审批(发起+我的申请) + 考勤管理(打卡+请假) + 公告消息 + 日程任务(日程+会议) + 即时通讯
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 21), (3, 23),
(3, 31), (3, 32),
(3, 41), (3, 42),
(3, 51), (3, 53),
(3, 81), (3, 82);
(3, 14), (3, 24), (3, 25),

-- ==================== 给HR分配菜单权限 ====================
-- 流程审批(发起+我的申请) + 考勤管理(全部) + 公告消息 + 日程任务(日程+会议) + 统计大屏(概览+考勤) + 即时通讯
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 21), (4, 23),
(4, 31), (4, 32), (4, 33),
(4, 41), (4, 42),
(4, 51), (4, 53),
(4, 71), (4, 73),
(4, 81), (4, 82);
(4, 14), (4, 24), (4, 25),

-- ==================== 给财务/行政分配菜单权限 ====================
-- 流程审批(发起+我的申请+我的审批) + 考勤管理(打卡+请假) + 公告消息 + 日程任务(日程+会议) + 行政管理(全部) + 即时通讯
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 21), (5, 22), (5, 23),
(5, 31), (5, 32),
(5, 41), (5, 42),
(5, 51), (5, 53),
(5, 61), (5, 62), (5, 63),
(5, 81), (5, 82);
(5, 14), (5, 24),

-- ==================== 会议室: 5间 ====================

INSERT INTO adm_meeting_room (room_name, location, capacity, has_projector, has_microphone, status) VALUES
('大会议室',   '3楼301', 20, 1, 1, 1),
('小会议室A',  '2楼201', 8,  1, 0, 1),
('小会议室B',  '2楼202', 8,  0, 0, 1),
('洽谈室',    '1楼101', 4,  0, 0, 1),
('培训室',    '4楼401', 50, 1, 1, 1);

-- ==================== 车辆: 3辆 ====================

INSERT INTO adm_vehicle (plate_number, vehicle_model, seats, status) VALUES
('京A88888', '别克GL8',  7, 'IDLE'),
('京B66666', '大众帕萨特', 5, 'IDLE'),
('京C12345', '丰田凯美瑞', 5, 'IDLE');

-- ==================== 资产: 5项 ====================

INSERT INTO adm_asset (asset_name, asset_code, category, model, dept_id, keeper_id, status) VALUES
('ThinkPad笔记本', 'AST001', '电子设备', 'ThinkPad X1', 2, 2, 'IDLE'),
('投影仪',         'AST002', '电子设备', 'Epson CB-X51', 1, 1, 'IDLE'),
('办公桌',         'AST003', '家具',     '1.6m实木',    2, 2, 'IDLE'),
('打印机',         'AST004', '电子设备', 'HP M429fdw',  1, 1, 'IDLE'),
('碎纸机',         'AST005', '办公设备', '得力9934',    5, 4, 'IDLE');

-- ==================== 日程任务: 5个 ====================

INSERT INTO sch_task (title, description, assigner_id, assignee_id, due_date, priority, status) VALUES
('完成需求文档', '编写OA系统需求规格说明书', 1, 2, '2026-06-20', 'HIGH', 'IN_PROGRESS'),
('数据库设计评审', '评审schema.sql设计', 1, 3, '2026-06-18', 'URGENT', 'TODO'),
('前端UI联调', '各面板集成到MainFrame', 1, 4, '2026-06-22', 'NORMAL', 'TODO'),
('测试用例编写', '编写核心模块测试用例', 2, 4, '2026-06-25', 'NORMAL', 'TODO'),
('部署文档整理', '整理项目部署说明', 1, 2, '2026-06-30', 'LOW', 'TODO');

-- ==================== 会议: 2个 ====================

INSERT INTO sch_meeting (title, room_id, host_id, start_time, end_time, description, status) VALUES
('项目进度周会', 1, 1, '2026-06-20 10:00:00', '2026-06-20 11:00:00', '讨论本周开发进度和下周计划', 'SCHEDULED'),
('技术方案评审', 2, 3, '2026-06-21 14:00:00', '2026-06-21 16:00:00', '评审审批引擎技术方案', 'SCHEDULED');

INSERT INTO sch_meeting_participant (meeting_id, user_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),
(2, 1), (2, 3), (2, 4);

-- ==================== ????: 7? x 6??? = 42? ====================

INSERT INTO att_leave_quota (user_id, year, leave_type, total_days, used_days) VALUES
(1, 2026, '??', 10, 0), (1, 2026, '??', 5, 0), (1, 2026, '??', 5, 0), (1, 2026, '??', 3, 0), (1, 2026, '??', 15, 0), (1, 2026, '??', 3, 0),
(2, 2026, '??', 10, 0), (2, 2026, '??', 5, 0), (2, 2026, '??', 5, 0), (2, 2026, '??', 3, 0), (2, 2026, '??', 15, 0), (2, 2026, '??', 3, 0),
(3, 2026, '??', 10, 0), (3, 2026, '??', 5, 0), (3, 2026, '??', 5, 0), (3, 2026, '??', 3, 0), (3, 2026, '??', 15, 0), (3, 2026, '??', 3, 0),
(4, 2026, '??', 10, 0), (4, 2026, '??', 5, 0), (4, 2026, '??', 5, 0), (4, 2026, '??', 3, 0), (4, 2026, '??', 15, 0), (4, 2026, '??', 3, 0),
(5, 2026, '??', 10, 0), (5, 2026, '??', 5, 0), (5, 2026, '??', 5, 0), (5, 2026, '??', 3, 0), (5, 2026, '??', 15, 0), (5, 2026, '??', 3, 0),
(6, 2026, '??', 10, 0), (6, 2026, '??', 5, 0), (6, 2026, '??', 5, 0), (6, 2026, '??', 3, 0), (6, 2026, '??', 15, 0), (6, 2026, '??', 3, 0)
ON DUPLICATE KEY UPDATE total_days=VALUES(total_days);


