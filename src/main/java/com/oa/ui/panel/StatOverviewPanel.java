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
        Map<String, Object> stats = statService.getOverview();

        // 重建卡片
        removeAll();
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);

        JPanel cardPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        cardPanel.add(createCard("用户总数", stats.get("totalUsers"), new Color(52, 152, 219)));
        cardPanel.add(createCard("部门数", stats.get("totalDepts"), new Color(46, 204, 113)));
        cardPanel.add(createCard("待审批", stats.get("pendingApprovals"), new Color(231, 76, 60)));
        cardPanel.add(createCard("今日提交", stats.get("todayInstances"), new Color(155, 89, 182)));

        add(cardPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createCard(String title, Object value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(String.valueOf(value != null ? value : 0), SwingConstants.CENTER);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 42));
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // ===== 测试入口 =====
    public static void main(String[] args) {
        JFrame frame = new JFrame("统计大屏");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 400);
        frame.setLocationRelativeTo(null);
        StatOverviewPanel panel = new StatOverviewPanel();
        panel.setCurrentUser(1L, "测试用户");
        frame.add(panel);
        frame.setVisible(true);
    }
}
