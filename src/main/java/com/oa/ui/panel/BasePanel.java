package com.oa.ui.panel;

import com.oa.common.Constants;
import com.oa.common.ExportUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public abstract class BasePanel extends JPanel {

    public BasePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public void setCurrentUser(Long userId, String username) {
        Constants.setCurrentUser(userId, username);
    }

    public Long getCurrentUserId()   { return Constants.getCurrentUserId(); }
    public String getCurrentUsername() { return Constants.getCurrentUsername(); }
    public Long getCurrentUserDeptId() { return Constants.getCurrentUserDeptId(); }

    public abstract String getPanelKey();
    public abstract String getPanelTitle();

    protected void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "错误", JOptionPane.ERROR_MESSAGE); }
    protected void showInfo(String msg) { JOptionPane.showMessageDialog(this, msg, "提示", JOptionPane.INFORMATION_MESSAGE); }
    protected boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    protected JTable createTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return new JTable(model);
    }

    protected JPanel createToolBar(Runnable onRefresh, Runnable onAdd, Runnable onExport) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> onRefresh.run());
        toolbar.add(refreshBtn);
        if (onAdd != null) {
            JButton addBtn = new JButton("新增");
            addBtn.addActionListener(e -> onAdd.run());
            toolbar.add(addBtn);
        }
        if (onExport != null) {
            JButton exportBtn = new JButton("导出Excel");
            exportBtn.addActionListener(e -> onExport.run());
            toolbar.add(exportBtn);
        }
        return toolbar;
    }

    /** 在后台线程执行数据库/耗时操作，避免阻塞 UI */
    protected void runAsync(Runnable bgTask) {
        new Thread(() -> {
            try {
                bgTask.run();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> showError(ex.getMessage()));
            }
        }).start();
    }

    protected void clearTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
    }

    protected static final int TEXT_FIELD_WIDTH = 15;
    protected static final int BUTTON_HEIGHT = 28;
}