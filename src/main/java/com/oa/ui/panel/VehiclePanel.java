package com.oa.ui.panel;

import com.oa.admin.service.AdminService;
import com.oa.admin.entity.Vehicle;
import com.oa.admin.entity.VehicleRecord;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * 用车管理面板 — 车辆CRUD / 出车 / 归队 / 用车记录
 */
public class VehiclePanel extends BasePanel {

    private AdminService adminService = new AdminService();
    private JTable vehicleTable;

    public VehiclePanel() { initUI(); }

    @Override public String getPanelKey()   { return "VEHICLE"; }
    @Override public String getPanelTitle() { return "用车管理"; }

    private void initUI() {
        // 顶部：新增 + 刷新
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton addBtn = new JButton("新增车辆");
        addBtn.addActionListener(e -> addVehicle());
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> refresh());
        toolbar.add(addBtn); toolbar.add(refreshBtn);
        add(toolbar, BorderLayout.NORTH);

        String[] columns = {"ID", "车牌号", "车型", "座位数", "状态"};
        vehicleTable = createTable(columns);
        add(new JScrollPane(vehicleTable), BorderLayout.CENTER);

        // 底部操作按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editBtn = new JButton("修改");
        editBtn.addActionListener(e -> editVehicle());
        JButton outBtn = new JButton("出车");
        outBtn.addActionListener(e -> vehicleOut());
        JButton returnBtn = new JButton("归队");
        returnBtn.addActionListener(e -> vehicleReturn());
        JButton historyBtn = new JButton("用车记录");
        historyBtn.addActionListener(e -> showHistory());
        bottomPanel.add(editBtn); bottomPanel.add(outBtn);
        bottomPanel.add(returnBtn); bottomPanel.add(historyBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        List<Vehicle> vehicles = adminService.getAllVehicles();
        clearTable(vehicleTable);
        DefaultTableModel model = (DefaultTableModel) vehicleTable.getModel();
        for (Vehicle v : vehicles) {
            String statusText;
            switch (v.getStatus()) {
                case "IDLE":     statusText = "待命"; break;
                case "IN_USE":   statusText = "出车中"; break;
                case "SCRAPPED": statusText = "已报废"; break;
                default:         statusText = v.getStatus();
            }
            model.addRow(new Object[]{
                    v.getId(), v.getPlateNumber(), v.getVehicleModel(),
                    v.getSeats(), statusText
            });
        }
    }

    /** 新增车辆 */
    private void addVehicle() {
        JTextField plateField = new JTextField(20);
        JTextField modelField = new JTextField(20);
        JTextField seatsField = new JTextField("5", 20);

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("车牌号:")); form.add(plateField);
        form.add(new JLabel("车型:")); form.add(modelField);
        form.add(new JLabel("座位数:")); form.add(seatsField);

        if (JOptionPane.showConfirmDialog(this, form, "新增车辆",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            Vehicle v = new Vehicle();
            v.setPlateNumber(plateField.getText().trim());
            v.setVehicleModel(modelField.getText().trim());
            v.setSeats(Integer.parseInt(seatsField.getText().trim()));
            v.setStatus("IDLE");
            adminService.addVehicle(v);
            refresh();
        } catch (Exception ex) { showError("新增失败: " + ex.getMessage()); }
    }

    /** 出车 */
    private void vehicleOut() {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow < 0) { showError("请先选择一辆车"); return; }
        Long vehicleId = (Long) vehicleTable.getValueAt(selectedRow, 0);

        JTextField destField = new JTextField(20);
        JTextField purposeField = new JTextField(20);

        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.add(new JLabel("目的地:")); form.add(destField);
        form.add(new JLabel("用途:")); form.add(purposeField);

        if (JOptionPane.showConfirmDialog(this, form, "出车登记",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        VehicleRecord record = new VehicleRecord();
        record.setVehicleId(vehicleId);
        record.setUserId(getCurrentUserId());
        record.setStartTime(java.time.LocalDateTime.now());
        record.setDestination(destField.getText().trim());
        record.setPurpose(purposeField.getText().trim());
        adminService.addVehicleRecord(record);
        adminService.updateVehicleStatus(vehicleId, "IN_USE");
        showInfo("出车登记成功");
        refresh();
    }

    /** 归队 */
    private void vehicleReturn() {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow < 0) { showError("请先选择一辆车"); return; }
        Long vehicleId = (Long) vehicleTable.getValueAt(selectedRow, 0);

        if (!confirm("确认该车辆已归队吗？")) return;
        adminService.updateVehicleStatus(vehicleId, "IDLE");
        showInfo("归队成功");
        refresh();
    }

    /** 查看用车历史 */
    private void showHistory() {
        int row = vehicleTable.getSelectedRow();
        if (row < 0) { showError("请先选择一辆车"); return; }
        Long vehicleId = (Long) vehicleTable.getValueAt(row, 0);

        List<VehicleRecord> records = adminService.getVehicleRecords(vehicleId);
        String[] cols = {"ID", "使用人ID", "出车时间", "目的地", "用途"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (VehicleRecord r : records) {
            model.addRow(new Object[]{
                    r.getId(), r.getUserId(), r.getStartTime(),
                    r.getDestination(), r.getPurpose()
            });
        }
        JTable historyTable = new JTable(model);
        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setPreferredSize(new Dimension(550, 300));
        JOptionPane.showMessageDialog(this, scroll, "用车记录", JOptionPane.PLAIN_MESSAGE);
    }

    /** 修改车辆信息 */
    private void editVehicle() {
        int row = vehicleTable.getSelectedRow();
        if (row < 0) { showError("请先选择一辆车"); return; }

        JTextField plateField = new JTextField((String) vehicleTable.getValueAt(row, 1), 20);
        JTextField modelField = new JTextField((String) vehicleTable.getValueAt(row, 2), 20);
        JTextField seatsField = new JTextField(vehicleTable.getValueAt(row, 3).toString(), 20);

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("车牌号:")); form.add(plateField);
        form.add(new JLabel("车型:")); form.add(modelField);
        form.add(new JLabel("座位数:")); form.add(seatsField);

        if (JOptionPane.showConfirmDialog(this, form, "修改车辆",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            Vehicle v = new Vehicle();
            v.setId((Long) vehicleTable.getValueAt(row, 0));
            v.setPlateNumber(plateField.getText().trim());
            v.setVehicleModel(modelField.getText().trim());
            v.setSeats(Integer.parseInt(seatsField.getText().trim()));
            String statusText = (String) vehicleTable.getValueAt(row, 4);
            if ("待命".equals(statusText)) v.setStatus("IDLE");
            else if ("出车中".equals(statusText)) v.setStatus("IN_USE");
            else if ("已报废".equals(statusText)) v.setStatus("SCRAPPED");
            else v.setStatus(statusText);
            adminService.updateVehicle(v);
            refresh();
        } catch (Exception ex) { showError("修改失败: " + ex.getMessage()); }
    }
}