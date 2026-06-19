package com.oa.ui.frame;

import com.oa.ui.panel.*;
import com.oa.common.Constants;
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

        // 底部状态栏：用户信息 + 退出登录按钮
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
        addBtn(p, "我的申请", "MY_APPLICATIONS", "WF_MY_APPLICATIONS", fg, bg, font);
        addBtn(p, "考勤打卡", "ATTENDANCE", "ATT_CLOCK", fg, bg, font);
        addBtn(p, "考勤统计", "ATTENDANCE_STAT", "ATT_STAT", fg, bg, font);
        addBtn(p, "公告消息", "NOTICE", "NOTICE_LIST", fg, bg, font);
        addBtn(p, "消息中心", "MESSAGE", "MSG_CENTER", fg, bg, font);
        addBtn(p, "日程任务", "SCHEDULE", "SCH_CALENDAR", fg, bg, font);
        addBtn(p, "行政管理", "ADMIN", "ADM_ROOM", fg, bg, font);
        addBtn(p, "统计大屏", "STATISTICS", "STAT_OVERVIEW", fg, bg, font);
        addBtn(p, "  审批效率", "APPROVAL_STAT", "STAT_APPROVAL", fg, bg, font.deriveFont(12f));
        addBtn(p, "  考勤图表", "ATT_CHART", "STAT_ATTENDANCE", fg, bg, font.deriveFont(12f));
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

                    /** 设置当前用户允许的菜单编码 */
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


