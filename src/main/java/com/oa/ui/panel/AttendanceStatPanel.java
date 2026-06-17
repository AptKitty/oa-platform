package com.oa.ui.panel;

import com.oa.attendance.service.AttendanceService;
import com.oa.common.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤统计面板 — 月度全员考勤汇总表 + 个人明细 + Excel 导出
 *
 * 注册 key: "ATTENDANCE_STAT"
 * 组员3 负责
 */
public class AttendanceStatPanel extends BasePanel {

    private AttendanceService attendanceService;
    private JComboBox<Integer> yearCombo;
    private JComboBox<Integer> monthCombo;
    private JTable statTable;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;

    public AttendanceStatPanel() {
        attendanceService = new AttendanceService();
        initUI();
        loadStats();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部：年月选择 + 操作按钮
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.add(new JLabel("年份："));
        int thisYear = LocalDate.now().getYear();
        yearCombo = new JComboBox<>();
        for (int y = thisYear - 2; y <= thisYear + 1; y++) yearCombo.addItem(y);
        yearCombo.setSelectedItem(thisYear);
        topPanel.add(yearCombo);
        topPanel.add(new JLabel("月份："));
        monthCombo = new JComboBox<>();
        for (int m = 1; m <= 12; m++) monthCombo.addItem(m);
        monthCombo.setSelectedItem(LocalDate.now().getMonthValue());
        topPanel.add(monthCombo);
        JButton queryBtn = new JButton("查询");
        queryBtn.addActionListener(e -> loadStats());
        topPanel.add(queryBtn);
        JButton exportBtn = new JButton("导出 Excel");
        exportBtn.addActionListener(e -> exportToExcel());
        topPanel.add(exportBtn);
        summaryLabel = new JLabel("");
        summaryLabel.setForeground(new Color(25, 118, 210));
        topPanel.add(summaryLabel);
        add(topPanel, BorderLayout.NORTH);

        // 统计表格
        String[] columns = {"姓名", "部门", "应出勤", "实际出勤", "迟到", "早退", "缺勤", "请假(天)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        statTable = new JTable(tableModel);
        statTable.setRowHeight(25);
        statTable.getTableHeader().setReorderingAllowed(false);
        statTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = statTable.getSelectedRow();
                    if (row >= 0) {
                        String name = (String) tableModel.getValueAt(row, 0);
                        showUserDetail(name);
                    }
                }
            }
        });
        add(new JScrollPane(statTable), BorderLayout.CENTER);
    }

    private void loadStats() {
        try {
            int year = (int) yearCombo.getSelectedItem();
            int month = (int) monthCombo.getSelectedItem();
            List<Map<String, Object>> stats =
                    attendanceService.getMonthlyStatsAllUsers(year, month);
            tableModel.setRowCount(0);
            int totalLate = 0, totalEarly = 0, totalAbsent = 0;
            for (Map<String, Object> row : stats) {
                String name    = (String) row.getOrDefault("realName", "未知");
                String dept    = (String) row.getOrDefault("deptName", "-");
                int workDays   = ((Number) row.getOrDefault("workDays", 22)).intValue();
                int actualDays = ((Number) row.getOrDefault("actualDays", 0)).intValue();
                int lateCount  = ((Number) row.getOrDefault("lateCount", 0)).intValue();
                int earlyCount = ((Number) row.getOrDefault("earlyCount", 0)).intValue();
                int absentDays = ((Number) row.getOrDefault("absentDays", 0)).intValue();
                double leaveDays = ((Number) row.getOrDefault("leaveDays", 0.0)).doubleValue();
                totalLate   += lateCount;
                totalEarly  += earlyCount;
                totalAbsent += absentDays;
                tableModel.addRow(new Object[]{
                        name, dept, workDays, actualDays,
                        lateCount, earlyCount, absentDays,
                        String.format("%.1f", leaveDays)
                });
            }
            summaryLabel.setText(String.format(
                    "共 %d 人 | 迟到合计 %d 次 | 早退合计 %d 次 | 缺勤合计 %d 天",
                    stats.size(), totalLate, totalEarly, totalAbsent));
        } catch (Exception e) {
            showError("加载统计数据失败：" + e.getMessage()
                    + "\n\n提示：需在 AttendanceService 中完善 getMonthlyStatsAllUsers 方法。");
        }
    }

    private void showUserDetail(String userName) {
        try {
            int year = (int) yearCombo.getSelectedItem();
            int month = (int) monthCombo.getSelectedItem();
            List<Map<String, Object>> details =
                    attendanceService.getMonthlyStats(year, month);
            String[] columns = {"日期", "上班打卡", "下班打卡", "状态"};
            DefaultTableModel detailModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            for (Map<String, Object> d : details) {
                detailModel.addRow(new Object[]{
                        d.getOrDefault("date", ""),
                        d.getOrDefault("checkInTime", "未打卡"),
                        d.getOrDefault("checkOutTime", "未打卡"),
                        "-"
                });
            }
            JTable detailTable = new JTable(detailModel);
            detailTable.setRowHeight(22);
            JScrollPane scrollPane = new JScrollPane(detailTable);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            JOptionPane.showMessageDialog(this, scrollPane,
                    userName + " - " + year + "年" + month + "月考勤明细",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            showError("加载明细失败：" + e.getMessage());
        }
    }

    private void exportToExcel() {
        try {
            String filename = "考勤统计_" + yearCombo.getSelectedItem()
                    + "年" + monthCombo.getSelectedItem() + "月";
            ExportUtil.exportToExcel(statTable, filename);
            showInfo("导出成功：" + filename + ".xlsx");
        } catch (Exception e) {
            showError("导出失败：" + e.getMessage());
        }
    }

    @Override
    public String getPanelKey()   { return "ATTENDANCE_STAT"; }
    @Override
    public String getPanelTitle() { return "考勤统计"; }
}
