package com.oa.ui.panel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 欢迎主页 — 待办摘要 + 快捷入口
 */
public class WelcomePanel extends BasePanel {

    public WelcomePanel() {
        super();
        initUI();
    }

    @Override
    public String getPanelKey() { return "WELCOME"; }

    @Override
    public String getPanelTitle() { return "欢迎首页"; }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));

        // === 顶部欢迎词 ===
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("欢迎使用 OA 协同办公平台", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 26));
        header.add(title, BorderLayout.CENTER);
        JLabel dateLabel = new JLabel("今天是: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                SwingConstants.RIGHT);
        dateLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        dateLabel.setForeground(Color.GRAY);
        header.add(dateLabel, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // === 中间: 2×3 卡片网格 ===
        JPanel cardGrid = new JPanel(new GridLayout(2, 3, 20, 20));
        cardGrid.add(createCard("\uD83D\uDCCB 待审批", "0", "条待审批任务", "去审批 \u2192"));
        cardGrid.add(createCard("\uD83D\uDCEC 未读消息", "0", "条未读消息", "查看消息 \u2192"));
        cardGrid.add(createCard("\uD83D\uDCC5 今日日程", "0", "项日程安排", "查看日程 \u2192"));
        cardGrid.add(createCard("\u2705 我的任务", "0", "个待办任务", "查看任务 \u2192"));
        cardGrid.add(createCard("\uD83D\uDCDD 快捷审批", "", "发起请假/报销/出差...", "发起审批 \u2192"));
        cardGrid.add(createCard("\uD83D\uDCE2 发布公告", "", "向全员发布通知公告", "发布公告 \u2192"));
        add(cardGrid, BorderLayout.CENTER);

        // === 底部提示 ===
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.add(new JLabel("\uD83D\uDCA1 提示: 点击左侧导航栏切换功能模块 | 右上角查看未读消息"));
        footer.setForeground(Color.GRAY);
        add(footer, BorderLayout.SOUTH);
    }

    /** 创建一个卡片面板 */
    private JPanel createCard(String icon, String number, String subtitle, String action) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        card.setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 28));
        top.add(iconLabel, BorderLayout.NORTH);

        if (!number.isEmpty()) {
            JLabel numLabel = new JLabel(number);
            numLabel.setFont(new Font("Arial", Font.BOLD, 36));
            numLabel.setForeground(new Color(24, 144, 255));
            top.add(numLabel, BorderLayout.CENTER);
        }
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        subLabel.setForeground(Color.GRAY);
        top.add(subLabel, BorderLayout.SOUTH);
        card.add(top, BorderLayout.CENTER);

        JLabel actionLabel = new JLabel(action);
        actionLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        actionLabel.setForeground(new Color(24, 144, 255));
        card.add(actionLabel, BorderLayout.SOUTH);

        return card;
    }
}
