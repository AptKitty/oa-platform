package com.oa.ui.panel;

import com.oa.workflow.service.WorkflowService;
import com.oa.notice.service.MessageService;
import com.oa.schedule.service.ScheduleService;
import com.oa.ui.frame.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 欢迎首页 — 待办摘要 + 快捷入口（数据实时从后端加载）
 */
public class WelcomePanel extends BasePanel {

    private WorkflowService workflowService = new WorkflowService();
    private MessageService messageService = new MessageService();
    private ScheduleService scheduleService = new ScheduleService();

    private JLabel pendingApprovalLabel;
    private JLabel unreadMsgLabel;
    private JLabel todayScheduleLabel;
    private JLabel myTaskLabel;

    private JPanel pendingCard, unreadCard, scheduleCard, taskCard, quickApplyCard, noticeCard;

    public WelcomePanel() {
        super();
        initUI();
        loadData();
    }

    @Override
    public String getPanelKey() { return "WELCOME"; }

    @Override
    public String getPanelTitle() { return "欢迎首页"; }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));

        // === 顶部欢迎语 ===
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("欢迎使用 OA 协同办公平台", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 26));
        header.add(title, BorderLayout.CENTER);
        JLabel dateLabel = new JLabel("今天是 " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                SwingConstants.RIGHT);
        dateLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        dateLabel.setForeground(Color.GRAY);
        header.add(dateLabel, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // === 中间: 2x3 卡片网格 ===
        JPanel cardGrid = new JPanel(new GridLayout(2, 3, 20, 20));

        pendingApprovalLabel = createNumLabel();
        pendingCard = createCard("待审批", pendingApprovalLabel, "条待审批任务", "去审批 →");
        pendingCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTo("APPROVAL"); }
        });
        cardGrid.add(pendingCard);

        unreadMsgLabel = createNumLabel();
        unreadCard = createCard("未读消息", unreadMsgLabel, "条未读消息", "查看消息 →");
        unreadCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTo("MESSAGE"); }
        });
        cardGrid.add(unreadCard);

        todayScheduleLabel = createNumLabel();
        scheduleCard = createCard("今日日程", todayScheduleLabel, "项目程安排", "查看日程 →");
        scheduleCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTo("SCHEDULE"); }
        });
        cardGrid.add(scheduleCard);

        myTaskLabel = createNumLabel();
        taskCard = createCard("我的任务", myTaskLabel, "个待办任务", "查看任务 →");
        taskCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTo("TASK"); }
        });
        cardGrid.add(taskCard);

        quickApplyCard = createCard("快捷审批", new JLabel(""), "发起请假/报销/出差...", "发起审批 →");
        quickApplyCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTo("APPLY"); }
        });
        cardGrid.add(quickApplyCard);

        noticeCard = createCard("发布公告", new JLabel(""), "向全员发布通知公告", "发布公告 →");
        noticeCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTo("NOTICE"); }
        });
        cardGrid.add(noticeCard);

        add(cardGrid, BorderLayout.CENTER);

        // === 底部提示 ===
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.add(new JLabel("提示: 点击左侧导航栏切换功能模块 | 卡片可点击直达"));
        footer.setForeground(Color.GRAY);
        add(footer, BorderLayout.SOUTH);
    }

    private JLabel createNumLabel() {
        JLabel label = new JLabel("...");
        label.setFont(new Font("Arial", Font.BOLD, 36));
        label.setForeground(new Color(24, 144, 255));
        return label;
    }

    /** 创建卡片面板 */
    private JPanel createCard(String icon, JLabel numberLabel, String subtitle, String action) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        card.setBackground(Color.WHITE);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 28));
        top.add(iconLabel, BorderLayout.NORTH);

        top.add(numberLabel, BorderLayout.CENTER);

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

    /** 异步加载所有摘要数据 */
    private void loadData() {
        final Long userId = getCurrentUserId();
        runAsync(() -> {
            try {
                // 待审批数量
                int pendingCount = workflowService.getPendingApprovals(userId, 1, 100).size();
                SwingUtilities.invokeLater(() -> pendingApprovalLabel.setText(String.valueOf(pendingCount)));

                // 未读消息数
                int unreadCount = messageService.getUnreadCount(userId);
                SwingUtilities.invokeLater(() -> unreadMsgLabel.setText(String.valueOf(unreadCount)));

                // 今日日程
                LocalDateTime todayStart = LocalDate.now().atStartOfDay();
                LocalDateTime todayEnd = todayStart.plusDays(1);
                int scheduleCount = scheduleService.getUserEvents(userId, todayStart, todayEnd).size();
                SwingUtilities.invokeLater(() -> todayScheduleLabel.setText(String.valueOf(scheduleCount)));

                // 我的任务（TODO + IN_PROGRESS）
                java.util.List<com.oa.schedule.entity.Task> tasks = scheduleService.getTasksByAssignee(userId, null);
                long taskCount = tasks.stream().filter(t -> !"DONE".equals(t.getStatus()) && !"CANCELLED".equals(t.getStatus())).count();
                SwingUtilities.invokeLater(() -> myTaskLabel.setText(String.valueOf(taskCount)));

            } catch (Exception e) {
                // 静默失败，卡片保持 "..."
            }
        });
    }

    /** 切换到指定面板 */
    private void switchTo(String panelKey) {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainFrame)) {
            parent = parent.getParent();
        }
        if (parent instanceof MainFrame) {
            ((MainFrame) parent).showPanel(panelKey);
        }
    }
}