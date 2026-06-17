# OA协同办公平台

> 企业协同办公与流程审批系统 — 专业生产劳动课程设计

## 技术栈

![Java](https://img.shields.io/badge/Java-8-brightgreen)
![Swing](https://img.shields.io/badge/GUI-Swing%20%2B%20FlatLaf-blue)
![MyBatis](https://img.shields.io/badge/ORM-MyBatis%203.5-red)
![MySQL](https://img.shields.io/badge/DB-MySQL%208.0-orange)
![JFreeChart](https://img.shields.io/badge/Chart-JFreeChart-yellow)

## 功能模块

| 模块 | 说明 |
|------|------|
| 🏢 系统管理 | 用户/部门/角色/RBAC权限 (按钮级) |
| 🔄 审批流程引擎 | 可配置表单模板 + 动态审批链 (会签/或签/条件分支/抄送) |
| 📋 考勤管理 | 上班/下班打卡, 请假额度管理, 月度统计 |
| 📢 公告消息 | 公告发布/已读追踪, 站内消息中心 |
| 📅 日程任务 | 日历视图, 会议管理+冲突检测, 任务分配跟踪 |
| 🏛 行政管理 | 会议室预约, 资产领用/归还, 车辆管理 |
| 📊 统计大屏 | 审批效率/考勤对比/请假分布 (JFreeChart图表) |
| 💬 即时通讯 | WebSocket通信 (预留) |

## 快速开始

### 环境要求

- JDK 8+
- MySQL 8.0+
- Maven 3.6+

### 1. 创建数据库

```sql
-- 执行 schema.sql
mysql -u root -p < src/main/resources/db/schema.sql
```

### 2. 修改数据库连接

编辑 `src/main/resources/mybatis-config.xml`:

```xml
<property name="url" value="jdbc:mysql://localhost:3306/oa_platform?..."/>
<property name="username" value="你的用户名"/>
<property name="password" value="你的密码"/>
```

### 3. 运行

```bash
mvn compile exec:java -Dexec.mainClass="com.oa.Application"
```

或在 IDE 中直接运行 `com.oa.Application.main()`

### 4. 登录

默认管理员账号: `admin` / `admin` (组员A初始化后生效)

## 项目结构

```
oa-platform
├── pom.xml                          Maven配置
├── TEAM_DIVISION.md                 团队分工文档
├── src/main/java/com/oa/
│   ├── Application.java            入口
│   ├── common/                      基础层 (BaseEntity, ExportUtil, MyBatisUtil...)
│   ├── system/                      系统管理 (用户/部门/角色/菜单)
│   ├── workflow/                    审批引擎 (表单模板/流程定义/实例/审批记录)
│   ├── attendance/                  考勤管理 (打卡/请假)
│   ├── notice/                      公告消息 (公告/站内消息)
│   ├── schedule/                    日程任务 (日历/会议/任务)
│   ├── admin/                       行政管理 (会议室/资产/车辆)
│   ├── statistics/                  统计大屏 (JFreeChart图表)
│   ├── im/                          即时通讯 (WebSocket预留)
│   └── ui/
│       ├── frame/                   BaseFrame / LoginFrame / MainFrame
│       └── panel/                   各模块面板 (BasePanel基类)
└── src/main/resources/
    ├── db/schema.sql                数据库DDL (24张表)
    ├── mybatis-config.xml           MyBatis配置
    └── mapper/                      16个Mapper XML
```

## 4人分工

| 组员 | 模块 | 难度 |
|------|------|------|
| 组员1 | 系统管理 + 主界面 + IM预留 | ⭐⭐⭐⭐ |
| 组员2 | 审批流程引擎 | ⭐⭐⭐⭐⭐ |
| 组员3 | 考勤管理 + 公告消息 | ⭐⭐⭐⭐ |
| 组员4 | 日程行政 + 统计大屏 + 导入导出 | ⭐⭐⭐⭐⭐ |

详见 [TEAM_DIVISION.md](./TEAM_DIVISION.md)

## 开发规范

### 分支策略

```
master          ← 稳定版本 (演示用)
├── dev          ← 开发分支 (日常合并)
│   ├── feat/user-management    ← 组员1: 用户管理
│   ├── feat/workflow-engine    ← 组员2: 审批引擎
│   ├── feat/attendance-notice  ← 组员3: 考勤+公告
│   └── feat/schedule-admin     ← 组员4: 日程行政统计
```

### Commit规范

```
feat: 新增用户管理面板
fix: 修复审批会签逻辑
refactor: 重构打卡防重复逻辑
docs: 更新分工文档
```

### 面板注册规范

所有模块面板必须:
1. 继承 `com.oa.ui.panel.BasePanel`
2. 实现 `getPanelKey()` 和 `getPanelTitle()`
3. 通过 `MainFrame.registerPanel(key, panel)` 注册

### 数据规范

- 每个模块至少5条有意义的基础数据
- 学号姓名显示在状态栏
- 代码符合阿里Java规范

## 课程信息

- 课程: 专业生产劳动①
- 主讲: 莫锦猛
- 学期: 2025-2026学年第2学期
