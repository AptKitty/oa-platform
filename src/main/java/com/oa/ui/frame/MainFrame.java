package com.oa.ui.frame;

import com.oa.system.service.RoleService;
import com.oa.system.entity.Menu;
import com.oa.ui.panel.ClockPanel;
import com.oa.ui.panel.LeavePanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 主界面框架 - 含菜单栏、侧边栏和工作区
 * 模块负责人: 【组员G】
 */
public class MainFrame extends BaseFrame {

    private JPanel sidebarPanel;
    private JPanel workspacePanel;
    private CardLayout workspaceLayout;
    private RoleService roleService = new RoleService();

    public MainFrame() {
        super("企业协同办公与流程审批系统");
        setSize(1280, 800);
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout());

        // 顶部菜单栏
        setJMenuBar(createMenuBar());

        // 侧边导航栏
        sidebarPanel = createSidebar();
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        sidebarPanel.setBackground(new Color(45, 52, 63));
        add(sidebarPanel, BorderLayout.WEST);

        // 工作区（卡片布局，切换不同功能面板）
        workspaceLayout = new CardLayout();
        workspacePanel = new JPanel(workspaceLayout);

        // 组员3：考勤管理 + 请假申请
        workspacePanel.add(new ClockPanel(), "ATTENDANCE");
        workspacePanel.add(new LeavePanel(), "LEAVE");

        // TODO: 其他组员将面板注册到这里

        workspacePanel.add(new JLabel("欢迎使用OA协同办公平台", SwingConstants.CENTER), "WELCOME");
        workspaceLayout.show(workspacePanel, "WELCOME");

        // 状态栏
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.add(new JLabel("当前用户: " + currentUsername));
        statusBar.add(new JLabel("  |  "));
        statusBar.add(new JLabel("学号: ________"));
        add(statusBar, BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        // TODO: 根据用户角色动态加载菜单
        JMenu sysMenu = new JMenu("系统管理");
        JMenu wfMenu = new JMenu("审批流程");
        JMenu attMenu = new JMenu("考勤管理");
        JMenu ntcMenu = new JMenu("公告消息");
        JMenu schMenu = new JMenu("日程任务");
        JMenu admMenu = new JMenu("行政管理");
        JMenu statMenu = new JMenu("统计大屏");
        JMenu imMenu = new JMenu("即时通讯"); // 预留

        menuBar.add(sysMenu);
        menuBar.add(wfMenu);
        menuBar.add(attMenu);
        menuBar.add(ntcMenu);
        menuBar.add(schMenu);
        menuBar.add(admMenu);
        menuBar.add(statMenu);
        menuBar.add(imMenu);
        return menuBar;
    }

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] navItems = {"系统管理", "审批流程", "考勤打卡", "公告消息",
                             "日程任务", "行政管理", "统计大屏", "即时通讯"};
        Color fgColor = new Color(200, 200, 200);
        Color bgColor = new Color(45, 52, 63);

        for (String item : navItems) {
            JButton btn = new JButton(item);
            btn.setMaximumSize(new Dimension(200, 45));
            btn.setForeground(fgColor);
            btn.setBackground(bgColor);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            // TODO: 点击切换workspace卡片
            panel.add(btn);
        }
        return panel;
    }

    /**
     * 注册功能面板到工作区 - 各模块组员调用此方法
     */
    public void registerPanel(String key, JPanel panel) {
        workspacePanel.add(panel, key);
    }

    /**
     * 切换工作区面板
     */
    public void showPanel(String key) {
        workspaceLayout.show(workspacePanel, key);
    }
}
