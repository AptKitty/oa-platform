# OA Collaborative Office Platform / OA协同办公平台

> Enterprise collaborative office & process approval system — Professional Production Labor course design
> 企业协同办公与流程审批系统 — 专业生产劳动课程设计

## Tech Stack / 技术栈

![Java](https://img.shields.io/badge/Java-8-brightgreen)
![Swing](https://img.shields.io/badge/GUI-Swing%20%2B%20FlatLaf-blue)
![MyBatis](https://img.shields.io/badge/ORM-MyBatis%203.5-red)
![MySQL](https://img.shields.io/badge/DB-MySQL%208.0-orange)
![JFreeChart](https://img.shields.io/badge/Chart-JFreeChart-yellow)

## Feature Modules / 功能模块

| Module / 模块 | Description / 说明 |
|------|------|
| 🏢 System Management / 系统管理 | Users / Departments / Roles / RBAC permissions (button-level) <br> 用户/部门/角色/RBAC权限 (按钮级) |
| 🔄 Workflow Engine / 审批流程引擎 | Configurable form templates + dynamic approval chains (countersign / or-sign / conditional branches / CC) <br> 可配置表单模板 + 动态审批链 (会签/或签/条件分支/抄送) |
| 📋 Attendance / 考勤管理 | Clock-in/out, leave quota management, monthly statistics <br> 上班/下班打卡, 请假额度管理, 月度统计 |
| 📢 Notices & Messages / 公告消息 | Announcement publishing / read tracking, in-site message center <br> 公告发布/已读追踪, 站内消息中心 |
| 📅 Schedule & Tasks / 日程任务 | Calendar view, meeting management + conflict detection, task assignment tracking <br> 日历视图, 会议管理+冲突检测, 任务分配跟踪 |
| 🏛 Admin Management / 行政管理 | Meeting room booking, asset check-out/return, vehicle management <br> 会议室预约, 资产领用/归还, 车辆管理 |
| 📊 Statistics Dashboard / 统计大屏 | Approval efficiency / attendance comparison / leave distribution (JFreeChart) <br> 审批效率/考勤对比/请假分布 (JFreeChart图表) |
| 💬 Instant Messaging / 即时通讯 | WebSocket communication (reserved) <br> WebSocket通信 (预留) |

## Quick Start / 快速开始

### Prerequisites / 环境要求

- JDK 8+
- MySQL 8.0+
- Maven 3.6+

### 1. Create Database / 创建数据库

```sql
-- Run schema.sql / 执行 schema.sql
mysql -u root -p < src/main/resources/db/schema.sql
```

### 2. Configure Database Connection / 修改数据库连接

Edit `src/main/resources/mybatis-config.xml` / 编辑 `src/main/resources/mybatis-config.xml`:

```xml
<property name="url" value="jdbc:mysql://localhost:3306/oa_platform?..."/>
<property name="username" value="your-username / 你的用户名"/>
<property name="password" value="your-password / 你的密码"/>
```

### 3. Run / 运行

```bash
mvn compile exec:java -Dexec.mainClass="com.oa.Application"
```

Or run `com.oa.Application.main()` directly in your IDE / 或在 IDE 中直接运行 `com.oa.Application.main()`

### 4. Login / 登录

Default admin account: `admin` / `admin` (takes effect after Member A initializes data)
默认管理员账号: `admin` / `admin` (组员A初始化后生效)

## Project Structure / 项目结构

```
oa-platform
├── pom.xml                          Maven configuration / Maven配置
├── TEAM_DIVISION.md                 Team division doc / 团队分工文档
├── src/main/java/com/oa/
│   ├── Application.java            Entry point / 入口
│   ├── common/                      Base layer (BaseEntity, ExportUtil, MyBatisUtil...) / 基础层
│   ├── system/                      System management (users / depts / roles / menus) / 系统管理
│   ├── workflow/                    Workflow engine (form templates / process defs / instances / approval records) / 审批引擎
│   ├── attendance/                  Attendance (clock-in / leave) / 考勤管理
│   ├── notice/                      Notices & messages (announcements / in-site messages) / 公告消息
│   ├── schedule/                    Schedule & tasks (calendar / meetings / tasks) / 日程任务
│   ├── admin/                       Admin management (meeting rooms / assets / vehicles) / 行政管理
│   ├── statistics/                  Statistics dashboard (JFreeChart) / 统计大屏
│   ├── im/                          Instant messaging (WebSocket reserved) / 即时通讯
│   └── ui/
│       ├── frame/                   BaseFrame / LoginFrame / MainFrame
│       └── panel/                   Module panels (BasePanel base class) / 各模块面板
└── src/main/resources/
    ├── db/schema.sql                Database DDL (24 tables) / 数据库DDL (24张表)
    ├── mybatis-config.xml           MyBatis configuration / MyBatis配置
    └── mapper/                      16 Mapper XMLs / 16个Mapper XML
```

## 4-Person Division / 4人分工

| Member / 组员 | Module / 模块 | Difficulty / 难度 |
|------|------|------|
| Member 1 / 组员1 | System Management + Main UI + IM (reserved) / 系统管理 + 主界面 + IM预留 | ⭐⭐⭐⭐ |
| Member 2 / 组员2 | Workflow Engine / 审批流程引擎 | ⭐⭐⭐⭐⭐ |
| Member 3 / 组员3 | Attendance + Notices & Messages / 考勤管理 + 公告消息 | ⭐⭐⭐⭐ |
| Member 4 / 组员4 | Schedule & Admin + Statistics Dashboard + Import/Export / 日程行政 + 统计大屏 + 导入导出 | ⭐⭐⭐⭐⭐ |

See [TEAM_DIVISION.md](./TEAM_DIVISION.md) for details / 详见 [TEAM_DIVISION.md](./TEAM_DIVISION.md)

## Development Conventions / 开发规范

### Branch Strategy / 分支策略

```
master          ← Stable release (for demo) / 稳定版本 (演示用)
├── dev          ← Development branch (daily merges) / 开发分支 (日常合并)
│   ├── feat/user-management    ← Member 1: User management / 组员1: 用户管理
│   ├── feat/workflow-engine    ← Member 2: Workflow engine / 组员2: 审批引擎
│   ├── feat/attendance-notice  ← Member 3: Attendance + Notices / 组员3: 考勤+公告
│   └── feat/schedule-admin     ← Member 4: Schedule & Admin & Stats / 组员4: 日程行政统计
```

### Commit Convention / Commit规范

```
feat: Add user management panel / 新增用户管理面板
fix: Fix workflow countersign logic / 修复审批会签逻辑
refactor: Refactor duplicate clock-in prevention / 重构打卡防重复逻辑
docs: Update team division doc / 更新分工文档
```

### Panel Registration Convention / 面板注册规范

All module panels must / 所有模块面板必须:
1. Extend `com.oa.ui.panel.BasePanel` / 继承 `com.oa.ui.panel.BasePanel`
2. Implement `getPanelKey()` and `getPanelTitle()` / 实现 `getPanelKey()` 和 `getPanelTitle()`
3. Register via `MainFrame.registerPanel(key, panel)` / 通过 `MainFrame.registerPanel(key, panel)` 注册

### Data Convention / 数据规范

- At least 5 meaningful seed records per module / 每个模块至少5条有意义的基础数据
- Student ID and name displayed in the status bar / 学号姓名显示在状态栏
- Code conforms to Alibaba Java Coding Guidelines / 代码符合阿里Java规范

## Course Info / 课程信息

- Course / 课程: Professional Production Labor ① / 专业生产劳动①
- Instructor / 主讲: Mo Jinmeng / 莫锦猛
- Semester / 学期: 2025-2026 Academic Year, Semester 2 / 2025-2026学年第2学期
