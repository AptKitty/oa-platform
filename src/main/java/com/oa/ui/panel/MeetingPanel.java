package com.oa.ui.panel;

import com.oa.schedule.service.ScheduleService;
import com.oa.schedule.entity.Meeting;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

public class MeetingPanel extends BasePanel {

    private ScheduleService scheduleService = new ScheduleService();
    private JTable meetingTable;

    public MeetingPanel() {
        initUI();
    }

    @Override
    public String getPanelKey() { return "MEETING"; }

    @Override
    public String getPanelTitle() { return "会议管理"; }

    private void initUI() {
        add(createToolBar(this::refresh, this::addMeeting, null), BorderLayout.NORTH);

        String[] columns = {"ID", "会议标题", "会议室ID", "开始时间", "结束时间", "状态"};
        meetingTable = createTable(columns);
        add(new JScrollPane(meetingTable), BorderLayout.CENTER);

        refresh();
    }

    private void refresh() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0);
        java.time.LocalDateTime end = now.plusMonths(1).withDayOfMonth(1);

        java.util.List<Meeting> meetings = scheduleService.findMeetingsByTime(start, end);

        clearTable(meetingTable);
        DefaultTableModel model = (DefaultTableModel) meetingTable.getModel();
        for (Meeting m : meetings) {
            model.addRow(new Object[]{
                    m.getId(), m.getTitle(), m.getRoomId(),
                    m.getStartTime(), m.getEndTime(), m.getStatus()
            });
        }
    }

    private void addMeeting() {
        JTextField titleField = new JTextField(20);
        JTextField roomField = new JTextField(20);
        JTextField startField = new JTextField("2026-06-10 09:00", 20);
        JTextField endField = new JTextField("2026-06-10 10:00", 20);
        JTextField descField = new JTextField(20);

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("会议标题:")); form.add(titleField);
        form.add(new JLabel("会议室ID:")); form.add(roomField);
        form.add(new JLabel("开始时间:")); form.add(startField);
        form.add(new JLabel("结束时间:")); form.add(endField);
        form.add(new JLabel("描述:")); form.add(descField);

        if (JOptionPane.showConfirmDialog(this, form, "新建会议",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            Meeting meeting = new Meeting();
            meeting.setTitle(titleField.getText().trim());
            meeting.setRoomId(Long.parseLong(roomField.getText().trim()));
            meeting.setHostId(currentUserId);
            meeting.setStartTime(java.time.LocalDateTime.parse(startField.getText().trim().replace(" ", "T")));
            meeting.setEndTime(java.time.LocalDateTime.parse(endField.getText().trim().replace(" ", "T")));
            meeting.setDescription(descField.getText().trim());
            meeting.setStatus("SCHEDULED");

            scheduleService.createMeeting(meeting, new ArrayList<>());
            refresh();
        } catch (Exception ex) {
            showError("新建失败: " + ex.getMessage());
        }
    }

    // ===== 测试入口 =====
    public static void main(String[] args) {
        JFrame frame = new JFrame("会议管理");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        MeetingPanel panel = new MeetingPanel();
        panel.setCurrentUser(1L, "测试用户");
        frame.add(panel);
        frame.setVisible(true);
    }
}
