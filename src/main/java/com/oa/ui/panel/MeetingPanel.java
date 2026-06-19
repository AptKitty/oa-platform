package com.oa.ui.panel;

import com.oa.schedule.service.ScheduleService;
import com.oa.schedule.entity.Meeting;
import com.oa.notice.service.MessageService;
import com.oa.notice.entity.Message;
import com.oa.common.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import com.oa.system.dao.UserDao;
import com.oa.system.entity.User;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * 会议管理面板 — 发起会议 / 选择参与人 / 冲突检测 / 发送通知
 */
public class MeetingPanel extends BasePanel {

    private ScheduleService scheduleService = new ScheduleService();
    private MessageService messageService = new MessageService();
    private JTable meetingTable;
    private DefaultTableModel tableModel;

    public MeetingPanel() { initUI(); }

    @Override public String getPanelKey()   { return "MEETING"; }
    @Override public String getPanelTitle() { return "会议管理"; }

    private void initUI() {
        add(createToolBar(this::refresh, this::addMeeting, null), BorderLayout.NORTH);

        String[] columns = {"ID", "会议标题", "会议室ID", "发起人", "开始时间", "结束时间", "状态"};
        meetingTable = createTable(columns);
        tableModel = (DefaultTableModel) meetingTable.getModel();

        // 双击查看详情
        meetingTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = meetingTable.getSelectedRow();
                    if (row >= 0) showMeetingDetail(row);
                }
            }
        });

        add(new JScrollPane(meetingTable), BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0);
        java.time.LocalDateTime end = now.plusMonths(1).withDayOfMonth(1);

        List<Meeting> meetings = scheduleService.findMeetingsByTime(start, end);
        clearTable(meetingTable);
        for (Meeting m : meetings) {
            String hostName = "用户#" + m.getHostId();
            try {
                UserDao userDao = MyBatisUtil.openSession().getMapper(UserDao.class);
                User host = userDao.findById(m.getHostId());
                if (host != null) hostName = host.getRealName();
            } catch (Exception ignored) {}

            String statusText;
            switch (m.getStatus()) {
                case "SCHEDULED": statusText = "已安排"; break;
                case "CANCELLED": statusText = "已取消"; break;
                case "FINISHED":  statusText = "已结束"; break;
                default: statusText = m.getStatus();
            }
            tableModel.addRow(new Object[]{
                    m.getId(), m.getTitle(), m.getRoomId(), hostName,
                    m.getStartTime(), m.getEndTime(), statusText
            });
        }
    }

    /** 新建会议：标题 + 会议室 + 时间 + 参与人（多选） + 冲突检测 */
    private void addMeeting() {
        JTextField titleField = new JTextField(20);
        JTextField roomField = new JTextField(20);
        JTextField startField = new JTextField("2026-06-20 09:00", 20);
        JTextField endField   = new JTextField("2026-06-20 10:00", 20);
        JTextArea descField = new JTextArea(3, 20);

        // 获取所有用户供选择参与人
        List<User> allUsers = new ArrayList<>();
        try {
            UserDao userDao = MyBatisUtil.openSession().getMapper(UserDao.class);
            allUsers = userDao.findByCondition(null, null, 1, 0, 999);
        } catch (Exception ignored) {}

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.add(new JLabel("会议标题:")); form.add(titleField);
        form.add(new JLabel("会议室ID:")); form.add(roomField);
        form.add(new JLabel("开始时间:")); form.add(startField);
        form.add(new JLabel("结束时间:")); form.add(endField);
        form.add(new JLabel("描述:")); form.add(new JScrollPane(descField));

        // 参与人多选对话框
        JList<String> userList = new JList<>();
        DefaultListModel<String> userListModel = new DefaultListModel<>();
        for (User u : allUsers) {
            userListModel.addElement(u.getId() + " - " + u.getRealName() + " (" + u.getDeptId() + ")");
        }
        userList.setModel(userListModel);
        userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(350, 150));
        form.add(new JLabel("参与人(可多选):")); form.add(userScroll);

        if (JOptionPane.showConfirmDialog(this, form, "新建会议",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            String title = titleField.getText().trim();
            if (title.isEmpty()) { showError("标题不能为空"); return; }

            java.time.LocalDateTime startTime = java.time.LocalDateTime.parse(startField.getText().trim().replace(" ", "T"));
            java.time.LocalDateTime endTime   = java.time.LocalDateTime.parse(endField.getText().trim().replace(" ", "T"));
            if (!endTime.isAfter(startTime)) { showError("结束时间必须晚于开始时间"); return; }

            // 冲突检测
            List<Meeting> existingMeetings = scheduleService.findMeetingsByTime(startTime, endTime);
            if (!existingMeetings.isEmpty()) {
                StringBuilder sb = new StringBuilder("以下会议与当前时间冲突：\n");
                for (Meeting em : existingMeetings) {
                    sb.append("· ").append(em.getTitle())
                            .append(" (").append(em.getStartTime()).append(" ~ ").append(em.getEndTime()).append(")\n");
                }
                int choice = JOptionPane.showConfirmDialog(this, sb.toString(),
                        "时间冲突警告", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) return;
            }

            Meeting meeting = new Meeting();
            meeting.setTitle(title);
            meeting.setRoomId(Long.parseLong(roomField.getText().trim()));
            meeting.setHostId(getCurrentUserId());
            meeting.setStartTime(startTime);
            meeting.setEndTime(endTime);
            meeting.setDescription(descField.getText().trim());
            meeting.setStatus("SCHEDULED");

            // 收集选中的参与人
            List<Long> participantIds = new ArrayList<>();
            for (int idx : userList.getSelectedIndices()) {
                String item = userListModel.get(idx);
                Long userId = Long.parseLong(item.split(" - ")[0]);
                participantIds.add(userId);
            }
            // 发起人自动加入
            if (!participantIds.contains(getCurrentUserId())) {
                participantIds.add(getCurrentUserId());
            }

            scheduleService.createMeeting(meeting, participantIds);

            // 发送通知给参与人
            for (Long uid : participantIds) {
                if (uid.equals(getCurrentUserId())) continue;
                try {
                    Message msg = new Message();
                    msg.setSenderId(getCurrentUserId());
                    msg.setReceiverId(uid);
                    msg.setTitle("会议邀请");
                    msg.setContent("您被邀请参加：【" + title + "】\n时间：" + startTime + " ~ " + endTime
                            + "\n发起人：用户#" + getCurrentUserId());
                    msg.setMsgType("SYSTEM");
                    messageService.send(msg);
                } catch (Exception ignored) {}
            }

            showInfo("会议创建成功！已通知 " + (participantIds.size() - 1) + " 位参与人");
            refresh();
        } catch (Exception ex) {
            showError("新建失败: " + ex.getMessage());
        }
    }

    private void showMeetingDetail(int row) {
        Long id = (Long) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);
        String room = String.valueOf(tableModel.getValueAt(row, 2));
        String host = (String) tableModel.getValueAt(row, 3);
        String start = String.valueOf(tableModel.getValueAt(row, 4));
        String end = String.valueOf(tableModel.getValueAt(row, 5));
        String status = (String) tableModel.getValueAt(row, 6);

        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("会议：" + title));
        panel.add(new JLabel("会议室ID：" + room));
        panel.add(new JLabel("发起人：" + host));
        panel.add(new JLabel("时间：" + start + " ~ " + end));
        panel.add(new JLabel("状态：" + status));
        panel.add(new JLabel("会议ID：" + id));

        JOptionPane.showMessageDialog(this, panel, "会议详情", JOptionPane.INFORMATION_MESSAGE);
    }
}
