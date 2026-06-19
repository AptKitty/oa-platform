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
        refresh();
    }

    private void refresh() {
        Map<String, Object> overview = statService.getOverview();
        Map<String, Object> pending = statService.getPendingTaskStats();

        removeAll();
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // 4个概览卡片（保持不变）
        JPanel cardPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardPanel.add(createCard("用户总数", overview.get("totalUsers"), new Color(52, 152, 219)));
        cardPanel.add(createCard("部门数",   overview.get("totalDepts"),      new Color(46, 204, 113)));
        cardPanel.add(createCard("待审批",   overview.get("pendingApprovals"),new Color(231, 76, 60)));
        cardPanel.add(createCard("今日提交", overview.get("todayInstances"),  new Color(155, 89, 182)));
        mainPanel.add(cardPanel, BorderLayout.NORTH);

        // 下半部分：待办统计（大卡片）+ 快捷入口
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));

        // 左侧：待办事项实时统计（大卡片样式）
        JPanel pendingPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        pendingPanel.setBorder(BorderFactory.createTitledBorder("待办事项实时统计"));

        pendingPanel.add(createMiniCard("待处理任务",
                pending.get("totalPending"),
                new Color(230, 126, 34),   // 橙色
                "此任务需要审批"));

        pendingPanel.add(createMiniCard("今日创建",
                pending.get("todayCreated"),
                new Color(52, 152, 219),   // 蓝色
                "本日新提交流程"));

        pendingPanel.add(createMiniCard("今日已审批",
                pending.get("todayApproved"),
                new Color(39, 174, 96),    // 绿色
                "本日已完成审批"));

        bottomPanel.add(pendingPanel);

        // 右侧：快捷入口
        JPanel linkPanel = new JPanel(new GridLayout(3, 1, 5, 8));
        linkPanel.setBorder(BorderFactory.createTitledBorder("详细统计"));
        linkPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("详细统计"),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

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

    /** 顶部大卡片 */
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

    /** 待办统计小卡片：大数字 + 颜色背景 + 底部说明 */
    private JPanel createMiniCard(String title, Object value, Color color, String subtitle) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 标题
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(new Color(255, 255, 255, 220));
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        card.add(titleLabel, BorderLayout.NORTH);

        // 数字
        String valStr = String.valueOf(value != null ? value : 0);
        JLabel valueLabel = new JLabel(valStr, SwingConstants.CENTER);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        card.add(valueLabel, BorderLayout.CENTER);

        // 底部说明
        JLabel subLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subLabel.setForeground(new Color(255, 255, 255, 180));
        subLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        card.add(subLabel, BorderLayout.SOUTH);

        return card;
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
