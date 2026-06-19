package com.oa.ui.frame;

import com.oa.ui.panel.*;
import com.oa.common.Constants;
import com.oa.system.service.MenuService;
import com.oa.system.entity.Menu;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainFrame extends BaseFrame {

    private JPanel workspacePanel;
    private CardLayout workspaceLayout;
    private java.util.Map<String, java.util.function.Supplier<JPanel>> panelFactories = new java.util.HashMap<>();
    private Set<String> allowedMenuCodes = new HashSet<>();

    public MainFrame() {
        super("OA协同办公平台");
        setSize(1280, 800);
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout());
        setJMenuBar(createMenuBar());

        add(createSidebar(), BorderLayout.WEST);

        workspaceLayout = new CardLayout();
        workspacePanel = new JPanel(workspaceLayout);

        registerLazy("APPLY",         () -> new ApplyPanel());
        registerLazy("APPROVAL",      () -> new ApprovalPanel());
        registerLazy("MY_APPLICATIONS", () -> new MyApplicationPanel());
        registerLazy("FORM_TEMPLATE", () -> new FormTemplatePanel());
        registerLazy("PROCESS_DEF",   () -> new ProcessDefPanel());
        registerLazy("ATTENDANCE",    () -> new ClockPanel());
        registerLazy("LEAVE",         () -> new LeavePanel());
        registerLazy("NOTICE",        () -> new NoticePanel());
        registerLazy("ATTENDANCE_STAT", () -> new AttendanceStatPanel());
        registerLazy("MESSAGE",       () -> new MessagePanel());
        registerLazy("SCHEDULE",      () -> new CalendarPanel());
        registerLazy("ADMIN",         () -> new MeetingRoomPanel());
        registerLazy("STATISTICS",    () -> new StatOverviewPanel());
        registerLazy("IM",            () -> new ImPanel());
        registerLazy("SYSTEM",        () -> new UserManagePanel());
        registerLazy("ASSET",         () -> new AssetPanel());
        registerLazy("VEHICLE",       () -> new VehiclePanel());
        registerLazy("TASK",          () -> new TaskPanel());
        registerLazy("MEETING",       () -> new MeetingPanel());
        registerLazy("WELCOME",       () -> new WelcomePanel());
        registerLazy("IMPORT",        () -> new ImportPanel());
        registerLazy("DEPT_MANAGE",   () -> new DeptManagePanel());
        registerLazy("ROLE_PERM",     () -> new RolePermissionPanel());
        registerLazy("APPROVAL_STAT", () -> new ApprovalStatPanel());
        registerLazy("ATT_CHART",     () -> new AttendanceStatChartPanel());

        add(workspacePanel, BorderLayout.CENTER);

        // 底部状态栏
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.add(new JLabel("当前用户: " + currentUsername));

        JButton logoutBtn = new JButton("退出登录");
        logoutBtn.addActionListener(e -> {
            if (confirm("确定要退出登录吗？")) {
                Constants.clearCurrentUser();
                dispose();
                SwingUtilities.invokeLater(() -> {
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                });
            }
        });
        bar.add(logoutBtn);
        add(bar, BorderLayout.SOUTH);
    }

    private void registerLazy(String key, java.util.function.Supplier<JPanel> factory) {
        panelFactories.put(key, factory);
        workspacePanel.add(new JPanel(), key);
    }

    // ==================== 顶部菜单栏（与侧边栏分组同步） ====================

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();

        addMenu(mb, "系统管理", new String[][]{
            {"用户管理", "SYSTEM", "USER_MANAGE"},
            {"部门管理", "DEPT_MANAGE", "DEPT_MANAGE"},
            {"角色权限", "ROLE_PERM", "ROLE_PERMISSION"},
            {"数据导入", "IMPORT", "SYSTEM"},
        });

        addMenu(mb, "流程审批", new String[][]{
            {"发起申请", "APPLY", "WF_START"},
            {"我的审批", "APPROVAL", "WF_MY_APPROVAL"},
            {"我的申请", "MY_APPLICATIONS", "WF_MY_APPLICATIONS"},
            {"审批模板", "FORM_TEMPLATE", "WF_TEMPLATE"},
            {"流程定义", "PROCESS_DEF", "WF_DEF"},
        });

        addMenu(mb, "考勤管理", new String[][]{
            {"考勤打卡", "ATTENDANCE", "ATT_CLOCK"},
            {"请假管理", "LEAVE", "ATT_LEAVE"},
            {"考勤统计", "ATTENDANCE_STAT", "ATT_STAT"},
        });

        addMenu(mb, "公告消息", new String[][]{
            {"公告列表", "NOTICE", "NOTICE_LIST"},
            {"消息中心", "MESSAGE", "MSG_CENTER"},
        });

        addMenu(mb, "日程任务", new String[][]{
            {"个人日程", "SCHEDULE", "SCH_CALENDAR"},
            {"会议管理", "MEETING", "SCH_MEETING"},
            {"任务管理", "TASK", "SCH_TASK"},
        });

        addMenu(mb, "行政管理", new String[][]{
            {"会议室", "ADMIN", "ADM_ROOM"},
            {"资产管理", "ASSET", "ADM_ASSET"},
            {"车辆管理", "VEHICLE", "ADM_VEHICLE"},
        });

        addMenu(mb, "统计大屏", new String[][]{
            {"数据概览", "STATISTICS", "STAT_OVERVIEW"},
            {"审批统计", "APPROVAL_STAT", "STAT_APPROVAL"},
            {"考勤图表", "ATT_CHART", "STAT_ATTENDANCE"},
            {"考勤汇总", "ATTENDANCE_STAT", "ATT_STAT"},
        });

        addMenu(mb, "即时通讯", new String[][]{
            {"聊天窗口", "IM", "IM_CHAT"},
        });

        return mb;
    }

    /** 添加一个下拉菜单，子项受权限过滤 */
    private void addMenu(JMenuBar mb, String title, String[][] items) {
        // 过滤有权限的子项
        List<JMenuItem> visible = new ArrayList<>();
        for (String[] item : items) {
            String menuCode = item[2];
            if ("WELCOME".equals(menuCode) || allowedMenuCodes.isEmpty()
                    || allowedMenuCodes.contains(menuCode)) {
                JMenuItem mi = new JMenuItem(item[0]);
                String panelKey = item[1];
                mi.addActionListener(e -> showPanel(panelKey));
                visible.add(mi);
            }
        }
        if (visible.isEmpty()) return;

        JMenu menu = new JMenu(title);
        for (JMenuItem mi : visible) menu.add(mi);
        mb.add(menu);
    }

    // ==================== 折叠分组侧边栏 ====================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(45, 52, 63));
        sidebar.setPreferredSize(new Dimension(210, 0));

        Color fg = new Color(200, 200, 200);
        Color bg = new Color(45, 52, 63);
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 14);

        addGroup(sidebar, "工作台", true, fg, bg, font,
            new String[][]{{"工作台首页", "WELCOME", "WELCOME"}});

        addGroup(sidebar, "流程审批", false, fg, bg, font,
            new String[][]{
                {"发起申请", "APPLY", "WF_START"},
                {"我的审批", "APPROVAL", "WF_MY_APPROVAL"},
                {"我的申请", "MY_APPLICATIONS", "WF_MY_APPLICATIONS"},
                {"审批模板", "FORM_TEMPLATE", "WF_TEMPLATE"},
                {"流程定义", "PROCESS_DEF", "WF_DEF"},
            });

        addGroup(sidebar, "考勤管理", false, fg, bg, font,
            new String[][]{
                {"考勤打卡", "ATTENDANCE", "ATT_CLOCK"},
                {"请假管理", "LEAVE", "ATT_LEAVE"},
                {"考勤统计", "ATTENDANCE_STAT", "ATT_STAT"},
            });

        addGroup(sidebar, "公告消息", false, fg, bg, font,
            new String[][]{
                {"公告列表", "NOTICE", "NOTICE_LIST"},
                {"消息中心", "MESSAGE", "MSG_CENTER"},
            });

        addGroup(sidebar, "日程任务", false, fg, bg, font,
            new String[][]{
                {"个人日程", "SCHEDULE", "SCH_CALENDAR"},
                {"会议管理", "MEETING", "SCH_MEETING"},
                {"任务管理", "TASK", "SCH_TASK"},
            });

        addGroup(sidebar, "行政管理", false, fg, bg, font,
            new String[][]{
                {"会议室", "ADMIN", "ADM_ROOM"},
                {"资产管理", "ASSET", "ADM_ASSET"},
                {"车辆管理", "VEHICLE", "ADM_VEHICLE"},
            });

        addGroup(sidebar, "统计大屏", false, fg, bg, font,
            new String[][]{
                {"数据概览", "STATISTICS", "STAT_OVERVIEW"},
                {"审批统计", "APPROVAL_STAT", "STAT_APPROVAL"},
                {"考勤图表", "ATT_CHART", "STAT_ATTENDANCE"},
                {"考勤汇总", "ATTENDANCE_STAT", "ATT_STAT"},
            });

        addGroup(sidebar, "系统管理", false, fg, bg, font,
            new String[][]{
                {"用户管理", "SYSTEM", "USER_MANAGE"},
                {"部门管理", "DEPT_MANAGE", "DEPT_MANAGE"},
                {"角色权限", "ROLE_PERM", "ROLE_PERMISSION"},
                {"数据导入", "IMPORT", "SYSTEM"},
            });

        addGroup(sidebar, "即时通讯", false, fg, bg, font,
            new String[][]{
                {"聊天窗口", "IM", "IM_CHAT"},
            });

        return sidebar;
    }

    /**
     * 添加一个可折叠的分组
     * @param title    分组标题（纯文本，不含emoji）
     * @param expanded 是否默认展开
     * @param items    子项数组，每项 {显示文字, panelKey, menuCode}
     */
    private void addGroup(JPanel parent, String title, boolean expanded,
                          Color fg, Color bg, Font font, String[][] items) {
        List<String[]> visibleItems = new ArrayList<>();
        for (String[] item : items) {
            String menuCode = item[2];
            if ("WELCOME".equals(menuCode) || allowedMenuCodes.isEmpty()
                    || allowedMenuCodes.contains(menuCode)) {
                visibleItems.add(item);
            }
        }
        if (visibleItems.isEmpty()) return;

        // 分组标题按钮
        JButton headerBtn = new JButton((expanded ? "- " : "+ ") + title);
        headerBtn.setMaximumSize(new Dimension(210, 38));
        headerBtn.setForeground(new Color(180, 180, 180));
        headerBtn.setBackground(new Color(55, 62, 73));
        headerBtn.setBorderPainted(false);
        headerBtn.setFocusPainted(false);
        headerBtn.setHorizontalAlignment(SwingConstants.LEFT);
        headerBtn.setFont(font.deriveFont(Font.BOLD, 13f));
        headerBtn.setMargin(new Insets(0, 15, 0, 0));

        // 子项面板
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
        subPanel.setBackground(bg);
        subPanel.setVisible(expanded);

        Font subFont = font.deriveFont(13f);
        for (String[] item : visibleItems) {
            JButton btn = new JButton("    " + item[0]);
            btn.setMaximumSize(new Dimension(210, 35));
            btn.setForeground(fg);
            btn.setBackground(bg);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setFont(subFont);
            String panelKey = item[1];
            btn.addActionListener(e -> showPanel(panelKey));
            subPanel.add(btn);
        }

        headerBtn.addActionListener(e -> {
            boolean nowVisible = !subPanel.isVisible();
            subPanel.setVisible(nowVisible);
            headerBtn.setText((nowVisible ? "- " : "+ ") + title);
            parent.revalidate();
            parent.repaint();
        });

        parent.add(headerBtn);
        parent.add(subPanel);
    }

    // ==================== 权限 & 面板切换 ====================

    public void setAllowedMenuCodes(Set<String> codes) {
        this.allowedMenuCodes = codes;
    }

    public void registerPanel(String key, JPanel panel) {
        workspacePanel.add(panel, key);
    }

    public void showPanel(String key) {
        java.util.function.Supplier<JPanel> factory = panelFactories.get(key);
        if (factory != null) {
            JPanel realPanel = factory.get();
            if (realPanel instanceof BasePanel) {
                ((BasePanel) realPanel).setCurrentUser(currentUserId, currentUsername);
            }
            workspacePanel.add(realPanel, key);
            panelFactories.remove(key);
        }
        workspaceLayout.show(workspacePanel, key);
        workspacePanel.revalidate();
    }
}


