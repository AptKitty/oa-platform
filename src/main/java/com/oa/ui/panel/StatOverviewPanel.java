package com.oa.ui.panel;

import com.oa.statistics.service.StatService;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class StatOverviewPanel extends BasePanel {

    private StatService statService = new StatService();

    public StatOverviewPanel() {
        initUI();
    }

    @Override
    public String getPanelKey() { return "STATISTICS"; }

    @Override
    public String getPanelTitle() { return "统计大屏"; }

    private void initUI() {
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);

        JPanel cardPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(cardPanel, BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        Map<String, Object> overview = statService.getOverview();
        Map<String, Object> pending = statService.getPendingTaskStats();

        removeAll();
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);

        // 主卡片面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // 4个概览卡片
        JPanel cardPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        cardPanel.add(createCard("用户总数", overview.get("totalUsers"), new Color(52, 152, 219)));
        cardPanel.add(createCard("部门数", overview.get("totalDepts"), new Color(46, 204, 113)));
        cardPanel.add(createCard("待审批", overview.get("pendingApprovals"), new Color(231, 76, 60)));
        cardPanel.add(createCard("今日提交", overview.get("todayInstances"), new Color(155, 89, 182)));
        mainPanel.add(cardPanel, BorderLayout.NORTH);

        // 待办实时统计 + 子面板入口
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));

        // 左侧：待办统计
        JPanel pendingPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        pendingPanel.setBorder(BorderFactory.createTitledBorder("待办事项实时统计"));
        pendingPanel.add(createStatRow("待处理任务", pending.get("totalPending")));
        pendingPanel.add(createStatRow("今日创建流程", pending.get("todayCreated")));
        pendingPanel.add(createStatRow("今日已审批", pending.get("todayApproved")));
        bottomPanel.add(pendingPanel);

        // 右侧：快捷入口
        JPanel linkPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        linkPanel.setBorder(BorderFactory.createTitledBorder("详细统计"));
        JButton approvalBtn = new JButton("审批效率统计（柱状图）");
        approvalBtn.addActionListener(e -> navigateTo("APPROVAL_STAT"));
        linkPanel.add(approvalBtn);

        JButton attBtn = new JButton("考勤统计图表（柱状图+饼图）");
        attBtn.addActionListener(e -> navigateTo("ATT_CHART"));
        linkPanel.add(attBtn);

        JButton attTableBtn = new JButton("月度考勤汇总表");
        attTableBtn.addActionListener(e -> navigateTo("ATTENDANCE_STAT"));
        linkPanel.add(attTableBtn);
        bottomPanel.add(linkPanel);

        mainPanel.add(bottomPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createCard(String title, Object value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(String.valueOf(value != null ? value : 0), SwingConstants.CENTER);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 42));
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStatRow(String label, Object value) {
        JPanel row = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("  " + label);
        lbl.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        row.add(lbl, BorderLayout.WEST);
        JLabel val = new JLabel(String.valueOf(value != null ? value : 0));
        val.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        val.setForeground(new Color(25, 118, 210));
        row.add(val, BorderLayout.EAST);
        return row;
    }

    /** 导航到其他统计子面板 */
    private void navigateTo(String panelKey) {
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof com.oa.ui.frame.MainFrame) {
                ((com.oa.ui.frame.MainFrame) parent).showPanel(panelKey);
                return;
            }
            parent = parent.getParent();
        }
    }

    // ===== 测试入口 =====
    public static void main(String[] args) {
        JFrame frame = new JFrame("统计大屏");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 550);
        frame.setLocationRelativeTo(null);
        StatOverviewPanel panel = new StatOverviewPanel();
        panel.setCurrentUser(1L, "测试用户");
        frame.add(panel);
        frame.setVisible(true);
    }
}
