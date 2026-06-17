package com.oa.ui.frame;

import com.oa.ui.panel.*;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends BaseFrame {

    private JPanel workspacePanel;
    private CardLayout workspaceLayout;

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

        workspacePanel.add(new ApplyPanel(), "APPLY");
        workspacePanel.add(new ApprovalPanel(), "APPROVAL");
        workspacePanel.add(new FormTemplatePanel(), "FORM_TEMPLATE");
        workspacePanel.add(new ProcessDefPanel(), "PROCESS_DEF");
        workspacePanel.add(new ClockPanel(), "ATTENDANCE");
        workspacePanel.add(new LeavePanel(), "LEAVE");
        workspacePanel.add(new NoticePanel(), "NOTICE");
        workspacePanel.add(new AttendanceStatPanel(), "ATTENDANCE_STAT");
        workspacePanel.add(new MessagePanel(), "MESSAGE");
        workspacePanel.add(new JLabel("欢迎使用OA协同办公平台", SwingConstants.CENTER), "WELCOME");
        workspaceLayout.show(workspacePanel, "APPLY");

        add(workspacePanel, BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.add(new JLabel("当前用户: " + currentUsername));
        add(bar, BorderLayout.SOUTH);
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

        addBtn(p, "系统管理", null, fg, bg, font);
        addBtn(p, "审批流程", "APPLY", fg, bg, font);
        addBtn(p, "考勤打卡", "ATTENDANCE", fg, bg, font);
        addBtn(p, "公告消息", null, fg, bg, font);
        addBtn(p, "日程任务", null, fg, bg, font);
        addBtn(p, "行政管理", null, fg, bg, font);
        addBtn(p, "统计大屏", null, fg, bg, font);
        addBtn(p, "即时通讯", null, fg, bg, font);
        return p;
    }

    private void addBtn(JPanel parent, String text, String panelKey, Color fg, Color bg, Font font) {
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
                workspaceLayout.show(workspacePanel, panelKey);
                workspacePanel.revalidate();
            } else {
                JOptionPane.showMessageDialog(MainFrame.this, text + "模块开发中，敬请期待");
            }
        });
        parent.add(b);
    }

    public void registerPanel(String key, JPanel panel) {
        workspacePanel.add(panel, key);
    }

    public void showPanel(String key) {
        workspaceLayout.show(workspacePanel, key);
        workspacePanel.revalidate();
    }
}