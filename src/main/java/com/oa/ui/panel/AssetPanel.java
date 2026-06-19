package com.oa.ui.panel;

import com.oa.admin.service.AdminService;
import com.oa.admin.entity.Asset;
import com.oa.admin.entity.AssetRecord;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * 资产管理面板 — 登记/领用/归还/报废 + 操作历史追踪
 */
public class AssetPanel extends BasePanel {

    private AdminService adminService = new AdminService();
    private JTable assetTable;

    public AssetPanel() { initUI(); }

    @Override public String getPanelKey()   { return "ASSET"; }
    @Override public String getPanelTitle() { return "资产管理"; }

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
        JButton scrapBtn = new JButton("报废");
        scrapBtn.addActionListener(e -> scrapAsset());
        JButton historyBtn = new JButton("操作历史");
        historyBtn.addActionListener(e -> showHistory());
        bottomPanel.add(editBtn); bottomPanel.add(borrowBtn);
        bottomPanel.add(returnBtn); bottomPanel.add(scrapBtn); bottomPanel.add(historyBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        List<Asset> assets = adminService.getAllAssets(null);
        clearTable(assetTable);
        DefaultTableModel model = (DefaultTableModel) assetTable.getModel();
        for (Asset a : assets) {
            String statusText;
            switch (a.getStatus()) {
                case "IDLE":     statusText = "闲置"; break;
                case "IN_USE":   statusText = "使用中"; break;
                case "SCRAPPED": statusText = "已报废"; break;
                default:         statusText = a.getStatus();
            }
            model.addRow(new Object[]{
                    a.getId(), a.getAssetName(), a.getAssetCode(),
                    a.getCategory(), a.getModel(), statusText
            });
        }
    }

    /** 新增资产 */
    private void addAsset() {
        JTextField nameField = new JTextField(20);
        JTextField codeField = new JTextField(20);
        JTextField categoryField = new JTextField(20);
        JTextField modelField = new JTextField(20);

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("资产名称:")); form.add(nameField);
        form.add(new JLabel("资产编码:")); form.add(codeField);
        form.add(new JLabel("分类:")); form.add(categoryField);
        form.add(new JLabel("型号:")); form.add(modelField);

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
        } catch (Exception ex) { showError("新增失败: " + ex.getMessage()); }
    }

    /** 领用/归还（状态自动更新） */
    private void changeStatus(String action) {
        int selectedRow = assetTable.getSelectedRow();
        if (selectedRow < 0) { showError("请先选择一条资产"); return; }
        Long assetId = (Long) assetTable.getValueAt(selectedRow, 0);

        AssetRecord record = new AssetRecord();
        record.setAssetId(assetId);
        record.setUserId(getCurrentUserId());
        record.setAction(action);
        record.setActionTime(java.time.LocalDateTime.now());

        try {
            if ("BORROW".equals(action)) {
                adminService.borrowAsset(record);
                showInfo("领用成功，状态已更新为「使用中」");
            } else {
                adminService.returnAsset(record);
                showInfo("归还成功，状态已更新为「闲置」");
            }
            refresh();
        } catch (Exception ex) { showError("操作失败: " + ex.getMessage()); }
    }

    /** 报废 */
    private void scrapAsset() {
        int row = assetTable.getSelectedRow();
        if (row < 0) { showError("请先选择一条资产"); return; }
        if (!confirm("确定报废该资产吗？报废后不可恢复。")) return;
        Long id = (Long) assetTable.getValueAt(row, 0);
        adminService.scrapAsset(id);
        showInfo("已报废");
        refresh();
    }

    /** 查看操作历史 */
    private void showHistory() {
        int row = assetTable.getSelectedRow();
        if (row < 0) { showError("请先选择一条资产"); return; }
        Long assetId = (Long) assetTable.getValueAt(row, 0);

        List<AssetRecord> records = adminService.getAssetRecords(assetId);
        String[] cols = {"ID", "操作人ID", "操作", "时间", "备注"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (AssetRecord r : records) {
            String actionText;
            switch (r.getAction()) {
                case "BORROW": actionText = "领用"; break;
                case "RETURN": actionText = "归还"; break;
                default: actionText = r.getAction();
            }
            model.addRow(new Object[]{r.getId(), r.getUserId(), actionText, r.getActionTime(), r.getRemark()});
        }
        JTable historyTable = new JTable(model);
        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setPreferredSize(new Dimension(550, 300));
        JOptionPane.showMessageDialog(this, scroll, "操作历史", JOptionPane.PLAIN_MESSAGE);
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
            // 保持原状态（需要把中文转回英文）
            String statusText = (String) assetTable.getValueAt(row, 5);
            if ("闲置".equals(statusText)) asset.setStatus("IDLE");
            else if ("使用中".equals(statusText)) asset.setStatus("IN_USE");
            else if ("已报废".equals(statusText)) asset.setStatus("SCRAPPED");
            else asset.setStatus(statusText);
            adminService.updateAsset(asset);
            refresh();
        } catch (Exception ex) { showError("修改失败: " + ex.getMessage()); }
    }

    private void exportExcel() {
        com.oa.common.ExportUtil.exportToExcel(assetTable, "资产列表");
    }
}