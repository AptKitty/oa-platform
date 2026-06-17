# OA协同办公平台 - 4人小组分工方案 (缩减版)

> 原7人小组，3人生病，压缩为4人。难度和工期不变，每人承担更多模块。

## 技术栈

| 层面 | 技术 |
|------|------|
| 语言 | Java 8 |
| GUI | Swing + FlatLaf 主题 |
| 数据库 | MySQL 8.0 + MyBatis 3.5 |
| 连接池 | HikariCP |
| 图表 | JFreeChart |
| 文件 | Apache POI (Excel) |
| IM预留 | Java-WebSocket + Jackson |

## 4人分工总览

| 组员 | 模块 | 难度 | 包路径 |
|------|------|------|--------|
| 1 | 系统管理 + 主界面 + IM预留 | **** | system + ui + im |
| 2 | 审批流程引擎 | ***** | workflow |
| 3 | 考勤管理 + 公告消息 | **** | attendance + notice |
| 4 | 日程行政 + 统计大屏 + 导入导出 | ***** | schedule + admin + statistics + ExportUtil |

---

### 组员1 - 系统管理 + 主界面框架 + IM预留
**难度**: **** (原A+G合并)  **文件**: 15+个  **最优先启动**

#### 系统管理 (原A)
- `system/service/UserService.java` - 完善登录 (MD5加密校验)
- `system/service/RoleService.java` - 已完成
- `system/dao/` - 全部接口已定义
- `resources/mapper/system/` - 全部已存在
- `ui/panel/UserManagePanel.java` - **[新建]** 用户增删改查
- `ui/panel/DeptManagePanel.java` - **[新建]** 部门树形管理
- `ui/panel/RolePermissionPanel.java` - **[新建]** 角色权限分配 (RBAC按钮级)
- **初始化数据**: admin/admin, 5个部门, 5种角色, 全部菜单项

#### 主界面框架 (原G)
- `ui/frame/MainFrame.java` - 侧边栏8按钮路由, 动态菜单, 状态栏
- `ui/frame/BaseFrame.java` - 已完成
- `ui/panel/BasePanel.java` - 已完成 (所有面板基类)
- `ui/panel/WelcomePanel.java` - **[新建]** 欢迎页 (待办摘要+快捷入口)
- 提供 `registerPanel(key, panel)` 让其他组员注册
- 右上角未读消息角标

#### IM预留 (原G)
- `im/websocket/ImWebSocketServer.java` - 完善占位框架
- `ui/panel/ImPanel.java` - **[新建]** IM面板占位
- `Application.java` 启动时可选启动WebSocket (端口8887)

**你是统筹者** - 其他3位组员开发完面板后，你来整合注册。

---

### 组员2 - 审批流程引擎 (核心难点，独立承担)
**难度**: *****  **文件**: 8+个  **全组最难点，集中精力**

#### 核心逻辑
- `workflow/service/WorkflowService.java` - 实现审批引擎 (TODO已标注)
  - `submitProcess()` → 创建首节点审批任务
  - `approve()` → 验证审批人 → 记录 → 下一节点
    - **会签(SIGN)**: 全员通过
    - **或签(OR_SIGN)**: 任一人通过
    - **条件分支(CONDITION)**: 根据表单数据路由
    - **抄送(CC)**: 不阻塞流程
  - `reject()` → 驳回 (状态→REJECTED)

#### DAO层 - 全部接口已定义
- `workflow/dao/ProcessInstanceDao.java`
- `workflow/dao/FormTemplateDao.java`
- `workflow/dao/ProcessDefinitionDao.java`

#### Mapper XML - 全部已存在
- `resources/mapper/workflow/ProcessInstanceMapper.xml` - 需完善
- `resources/mapper/workflow/FormTemplateMapper.xml`
- `resources/mapper/workflow/ProcessDefinitionMapper.xml`
- `resources/mapper/workflow/ApprovalRecordMapper.xml`

#### UI面板 (全部新建)
- `ui/panel/FormTemplatePanel.java` - 表单模板设计 (拖拽字段)
- `ui/panel/ProcessDefPanel.java` - 审批链可视化配置
- `ui/panel/ApplyPanel.java` - 发起审批 (动态表单渲染)
- `ui/panel/ApprovalPanel.java` - 审批处理 (时间线展示)
- **初始化**: 6个预设模板 (请假/报销/出差/加班/用章/采购), 每模板5+字段

---

### 组员3 - 考勤管理 + 公告消息 (双模块)
**难度**: **** (原C+D合并)  **文件**: 10+个

#### 考勤管理 (原C)
- `attendance/service/AttendanceService.java` - 打卡/统计
- `attendance/dao/AttendanceDao.java` - 已定义
- `attendance/dao/LeaveDao.java` - 已定义
- `resources/mapper/attendance/AttendanceMapper.xml` - 已存在
- `ui/panel/ClockPanel.java` - **[新建]** 上班/下班打卡
- `ui/panel/LeavePanel.java` - **[新建]** 请假申请
- `ui/panel/AttendanceStatPanel.java` - **[新建]** 月度考勤统计
- 防重复打卡, 迟到判定, 请假额度扣减, Excel导出

#### 公告消息 (原D)
- `notice/service/NoticeService.java` - 已完成
- `notice/service/MessageService.java` - 已完成
- `notice/dao/NoticeDao.java` - 已定义
- `notice/dao/MessageDao.java` - 已定义
- `resources/mapper/notice/NoticeMapper.xml` - 已存在
- `resources/mapper/notice/MessageMapper.xml` - 已存在
- `ui/panel/NoticePanel.java` - **[新建]** 公告发布/详情
- `ui/panel/MessagePanel.java` - **[新建]** 消息中心
- 已读追踪 (notice_read_record表), 未读角标, 批量标记

---

### 组员4 - 日程行政 + 统计大屏 + 导入导出 (三模块，体量最大)
**难度**: *****  **文件**: 14+个  **面板最多，但逻辑相对独立**

#### 日程任务 (原E上半)
- `schedule/service/ScheduleService.java` - 已完成
- `schedule/dao/ScheduleDao.java` - 已定义
- `resources/mapper/schedule/ScheduleMapper.xml` - 已存在
- `ui/panel/CalendarPanel.java` - **[新建]** 月/周/日日历视图
- `ui/panel/MeetingPanel.java` - **[新建]** 会议管理+时间冲突检测
- `ui/panel/TaskPanel.java` - **[新建]** 任务分配与跟踪

#### 行政管理 (原E下半)
- `admin/service/AdminService.java` - 已完成
- `admin/dao/AdminDao.java` - 统一DAO (会议室/资产/车辆)
- `resources/mapper/admin/AssetMapper.xml` - 刚创建
- `ui/panel/MeetingRoomPanel.java` - **[新建]** 会议室预约+冲突检测
- `ui/panel/AssetPanel.java` - **[新建]** 资产登记/领用/归还/报废
- `ui/panel/VehiclePanel.java` - **[新建]** 车辆出车/归队

#### 统计大屏 (原F)
- `statistics/service/StatService.java` - 已完成
- `statistics/dao/StatDao.java` - 6个统计查询
- `statistics/entity/StatResultVO.java` - 通用统计VO
- `resources/mapper/statistics/StatMapper.xml` - 刚创建
- `ui/panel/StatOverviewPanel.java` - **[新建]** 4个概览卡片
- `ui/panel/ApprovalStatPanel.java` - **[新建]** 审批效率柱状图
- `ui/panel/AttendanceStatChartPanel.java` - **[新建]** 出勤对比+请假饼图

#### 导入导出通用工具
- `common/ExportUtil.java` - 已完成 (Excel导入导出)
- `ui/panel/ImportPanel.java` - **[新建]** 批量导入用户/员工

---

## 模块接口契约 (关键!)

### BasePanel 规范 (组员1已建好)
所有面板必须继承 `BasePanel`，实现:
- `getPanelKey()` → 返回注册key (如 `"WORKFLOW"`)
- `getPanelTitle()` → 返回面板标题
- 通过 `setCurrentUser(userId, username)` 获取登录态
- 使用 `showError()` / `showInfo()` / `confirm()` 统一弹窗
- 使用 `createTable(columns)` / `createToolBar(onRefresh, onAdd, onExport)` 统一组件

### 注册方式 (组员1协调)
```java
// 所有组员开发的面板，在组员1的 MainFrame.initUI() 中注册:
mainFrame.registerPanel("SYSTEM",     new UserManagePanel());    // 组员1
mainFrame.registerPanel("WORKFLOW",   new ApplyPanel());         // 组员2
mainFrame.registerPanel("ATTENDANCE", new ClockPanel());         // 组员3
mainFrame.registerPanel("NOTICE",     new NoticePanel());        // 组员3
mainFrame.registerPanel("SCHEDULE",   new CalendarPanel());      // 组员4
mainFrame.registerPanel("ADMIN",      new MeetingRoomPanel());   // 组员4
mainFrame.registerPanel("STATISTICS", new StatOverviewPanel());  // 组员4
mainFrame.registerPanel("IM",         new ImPanel());            // 组员1
```

---

## 开发顺序 (6周)

```
第1周:
  组员1: 登录+用户管理+MainFrame框架  (最优先)
  组员2: 熟悉审批实体, DAO, 开始submitProcess
  组员3: 考勤打卡+请假界面
  组员4: 日程日历+会议室预约

第2周:
  组员1: 部门+角色权限, WelcomePanel, IM占位
  组员2: approve()核心: 会签/或签/条件分支逻辑
  组员3: 考勤统计+公告发布
  组员4: 资产车辆+任务面板

第3周:
  组员1: 整合各面板注册, 侧边栏路由切换
  组员2: reject()驳回 + 审批时间线UI
  组员3: 消息中心+未读角标
  组员4: 统计大屏4个图表 (JFreeChart)

第4周:
  组员1: IM WebSocket骨架 + 动态菜单权限
  组员2: 6个预设模板初始化 + 联调测试
  组员3: 请假审批联动(调组员2接口)+Excel导出
  组员4: 导入面板 + 全部图表联调

第5周:
  全员集成测试, 串流程:
    登录 -> 发起请假 -> 审批 -> 打卡 -> 查看统计
  各组员插入5+条基础数据

第6周:
  演示准备, Bug修复, 论文撰写
```

---

## 数据库

执行 `resources/db/schema.sql` 创建所有表
连接: `jdbc:mysql://localhost:3306/oa_platform` (root/root)
每组员至少插入5条有意义的基础数据
