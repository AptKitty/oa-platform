package com.oa.ui.panel;

import com.oa.admin.service.AdminService;
import com.oa.admin.entity.MeetingRoom;
import com.oa.admin.entity.RoomBooking;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class MeetingRoomPanel extends BasePanel {

    private AdminService adminService = new AdminService();
    private JTable roomTable;

    public MeetingRoomPanel() {
        initUI();
    }

    @Override
    public String getPanelKey() { return "ADMIN_ROOM"; }

    @Override
    public String getPanelTitle() { return "会议室预约"; }

    private void initUI() {
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);

        String[] columns = {"ID", "会议室", "位置", "容纳人数", "投影仪", "话筒", "状态"};
        roomTable = createTable(columns);
        add(new JScrollPane(roomTable), BorderLayout.CENTER);

        // 底部预约按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bookBtn = new JButton("预约会议室");
        bookBtn.addActionListener(e -> bookRoom());
        bottomPanel.add(bookBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        java.util.List<MeetingRoom> rooms = adminService.getAllRooms();

        clearTable(roomTable);
        DefaultTableModel model = (DefaultTableModel) roomTable.getModel();
        for (MeetingRoom r : rooms) {
            model.addRow(new Object[]{
                    r.getId(), r.getRoomName(), r.getLocation(), r.getCapacity(),
                    r.getHasProjector() == 1 ? "有" : "无",
                    r.getHasMicrophone() == 1 ? "有" : "无",
                    r.getStatus() == 1 ? "可用" : "不可用"
            });
        }
    }

    private void bookRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("请先在表格里选一间会议室");
            return;
        }

        Long roomId = (Long) roomTable.getValueAt(selectedRow, 0);
        String roomName = (String) roomTable.getValueAt(selectedRow, 1);

        JTextField startField = new JTextField("2026-06-10 14:00", 20);
        JTextField endField = new JTextField("2026-06-10 15:00", 20);
        JTextField purposeField = new JTextField(20);

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("会议室:"));
        form.add(new JLabel(roomName));
        form.add(new JLabel("开始时间:"));
        form.add(startField);
        form.add(new JLabel("结束时间:"));
        form.add(endField);
        form.add(new JLabel("用途:"));
        form.add(purposeField);

        if (JOptionPane.showConfirmDialog(this, form, "预约会议室",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            RoomBooking booking = new RoomBooking();
            booking.setRoomId(roomId);
            booking.setUserId(currentUserId);
            booking.setStartTime(java.time.LocalDateTime.parse(
                    startField.getText().trim().replace(" ", "T")));
            booking.setEndTime(java.time.LocalDateTime.parse(
                    endField.getText().trim().replace(" ", "T")));
            booking.setPurpose(purposeField.getText().trim());

            adminService.bookRoom(booking);
            showInfo("预约成功！");
            refresh();
        } catch (Exception ex) {
            showError("预约失败: " + ex.getMessage());
        }
    }

    // ===== 测试入口 =====
    public static void main(String[] args) {
        JFrame frame = new JFrame("会议室预约");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        MeetingRoomPanel panel = new MeetingRoomPanel();
        panel.setCurrentUser(1L, "测试用户");
        frame.add(panel);
        frame.setVisible(true);
    }
}