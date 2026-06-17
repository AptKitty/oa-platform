-- ============================================================
-- init_data.sql — 审批流程引擎初始化数据
-- ============================================================
-- 作用：为 OA 协同办公平台预置 6 套完整的审批流程（模板 + 字段 + 流程定义 + 节点）
-- 执行顺序：必须在 schema.sql 之后执行（表结构已建好才能插入数据）
-- 数据概览：
--   6 个表单模板      （请假/报销/出差/加班/用章/采购）
--   30 个表单字段      （每个模板 5 个字段）
--   6 个流程定义      （每个模板对应 1 个流程定义）
--   13 个审批节点      （每个流程 1~3 个审批步骤）
-- 注意：
--   - sys_user 表的数据由成员1负责插入，此处不包含用户数据
--   - 节点中的 approver_id=2,3,4,5,6 是占位值，需根据实际用户 ID 调整
--   - 当前仅支持 SPECIFIC_USER（指定人）审批人类型
-- 编写：成员2
-- ============================================================

-- ============================================================
-- 第一部分：6 个表单模板
-- 每个模板决定"一种申请单需要填哪些字段"
-- 模板编码(template_code)用于程序内部识别，不可重复
-- status=1 表示启用，status=0 表示停用
-- ============================================================
INSERT INTO wf_form_template (template_name, template_code, description, category, status) VALUES
('请假申请', 'LEAVE', '员工请假审批模板', '请假', 1),
('报销申请', 'EXPENSE', '费用报销审批模板', '报销', 1),
('出差申请', 'TRAVEL', '出差审批模板', '出差', 1),
('加班申请', 'OVERTIME', '加班审批模板', '加班', 1),
('用章申请', 'SEAL', '印章使用审批模板', '用章', 1),
('采购申请', 'PURCHASE', '采购审批模板', '采购', 1);

-- ============================================================
-- 第二部分：30 个表单字段（每个模板 5 个字段）
-- 字段决定"申请单上有哪些输入框"
-- field_type 类型：TEXT=文本框 / NUMBER=数字 / DATE=日期 / SELECT=下拉 / TEXTAREA=多行文本
-- is_required: 0=选填 / 1=必填
-- options: 仅 SELECT 类型使用，JSON数组格式，其他类型填 NULL
-- sort_order: 字段在表单上的显示顺序，值小的排前面
-- ============================================================

-- ===== 一、请假申请字段（模板ID=1）=====
-- 需填：请假类型、开始/结束日期、天数、原因
INSERT INTO wf_form_field (template_id, field_name, field_label, field_type, is_required, options, sort_order) VALUES
(1, 'leaveType',  '请假类型', 'SELECT', 1, '["事假","病假","年假","婚假","产假","调休"]', 1),
(1, 'startDate',  '开始日期', 'DATE',   1, NULL, 2),
(1, 'endDate',    '结束日期', 'DATE',   1, NULL, 3),
(1, 'days',       '请假天数', 'NUMBER', 1, NULL, 4),
(1, 'reason',     '请假原因', 'TEXTAREA', 1, NULL, 5);

-- ===== 二、报销申请字段（模板ID=2）=====
-- 需填：报销类型、金额、日期、发票号(选填)、说明
INSERT INTO wf_form_field (template_id, field_name, field_label, field_type, is_required, options, sort_order) VALUES
(2, 'expenseType', '报销类型', 'SELECT', 1, '["差旅费","办公用品","招待费","交通费","通讯费","其他"]', 1),
(2, 'amount',      '报销金额', 'NUMBER', 1, NULL, 2),
(2, 'expenseDate', '费用日期', 'DATE',   1, NULL, 3),
(2, 'invoiceNo',  '发票号码', 'TEXT',   0, NULL, 4),
(2, 'description','费用说明', 'TEXTAREA', 1, NULL, 5);

-- ===== 三、出差申请字段（模板ID=3）=====
-- 需填：目的地、开始/结束日期、同行人员(选填)、事由
INSERT INTO wf_form_field (template_id, field_name, field_label, field_type, is_required, options, sort_order) VALUES
(3, 'destination','出差目的地', 'TEXT',   1, NULL, 1),
(3, 'startDate',  '开始日期',  'DATE',   1, NULL, 2),
(3, 'endDate',    '结束日期',  'DATE',   1, NULL, 3),
(3, 'companions', '同行人员',  'TEXT',   0, NULL, 4),
(3, 'purpose',    '出差事由',  'TEXTAREA', 1, NULL, 5);

-- ===== 四、加班申请字段（模板ID=4）=====
-- 需填：加班日期、开始/结束时间、时长、原因
INSERT INTO wf_form_field (template_id, field_name, field_label, field_type, is_required, options, sort_order) VALUES
(4, 'overtimeDate','加班日期', 'DATE',   1, NULL, 1),
(4, 'startTime',  '开始时间',  'TEXT',   1, NULL, 2),
(4, 'endTime',    '结束时间',  'TEXT',   1, NULL, 3),
(4, 'hours',      '加班时长',  'NUMBER', 1, NULL, 4),
(4, 'reason',     '加班原因',  'TEXTAREA', 1, NULL, 5);

-- ===== 五、用章申请字段（模板ID=5）=====
-- 需填：印章类型、文件名称、份数、事由、归还日期(选填)
INSERT INTO wf_form_field (template_id, field_name, field_label, field_type, is_required, options, sort_order) VALUES
(5, 'sealType',   '印章类型', 'SELECT', 1, '["公章","合同章","财务章","法人章","其他"]', 1),
(5, 'docName',    '文件名称', 'TEXT',   1, NULL, 2),
(5, 'copies',     '用章份数', 'NUMBER', 1, NULL, 3),
(5, 'purpose',    '用章事由', 'TEXTAREA', 1, NULL, 4),
(5, 'returnDate', '归还日期', 'DATE',   0, NULL, 5);

-- ===== 六、采购申请字段（模板ID=6）=====
-- 需填：物品名称、数量、预算金额、规格型号(选填)、理由
INSERT INTO wf_form_field (template_id, field_name, field_label, field_type, is_required, options, sort_order) VALUES
(6, 'itemName',   '物品名称', 'TEXT',   1, NULL, 1),
(6, 'quantity',   '数量',     'NUMBER', 1, NULL, 2),
(6, 'budget',     '预算金额', 'NUMBER', 1, NULL, 3),
(6, 'specs',      '规格型号', 'TEXT',   0, NULL, 4),
(6, 'reason',     '采购理由', 'TEXTAREA', 1, NULL, 5);

-- ============================================================
-- 第三部分：6 个流程定义
-- 每个流程定义决定"一种申请走几个审批步骤"
-- 一个模板可以对应一个或多个流程定义
-- ============================================================

-- ===== 流程一：请假审批流程（关联模板ID=1）=====
INSERT INTO wf_process_definition (def_name, def_code, template_id, description, status) VALUES
('请假标准流程', 'LEAVE_FLOW', 1, '请假标准审批流程', 1);

-- ===== 流程二：报销审批流程（关联模板ID=2）=====
INSERT INTO wf_process_definition (def_name, def_code, template_id, description, status) VALUES
('报销标准流程', 'EXPENSE_FLOW', 2, '报销标准审批流程', 1);

-- ===== 流程三：出差审批流程（关联模板ID=3）=====
INSERT INTO wf_process_definition (def_name, def_code, template_id, description, status) VALUES
('出差标准流程', 'TRAVEL_FLOW', 3, '出差标准审批流程', 1);

-- ===== 流程四：加班审批流程（关联模板ID=4）=====
INSERT INTO wf_process_definition (def_name, def_code, template_id, description, status) VALUES
('加班标准流程', 'OVERTIME_FLOW', 4, '加班标准审批流程', 1);

-- ===== 流程五：用章审批流程（关联模板ID=5）=====
INSERT INTO wf_process_definition (def_name, def_code, template_id, description, status) VALUES
('用章标准流程', 'SEAL_FLOW', 5, '用章标准审批流程', 1);

-- ===== 流程六：采购审批流程（关联模板ID=6）=====
INSERT INTO wf_process_definition (def_name, def_code, template_id, description, status) VALUES
('采购标准流程', 'PURCHASE_FLOW', 6, '采购标准审批流程', 1);

-- ============================================================
-- 第四部分：13 个审批节点（每个流程 1~3 个节点）
-- 每个节点代表审批链上的一个步骤，按 sort_order 排序
-- 节点字段说明：
--   def_id         — 所属流程定义ID（外键）
--   node_name      — 节点显示名称，如"部门经理审批"
--   node_type      — 审批类型 ↓
--       APPROVE    普通审批   一人批了就行
--       SIGN       会签       全部人批了才算通过
--       OR_SIGN    或签       任意一人批了就行
--       CC         抄送       通知但不阻塞流程
--       CONDITION  条件分支   根据条件选择走哪条路线
--   approver_type  — 审批人类型 ↓
--       SPECIFIC_USER  指定用户  直接用 approver_id
--       DEPT_LEADER    部门负责人 查 sys_dept 表动态查找
--       ROLE           角色      查 sys_user_role 表动态查找
--   approver_id    — 审批人ID，仅在 SPECIFIC_USER 时有效
--   sort_order     — 排序，第1步=1，第2步=2...
-- 注意：approver_id=2,3,4,5,6 是占位 ID，需根据 sys_user 表实际数据调整
-- ============================================================

-- ===== 请假流程节点（def_id=1）：3 个节点 =====
-- 第1步：部门经理审批（单人批）→ 第2步：总监审批（会签，需全员通过）→ 第3步：HR抄送（不阻塞）
INSERT INTO wf_process_node (def_id, node_name, node_type, approver_type, approver_id, sort_order) VALUES
(1, '部门经理审批', 'APPROVE', 'SPECIFIC_USER', 2, 1),
(1, '总监审批',     'SIGN',    'SPECIFIC_USER', 3, 2),
(1, 'HR备案',       'CC',      'SPECIFIC_USER', 4, 3);

-- ===== 报销流程节点（def_id=2）：2 个节点 =====
-- 第1步：部门经理审批 → 第2步：财务审批
INSERT INTO wf_process_node (def_id, node_name, node_type, approver_type, approver_id, sort_order) VALUES
(2, '部门经理审批', 'APPROVE', 'SPECIFIC_USER', 2, 1),
(2, '财务审批',     'APPROVE', 'SPECIFIC_USER', 5, 2);

-- ===== 出差流程节点（def_id=3）：2 个节点 =====
-- 第1步：部门经理审批 → 第2步：总监审批
INSERT INTO wf_process_node (def_id, node_name, node_type, approver_type, approver_id, sort_order) VALUES
(3, '部门经理审批', 'APPROVE', 'SPECIFIC_USER', 2, 1),
(3, '总监审批',     'APPROVE', 'SPECIFIC_USER', 3, 2);

-- ===== 加班流程节点（def_id=4）：1 个节点 =====
-- 只有1步：部门经理审批（最简单的流程）
INSERT INTO wf_process_node (def_id, node_name, node_type, approver_type, approver_id, sort_order) VALUES
(4, '部门经理审批', 'APPROVE', 'SPECIFIC_USER', 2, 1);

-- ===== 用章流程节点（def_id=5）：2 个节点 =====
-- 第1步：部门经理审批 → 第2步：行政审批
INSERT INTO wf_process_node (def_id, node_name, node_type, approver_type, approver_id, sort_order) VALUES
(5, '部门经理审批', 'APPROVE', 'SPECIFIC_USER', 2, 1),
(5, '行政审批',     'APPROVE', 'SPECIFIC_USER', 6, 2);

-- ===== 采购流程节点（def_id=6）：3 个节点 =====
-- 最复杂的三级审批：部门经理 → 财务 → 总监
INSERT INTO wf_process_node (def_id, node_name, node_type, approver_type, approver_id, sort_order) VALUES
(6, '部门经理审批', 'APPROVE', 'SPECIFIC_USER', 2, 1),
(6, '财务审批',     'APPROVE', 'SPECIFIC_USER', 5, 2),
(6, '总监审批',     'APPROVE', 'SPECIFIC_USER', 3, 3);

-- ============================================================
-- 初始化数据加载完成
-- 共插入：6 模板 + 30 字段 + 6 流程 + 13 节点 = 55 条记录
-- ============================================================