package com.oa.ui.panel;

import com.oa.notice.entity.Notice;
import com.oa.notice.service.NoticeService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import com.oa.common.PageResult;

/**
 * 公告面板 — 公告列表 / 发布 / 详情 / 已读追踪
 *
 * 注册 key: "NOTICE"
 * 组员3 负责
 */
public class NoticePanel extends BasePanel {

    private NoticeService noticeService;
    private JTable noticeTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public NoticePanel() {
        noticeService = new NoticeService();
        initUI();
        loadNotices(null);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton newBtn = new JButton("新建公告");
        newBtn.addActionListener(e -> showPublishDialog());
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> loadNotices(searchField.getText().trim()));
        leftBtns.add(newBtn);
        leftBtns.add(refreshBtn);

        JPanel rightSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchField = new JTextField(15);
        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> loadNotices(searchField.getText().trim()));
        rightSearch.add(new JLabel("关键字："));
        rightSearch.add(searchField);
        rightSearch.add(searchBtn);

        toolbar.add(leftBtns, BorderLayout.WEST);
        toolbar.add(rightSearch, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        // 公告列表表格
        String[] columns = {"ID", "标题", "发布人", "发布时间", "已读", "未读", "置顶"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        noticeTable = new JTable(tableModel);
        noticeTable.setRowHeight(28);
        noticeTable.getTableHeader().setReorderingAllowed(false);
        noticeTable.getColumnModel().getColumn(0).setMaxWidth(40);
        noticeTable.getColumnModel().getColumn(5).setMaxWidth(50);
        noticeTable.getColumnModel().getColumn(6).setMaxWidth(50);

        noticeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = noticeTable.getSelectedRow();
                if (row >= 0) {
                    Long noticeId = (Long) tableModel.getValueAt(row, 0);
                    showDetailDialog(noticeId);
                }
            }
        });
        add(new JScrollPane(noticeTable), BorderLayout.CENTER);
    }

    private void loadNotices(String keyword) {
        try {
            PageResult<Notice> result = noticeService.findByPage(
                    (keyword == null || keyword.isEmpty()) ? null : keyword, 1, 100);
            tableModel.setRowCount(0);
            for (Notice n : result.getRows()) {
                int readCount = noticeService.getReadCount(n.getId());
                int totalUsers = 10;
                int unreadCount = Math.max(0, totalUsers - readCount);
                tableModel.addRow(new Object[]{
                        n.getId(), n.getTitle(),
                        "用户#" + n.getPublisherId(),
                        n.getCreateTime() != null
                                ? n.getCreateTime().toLocalDate().toString() : "",
                        readCount, unreadCount,
                        n.getIsTop() != null && n.getIsTop() == 1 ? "🔝" : ""
                });
            }
        } catch (Exception e) {
            showError("加载公告失败：" + e.getMessage());
        }
    }

    private void showDetailDialog(Long noticeId) {
        try {
            Notice notice = noticeService.findById(noticeId);
            if (notice == null) { showError("公告不存在"); return; }
            noticeService.markRead(noticeId, getCurrentUserId());

            List<Long> readUserIds = noticeService.getReadUserIds(noticeId);
            String readInfo = "已读 " + readUserIds.size() + " 人";

            JPanel detailPanel = new JPanel(new BorderLayout(10, 10));
            detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JTextArea contentArea = new JTextArea(notice.getContent());
            contentArea.setEditable(false);
            contentArea.setLineWrap(true);
            contentArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            infoPanel.add(new JLabel("发布人：用户#" + notice.getPublisherId()));
            infoPanel.add(new JLabel("发布时间：" +
                    (notice.getCreateTime() != null
                            ? notice.getCreateTime().toString().replace("T", " ") : "")));
            infoPanel.add(new JLabel(readInfo));
            detailPanel.add(new JLabel(notice.getTitle()), BorderLayout.NORTH);
            detailPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
            detailPanel.add(infoPanel, BorderLayout.SOUTH);
            JOptionPane.showMessageDialog(this, detailPanel,
                    "公告详情", JOptionPane.INFORMATION_MESSAGE);
            loadNotices(searchField.getText().trim());
        } catch (Exception e) {
            showError("加载公告详情失败：" + e.getMessage());
        }
    }

    private void showPublishDialog() {
        JTextField titleField = new JTextField(30);
        JTextArea contentArea = new JTextArea(8, 40);
        contentArea.setLineWrap(true);
        JCheckBox topCheck = new JCheckBox("置顶");
        JTextField schedDateField = new JTextField(10);
        JTextField schedTimeField = new JTextField(6);
        schedTimeField.setText("09:00");

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("标题："), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(titleField, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("内容："), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(new JScrollPane(contentArea), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(topCheck, gbc);
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("定时发布："), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        JPanel schedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        schedPanel.add(new JLabel("日期(yyyy-MM-dd):"));
        schedPanel.add(schedDateField);
        schedPanel.add(new JLabel("时间(HH:mm):"));
        schedPanel.add(schedTimeField);
        formPanel.add(schedPanel, gbc);
        panel.add(formPanel, BorderLayout.CENTER);

        if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(this, panel,
                "新建公告", JOptionPane.OK_CANCEL_OPTION)) return;

        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        if (title.isEmpty() || content.isEmpty()) { showError("标题和内容不能为空"); return; }
        try {
            Notice notice = new Notice();
            notice.setTitle(title);
            notice.setContent(content);
            notice.setPublisherId(getCurrentUserId());
            notice.setIsTop(topCheck.isSelected() ? 1 : 0);
            String schedDate = schedDateField.getText().trim();
            String schedTime = schedTimeField.getText().trim();
            if (!schedDate.isEmpty() && !schedTime.isEmpty()) {
                notice.setScheduledTime(java.time.LocalDateTime.parse(schedDate + "T" + schedTime + ":00"));
            }
            notice.setStatus(1);
            noticeService.publish(notice);
            showInfo("公告发布成功！");
            loadNotices(null);
        } catch (Exception e) {
            showError("发布失败：" + e.getMessage());
        }
    }

    @Override
    public String getPanelKey()   { return "NOTICE"; }
    @Override
    public String getPanelTitle() { return "公告消息"; }
}
