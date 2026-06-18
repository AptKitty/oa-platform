package com.oa.ui.frame;

import com.oa.ui.panel.*;
import com.oa.system.service.MenuService;
import com.oa.system.entity.Menu;
import java.util.Set;
import java.util.HashSet;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends BaseFrame {

    private JPanel workspacePanel;
    private CardLayout workspaceLayout;
    // 面板工厂：key → 创建面板的 lambda，首次点击时才 new
    private Map<String, java.util.function.Supplier<JPanel>> panelFactories = new HashMap<>();
    private Set<String> allowedMenuCodes = new HashSet<>();
    private JPanel sidebarPanel;

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

        // 注册面板工厂：只在首次访问时创建面板（懒加载）
        registerLazy("APPLY",         () -> new ApplyPanel());
        registerLazy("APPROVAL",      () -> new ApprovalPanel());
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

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.add(new JLabel("当前用户: " + currentUsername));
        add(bar, BorderLayout.SOUTH);
    }

    private void registerLazy(String key, java.util.function.Supplier<JPanel> factory) {
        panelFactories.put(key, factory);
        // 先放一个占位 JPanel，等首次访问时替换
        workspacePanel.add(new JPanel(), key);
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.add(new JMenu("系统管理"));
        mb.add(new JMenu("审批流程"));
        mb.add(new JMenu("考勤管理"));
        mb.add(new JMenu("公告消息"));
        mb.add(new JMenu("日程任务"));
        mb.add(new JMenu("行政管理"));
        mb.add(new JMenu("统计大屏"));
        mb.add(new JMenu("即时通讯"));
        return mb;
    }

    private JPanel createSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(45, 52, 63));
        p.setPreferredSize(new Dimension(200, 0));
        Color fg = new Color(200, 200, 200);
        Color bg = new Color(45, 52, 63);
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 14);

        addBtn(p, "系统管理", "SYSTEM", "SYSTEM", fg, bg, font);
        addBtn(p, "审批流程", "APPLY", "WF_START", fg, bg, font);
        addBtn(p, "我的审批", "APPROVAL", "WF_MY_APPROVAL", fg, bg, font);
        addBtn(p, "考勤打卡", "ATTENDANCE", "ATT_CLOCK", fg, bg, font);
        addBtn(p, "公告消息", "NOTICE", "NOTICE_LIST", fg, bg, font);
        addBtn(p, "日程任务", "SCHEDULE", "SCH_CALENDAR", fg, bg, font);
        addBtn(p, "行政管理", "ADMIN", "ADM_ROOM", fg, bg, font);
        addBtn(p, "统计大屏", "STATISTICS", "STAT_OVERVIEW", fg, bg, font);
        addBtn(p, "即时通讯", "IM", "IM_CHAT", fg, bg, font);
        addBtn(p, "工作台",    "WELCOME", "WELCOME",  fg, bg, font);
        addBtn(p, "资产管理", "ASSET", "ADM_ASSET",    fg, bg, font);
        addBtn(p, "车辆管理", "VEHICLE", "ADM_VEHICLE",  fg, bg, font);
        addBtn(p, "任务管理", "TASK", "SCH_TASK",     fg, bg, font);
        addBtn(p, "会议管理", "MEETING", "SCH_MEETING",  fg, bg, font);
        addBtn(p, "数据导入", "IMPORT", "SYSTEM",   fg, bg, font);
        return p;
    }

    private void addBtn(JPanel parent, String text, String panelKey, String menuCode, Color fg, Color bg, Font font) {
        if (!allowedMenuCodes.isEmpty() && !allowedMenuCodes.contains(menuCode) && !"WELCOME".equals(panelKey)) {
            return;
        }
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(200, 45));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(font);
        b.addActionListener(e -> {
            if (panelKey != null) {
                showPanel(panelKey);
            }
        });
        parent.add(b);
    }

    public void registerPanel(String key, JPanel panel) {
        workspacePanel.add(panel, key);
    }

    /** 切换面板（首次访问时懒加载） */
    public void showPanel(String key) {
        java.util.function.Supplier<JPanel> factory = panelFactories.get(key);
        if (factory != null) {
            // 懒加载：首次访问时创建真正的面板
            JPanel realPanel = factory.get();
            if (realPanel instanceof BasePanel) {
                ((BasePanel) realPanel).setCurrentUser(currentUserId, currentUsername);
            }
            workspacePanel.add(realPanel, key);
            panelFactories.remove(key);  // 只创建一次
        }
        workspaceLayout.show(workspacePanel, key);
        workspacePanel.revalidate();
    }
}