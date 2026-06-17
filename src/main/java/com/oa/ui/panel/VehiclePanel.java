package com.oa.ui.panel;

import com.oa.admin.service.AdminService;
import com.oa.admin.entity.Vehicle;
import com.oa.admin.entity.VehicleRecord;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class VehiclePanel extends BasePanel {

    private AdminService adminService = new AdminService();
    private JTable vehicleTable;

    public VehiclePanel() {
        initUI();
    }

    @Override
    public String getPanelKey() { return "VEHICLE"; }

    @Override
    public String getPanelTitle() { return "车辆管理"; }

    private void initUI() {
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);

        String[] columns = {"ID", "车牌号", "车型", "座位数", "状态"};
        vehicleTable = createTable(columns);
        add(new JScrollPane(vehicleTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editBtn = new JButton("修改");
        editBtn.addActionListener(e -> editVehicle());
        JButton outBtn = new JButton("出车");
        outBtn.addActionListener(e -> vehicleAction());
        bottomPanel.add(editBtn);
        bottomPanel.add(outBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        java.util.List<Vehicle> vehicles = adminService.getAllVehicles();

        clearTable(vehicleTable);
        DefaultTableModel model = (DefaultTableModel) vehicleTable.getModel();
        for (Vehicle v : vehicles) {
            model.addRow(new Object[]{
                    v.getId(), v.getPlateNumber(), v.getVehicleModel(),
                    v.getSeats(), v.getStatus()
            });
        }
    }

    private void vehicleAction() {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("请先选择一辆车");
            return;
        }
        Long vehicleId = (Long) vehicleTable.getValueAt(selectedRow, 0);

        JTextField destField = new JTextField(20);
        JTextField purposeField = new JTextField(20);

        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.add(new JLabel("目的地:"));
        form.add(destField);
        form.add(new JLabel("用途:"));
        form.add(purposeField);

        if (JOptionPane.showConfirmDialog(this, form, "出车登记",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        VehicleRecord record = new VehicleRecord();
        record.setVehicleId(vehicleId);
        record.setUserId(getCurrentUserId());
        record.setStartTime(java.time.LocalDateTime.now());
        record.setDestination(destField.getText().trim());
        record.setPurpose(purposeField.getText().trim());

        adminService.addVehicleRecord(record);
        refresh();
    }

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
            v.setStatus((String) vehicleTable.getValueAt(row, 4));
            adminService.updateVehicle(v);
            refresh();
        } catch (Exception ex) {
            showError("修改失败: " + ex.getMessage());
        }
    }

    // ===== 测试入口 =====
    public static void main(String[] args) {
        JFrame frame = new JFrame("车辆管理");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        VehiclePanel panel = new VehiclePanel();
        panel.setCurrentUser(1L, "测试用户");
        frame.add(panel);
        frame.setVisible(true);
    }
}
