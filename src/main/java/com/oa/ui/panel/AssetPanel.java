package com.oa.ui.panel;

import com.oa.admin.service.AdminService;
import com.oa.admin.entity.Asset;
import com.oa.admin.entity.AssetRecord;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class AssetPanel extends BasePanel {

    private AdminService adminService = new AdminService();
    private JTable assetTable;

    public AssetPanel() {
        initUI();
    }

    @Override
    public String getPanelKey() { return "ASSET"; }

    @Override
    public String getPanelTitle() { return "资产管理"; }

    private void initUI() {
        add(createToolBar(this::refresh, this::addAsset, this::exportExcel), BorderLayout.NORTH);

        String[] columns = {"ID", "资产名称", "编码", "分类", "型号", "状态"};
        assetTable = createTable(columns);
        add(new JScrollPane(assetTable), BorderLayout.CENTER);

        // 底部操作按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editBtn = new JButton("修改");
        editBtn.addActionListener(e -> editAsset());
        JButton borrowBtn = new JButton("领用");
        borrowBtn.addActionListener(e -> changeStatus("BORROW"));
        JButton returnBtn = new JButton("归还");
        returnBtn.addActionListener(e -> changeStatus("RETURN"));
        bottomPanel.add(editBtn);
        bottomPanel.add(borrowBtn);
        bottomPanel.add(returnBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        java.util.List<Asset> assets = adminService.getAllAssets(null);

        clearTable(assetTable);
        DefaultTableModel model = (DefaultTableModel) assetTable.getModel();
        for (Asset a : assets) {
            model.addRow(new Object[]{
                    a.getId(), a.getAssetName(), a.getAssetCode(),
                    a.getCategory(), a.getModel(), a.getStatus()
            });
        }
    }

    private void addAsset() {
        JTextField nameField = new JTextField(20);
        JTextField codeField = new JTextField(20);
        JTextField categoryField = new JTextField(20);
        JTextField modelField = new JTextField(20);

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("资产名称:"));
        form.add(nameField);
        form.add(new JLabel("资产编码:"));
        form.add(codeField);
        form.add(new JLabel("分类:"));
        form.add(categoryField);
        form.add(new JLabel("型号:"));
        form.add(modelField);

        if (JOptionPane.showConfirmDialog(this, form, "新增资产",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            Asset asset = new Asset();
            asset.setAssetName(nameField.getText().trim());
            asset.setAssetCode(codeField.getText().trim());
            asset.setCategory(categoryField.getText().trim());
            asset.setModel(modelField.getText().trim());
            asset.setStatus("IDLE");

            adminService.addAsset(asset);
            refresh();
        } catch (Exception ex) {
            showError("新增失败: " + ex.getMessage());
        }
    }

    private void changeStatus(String action) {
        int selectedRow = assetTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("请先选择一条资产");
            return;
        }
        Long assetId = (Long) assetTable.getValueAt(selectedRow, 0);

        AssetRecord record = new AssetRecord();
        record.setAssetId(assetId);
        record.setUserId(getCurrentUserId());
        record.setAction(action);
        record.setActionTime(java.time.LocalDateTime.now());

        try {
            if ("BORROW".equals(action)) {
                adminService.borrowAsset(record);
            } else {
                adminService.returnAsset(record);
            }
            refresh();
        } catch (Exception ex) {
            showError("操作失败: " + ex.getMessage());
        }
    }

    private void editAsset() {
        int row = assetTable.getSelectedRow();
        if (row < 0) { showError("请先选择一条资产"); return; }

        JTextField nameField = new JTextField((String) assetTable.getValueAt(row, 1), 20);
        JTextField codeField = new JTextField((String) assetTable.getValueAt(row, 2), 20);
        JTextField categoryField = new JTextField((String) assetTable.getValueAt(row, 3), 20);
        JTextField modelField = new JTextField((String) assetTable.getValueAt(row, 4), 20);

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("资产名称:")); form.add(nameField);
        form.add(new JLabel("资产编码:")); form.add(codeField);
        form.add(new JLabel("分类:")); form.add(categoryField);
        form.add(new JLabel("型号:")); form.add(modelField);

        if (JOptionPane.showConfirmDialog(this, form, "修改资产",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            Asset asset = new Asset();
            asset.setId((Long) assetTable.getValueAt(row, 0));
            asset.setAssetName(nameField.getText().trim());
            asset.setAssetCode(codeField.getText().trim());
            asset.setCategory(categoryField.getText().trim());
            asset.setModel(modelField.getText().trim());
            asset.setStatus((String) assetTable.getValueAt(row, 5));
            adminService.updateAsset(asset);
            refresh();
        } catch (Exception ex) {
            showError("修改失败: " + ex.getMessage());
        }
    }

    private void exportExcel() {
        com.oa.common.ExportUtil.exportToExcel(assetTable, "资产列表");
    }

    // ===== 测试入口 =====
    public static void main(String[] args) {
        JFrame frame = new JFrame("资产管理");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        AssetPanel panel = new AssetPanel();
        panel.setCurrentUser(1L, "测试用户");
        frame.add(panel);
        frame.setVisible(true);
    }
}