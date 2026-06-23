============================================================
  企业协同办公与流程审批系统（OA平台） v1.0.0
  作者：陈健聪  学号：2531020130218
============================================================

【环境要求】
  1. JDK 8 或更高版本（必须）
  2. MySQL 8.0+（必须）

================================================================
  给别人的电脑 — 完整部署步骤（4步）
================================================================

第1步：安装 Java 运行环境
  - 下载 JDK 8：https://www.oracle.com/java/technologies/downloads/
    或 OpenJDK：https://adoptium.net/
  - 安装后验证：打开命令行输入 java -version，看到版本号即可

第2步：安装 MySQL 数据库
  - 下载 MySQL 8.0：https://dev.mysql.com/downloads/installer/
  - 安装时设置 root 密码为 root
    （如果设了别的密码，后面需要改 mybatis-config.xml）
  - 确保 MySQL 服务已启动（Windows 服务里找到 MySQL80 正在运行）

第3步：创建数据库并导入数据
  打开命令行（cmd 或 PowerShell），进入 release 文件夹：
  
  方式A（命令行）：
    mysql -u root -proot < 数据库建表.sql
    mysql -u root -proot oa_platform < 测试数据.sql

  方式B（用 Navicat/DBeaver 等工具）：
    先执行"数据库建表.sql" → 再执行"测试数据.sql"

  注意：-u root 是用户名，-proot 是密码（root和root之间没有空格）

第4步：启动系统
  双击"启动系统.bat"
  或在命令行执行：java -jar OA办公系统.jar

================================================================
  常见问题
================================================================

Q: 启动时报 "找不到数据库"
A: 1. 确认 MySQL 服务已启动
   2. 确认 root 密码是 root，否则修改 mybatis-config.xml 里的 password
   3. 确认已执行过数据库建表.sql

Q: 报 "Access denied for user root"
A: MySQL 密码不对，修改 mybatis-config.xml 第24行的 password

Q: 报 "Unknown database oa_platform"
A: 没执行建表SQL，先执行：mysql -u root -proot < 数据库建表.sql

Q: 报 "Communications link failure"
A: MySQL 服务没启动，去 Windows 服务里启动 MySQL80

Q: 双击 bat 闪退
A: 右键 bat → 编辑 → 最后加一行 pause，保存后再双击看报错信息

Q: MySQL 端口不是 3306
A: 修改 mybatis-config.xml 第22行 URL 里的 3306 改你的端口号

Q: 没有安装 MySQL，想用别的电脑的数据库
A: 修改 mybatis-config.xml 第22行的 localhost 为那台电脑的 IP

================================================================
  修改数据库连接配置
================================================================

用记事本打开 mybatis-config.xml，找到这段：

  <property name="url" value="jdbc:mysql://localhost:3306/oa_platform?..."/>
  <property name="username" value="root"/>
  <property name="password" value="root"/>

把 localhost 改成数据库IP，3306 改成端口，root 改成你的密码

================================================================
  测试账号
================================================================

  用户名      密码      角色              用途
  --------   ------   ---------------   ------------------
  admin      123456   超级管理员         系统管理/审计/统计大屏
  zhangjl    123456   部门主管（技术部）  审批下级申请
  lisi       123456   普通员工           发起申请/打卡/日程
  wanghr     123456   HR                考勤统计/员工档案
  zhaocw     123456   行政              会议室/资产/用车/公告
  chenkf     123456   财务              报销审批

  更多账号（共18个）请查看"测试数据.sql"文件

================================================================
  发布包文件清单
================================================================

  OA办公系统.jar       - 可执行程序（31.4MB，包含所有依赖）
  数据库建表.sql        - 33张表的DDL建表语句
  测试数据.sql          - 18个用户 + 完整测试数据
  mybatis-config.xml    - 数据库连接配置（可按需修改）
  启动系统.bat          - Windows双击启动脚本
  设计说明书.txt        - 完整设计文档
  7人分工明细.docx      - 团队分工Word文档
  7人分工明细.txt       - 团队分工文本文档
  README.txt            - 本说明文件

============================================================