package com.oa.ui.panel;

import com.oa.admin.service.AdminService;
import com.oa.admin.entity.MeetingRoom;
import com.oa.admin.entity.RoomBooking;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * 会议室管理面板 — 会议室CRUD / 预约 / 时段冲突检测 / 我的预约 / 取消预约
 */
public class MeetingRoomPanel extends BasePanel {

    private AdminService adminService = new AdminService();
    private JTable roomTable;

    public MeetingRoomPanel() { initUI(); }

    @Override public String getPanelKey()   { return "ADMIN_ROOM"; }
    @Override public String getPanelTitle() { return "会议室管理"; }

    private void initUI() {
        // 工具栏：新增会议室 + 我的预约 + 刷新
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton addBtn = new JButton("新增会议室");
        addBtn.addActionListener(e -> addRoom());
        JButton myBookBtn = new JButton("我的预约");
        myBookBtn.addActionListener(e -> showMyBookings());
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> refresh());
        toolbar.add(addBtn); toolbar.add(myBookBtn); toolbar.add(refreshBtn);
        add(toolbar, BorderLayout.NORTH);

        String[] columns = {"ID", "会议室", "位置", "容纳人数", "投影仪", "话筒", "状态"};
        roomTable = createTable(columns);
        add(new JScrollPane(roomTable), BorderLayout.CENTER);

        // 底部：预约 + 编辑 + 删除
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bookBtn = new JButton("预约会议室");
        bookBtn.addActionListener(e -> bookRoom());
        JButton editBtn = new JButton("编辑");
        editBtn.addActionListener(e -> editRoom());
        JButton delBtn = new JButton("删除");
        delBtn.addActionListener(e -> deleteRoom());
        bottomPanel.add(bookBtn); bottomPanel.add(editBtn); bottomPanel.add(delBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        List<MeetingRoom> rooms = adminService.getAllRooms();
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

    /** 新增会议室 */
    private void addRoom() {
        JTextField nameField = new JTextField(20);
        JTextField locationField = new JTextField(20);
        JTextField capacityField = new JTextField("10", 20);
        JCheckBox projectorCheck = new JCheckBox("有投影仪");
        JCheckBox micCheck = new JCheckBox("有话筒");

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("名称:")); form.add(nameField);
        form.add(new JLabel("位置:")); form.add(locationField);
        form.add(new JLabel("容纳人数:")); form.add(capacityField);
        form.add(new JLabel("")); form.add(projectorCheck);
        form.add(new JLabel("")); form.add(micCheck);

        if (JOptionPane.showConfirmDialog(this, form, "新增会议室",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            MeetingRoom room = new MeetingRoom();
            room.setRoomName(nameField.getText().trim());
            room.setLocation(locationField.getText().trim());
            room.setCapacity(Integer.parseInt(capacityField.getText().trim()));
            room.setHasProjector(projectorCheck.isSelected() ? 1 : 0);
            room.setHasMicrophone(micCheck.isSelected() ? 1 : 0);
            room.setStatus(1);
            adminService.addRoom(room);
            refresh();
        } catch (Exception ex) { showError("新增失败: " + ex.getMessage()); }
    }

    /** 编辑会议室 */
    private void editRoom() {
        int row = roomTable.getSelectedRow();
        if (row < 0) { showError("请先选择一间会议室"); return; }

        Long id = (Long) roomTable.getValueAt(row, 0);
        JTextField nameField = new JTextField((String) roomTable.getValueAt(row, 1), 20);
        JTextField locationField = new JTextField((String) roomTable.getValueAt(row, 2), 20);
        JTextField capacityField = new JTextField(roomTable.getValueAt(row, 3).toString(), 20);
        JCheckBox projectorCheck = new JCheckBox("", "有".equals(roomTable.getValueAt(row, 4)));
        JCheckBox micCheck = new JCheckBox("", "有".equals(roomTable.getValueAt(row, 5)));

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("名称:")); form.add(nameField);
        form.add(new JLabel("位置:")); form.add(locationField);
        form.add(new JLabel("容纳人数:")); form.add(capacityField);
        form.add(new JLabel("投影仪:")); form.add(projectorCheck);
        form.add(new JLabel("话筒:")); form.add(micCheck);

        if (JOptionPane.showConfirmDialog(this, form, "编辑会议室",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            MeetingRoom room = new MeetingRoom();
            room.setId(id);
            room.setRoomName(nameField.getText().trim());
            room.setLocation(locationField.getText().trim());
            room.setCapacity(Integer.parseInt(capacityField.getText().trim()));
            room.setHasProjector(projectorCheck.isSelected() ? 1 : 0);
            room.setHasMicrophone(micCheck.isSelected() ? 1 : 0);
            room.setStatus("可用".equals(roomTable.getValueAt(row, 6)) ? 1 : 0);
            adminService.updateRoom(room);
            refresh();
        } catch (Exception ex) { showError("编辑失败: " + ex.getMessage()); }
    }

    /** 删除会议室 */
    private void deleteRoom() {
        int row = roomTable.getSelectedRow();
        if (row < 0) { showError("请先选择一间会议室"); return; }
        if (!confirm("确定删除该会议室吗？")) return;
        Long id = (Long) roomTable.getValueAt(row, 0);
        adminService.deleteRoom(id);
        refresh();
    }

    /** 预约会议室 */
    private void bookRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow < 0) { showError("请先选择一间会议室"); return; }

        Long roomId = (Long) roomTable.getValueAt(selectedRow, 0);
        String roomName = (String) roomTable.getValueAt(selectedRow, 1);

        JTextField startField = new JTextField("2026-06-10 14:00", 20);
        JTextField endField = new JTextField("2026-06-10 15:00", 20);
        JTextField purposeField = new JTextField(20);

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("会议室:")); form.add(new JLabel(roomName));
        form.add(new JLabel("开始时间:")); form.add(startField);
        form.add(new JLabel("结束时间:")); form.add(endField);
        form.add(new JLabel("用途:")); form.add(purposeField);

        if (JOptionPane.showConfirmDialog(this, form, "预约会议室",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            RoomBooking booking = new RoomBooking();
            booking.setRoomId(roomId);
            booking.setUserId(getCurrentUserId());
            booking.setStartTime(java.time.LocalDateTime.parse(startField.getText().trim().replace(" ", "T")));
            booking.setEndTime(java.time.LocalDateTime.parse(endField.getText().trim().replace(" ", "T")));
            booking.setPurpose(purposeField.getText().trim());
            booking.setStatus("BOOKED");
            adminService.bookRoom(booking);
            showInfo("预约成功！");
            refresh();
        } catch (Exception ex) { showError("预约失败: " + ex.getMessage()); }
    }

    /** 我的预约列表 */
    private void showMyBookings() {
        List<RoomBooking> bookings = adminService.getMyBookings(getCurrentUserId());
        String[] cols = {"ID", "会议室ID", "开始时间", "结束时间", "用途", "状态"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (RoomBooking b : bookings) {
            model.addRow(new Object[]{
                    b.getId(), b.getRoomId(), b.getStartTime(), b.getEndTime(),
                    b.getPurpose(), b.getStatus()
            });
        }
        JTable bookingTable = new JTable(model);
        bookingTable.setRowHeight(25);

        JButton cancelBtn = new JButton("取消选中预约");
        cancelBtn.addActionListener(e -> {
            int row = bookingTable.getSelectedRow();
            if (row < 0) { showError("请先选择一条预约"); return; }
            if (!confirm("确定取消该预约吗？")) return;
            Long bookingId = (Long) model.getValueAt(row, 0);
            adminService.cancelBooking(bookingId);
            showInfo("已取消");
            showMyBookings(); // 刷新
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        panel.add(cancelBtn, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "我的预约", JOptionPane.PLAIN_MESSAGE);
    }
}