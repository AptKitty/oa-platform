package com.oa.ui.panel;

import com.oa.notice.entity.Message;
import com.oa.notice.service.MessageService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 消息中心面板 — 站内消息收件箱
 *
 * 注册 key: "MESSAGE"
 * 组员3 负责
 */
public class MessagePanel extends BasePanel {

    private MessageService messageService;
    private JTable messageTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;
    private JLabel unreadBadge;

    private static final String FILTER_ALL      = "全部消息";
    private static final String FILTER_SYSTEM   = "系统通知";
    private static final String FILTER_APPROVAL = "审批消息";
    private static final String FILTER_UNREAD   = "未读消息";

    public MessagePanel() {
        messageService = new MessageService();
        initUI();
        loadMessages(null);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.add(new JLabel("筛选："));
        filterCombo = new JComboBox<>(new String[]{
                FILTER_ALL, FILTER_SYSTEM, FILTER_APPROVAL, FILTER_UNREAD});
        filterCombo.addActionListener(e -> {
            String selected = (String) filterCombo.getSelectedItem();
            loadMessages(FILTER_UNREAD.equals(selected) ? 0 : null);
        });
        leftPanel.add(filterCombo);
        unreadBadge = new JLabel("");
        unreadBadge.setForeground(Color.RED);
        unreadBadge.setFont(new Font("微软雅黑", Font.BOLD, 13));
        leftPanel.add(unreadBadge);
        toolbar.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton readAllBtn = new JButton("全部标为已读");
        readAllBtn.addActionListener(e -> markAllRead());
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> {
            String filter = (String) filterCombo.getSelectedItem();
            loadMessages(FILTER_UNREAD.equals(filter) ? 0 : null);
        });
        rightPanel.add(readAllBtn);
        rightPanel.add(refreshBtn);
        toolbar.add(rightPanel, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        // 消息列表表格
        String[] columns = {"", "标题", "内容摘要", "发送者", "时间", "类型"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        messageTable = new JTable(tableModel);
        messageTable.setRowHeight(28);
        messageTable.getTableHeader().setReorderingAllowed(false);
        messageTable.getColumnModel().getColumn(0).setMaxWidth(30);
        messageTable.getColumnModel().getColumn(4).setMaxWidth(140);
        messageTable.getColumnModel().getColumn(5).setMaxWidth(80);

        messageTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = messageTable.getSelectedRow();
                if (row >= 0) showMessageDetail(row);
            }
        });
        add(new JScrollPane(messageTable), BorderLayout.CENTER);
    }

    private void loadMessages(Integer isRead) {
        try {
            String filter = (String) filterCombo.getSelectedItem();
            if (isRead == null && FILTER_UNREAD.equals(filter)) isRead = 0;
            List<Message> messages = messageService.findByReceiver(
                    getCurrentUserId(), isRead, 1, 100);
            tableModel.setRowCount(0);
            for (Message msg : messages) {
                if (FILTER_SYSTEM.equals(filter)
                        && !"SYSTEM".equals(msg.getMsgType())) continue;
                if (FILTER_APPROVAL.equals(filter)
                        && !"APPROVAL".equals(msg.getMsgType())) continue;

                String summary = msg.getContent();
                if (summary != null && summary.length() > 30)
                    summary = summary.substring(0, 30) + "...";

                String typeText;
                switch (msg.getMsgType()) {
                    case "SYSTEM":   typeText = "系统通知"; break;
                    case "APPROVAL": typeText = "审批消息"; break;
                    case "CHAT":     typeText = "聊天消息"; break;
                    default:         typeText = msg.getMsgType();
                }
                boolean isUnread = msg.getIsRead() == null || msg.getIsRead() == 0;
                tableModel.addRow(new Object[]{
                        isUnread ? "●" : "",
                        msg.getTitle(), summary != null ? summary : "",
                        msg.getSenderId() == 0 ? "系统" : "用户#" + msg.getSenderId(),
                        msg.getCreateTime() != null
                                ? msg.getCreateTime().toString().replace("T", " ") : "",
                        typeText
                });
            }
            int unreadCount = messageService.getUnreadCount(getCurrentUserId());
            unreadBadge.setText(unreadCount > 0 ? "未读 " + unreadCount + " 条" : "");
        } catch (Exception e) {
            showError("加载消息失败：" + e.getMessage());
        }
    }

    private void showMessageDetail(int row) {
        try {
            String title   = (String) tableModel.getValueAt(row, 1);
            String content = (String) tableModel.getValueAt(row, 2);
            String sender  = (String) tableModel.getValueAt(row, 3);
            String time    = (String) tableModel.getValueAt(row, 4);
            String type    = (String) tableModel.getValueAt(row, 5);

            List<Message> messages = messageService.findByReceiver(
                    getCurrentUserId(), null, 1, 100);
            String fullContent = content;
            Long msgId = null;
            for (Message msg : messages) {
                if (title.equals(msg.getTitle())) {
                    fullContent = msg.getContent();
                    msgId = msg.getId();
                    break;
                }
            }
            if (msgId != null) messageService.markRead(msgId);

            JPanel detailPanel = new JPanel(new BorderLayout(10, 10));
            detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JTextArea contentArea = new JTextArea(fullContent);
            contentArea.setEditable(false);
            contentArea.setLineWrap(true);
            contentArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            JPanel infoPanel = new JPanel(new GridLayout(3, 1, 3, 3));
            infoPanel.add(new JLabel("发送者：" + sender));
            infoPanel.add(new JLabel("时间：" + time));
            infoPanel.add(new JLabel("类型：" + type));
            detailPanel.add(new JLabel(title), BorderLayout.NORTH);
            detailPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
            detailPanel.add(infoPanel, BorderLayout.SOUTH);
            JOptionPane.showMessageDialog(this, detailPanel,
                    "消息详情", JOptionPane.INFORMATION_MESSAGE);

            String filter = (String) filterCombo.getSelectedItem();
            loadMessages(FILTER_UNREAD.equals(filter) ? 0 : null);
        } catch (Exception e) {
            showError("加载消息详情失败：" + e.getMessage());
        }
    }

    private void markAllRead() {
        try {
            messageService.markAllRead(getCurrentUserId());
            showInfo("已全部标为已读");
            loadMessages(null);
        } catch (Exception e) {
            showError("操作失败：" + e.getMessage());
        }
    }

    @Override
    public String getPanelKey()   { return "MESSAGE"; }
    @Override
    public String getPanelTitle() { return "消息中心"; }
}
