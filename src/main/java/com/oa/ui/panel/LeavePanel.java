package com.oa.ui.panel;

import com.oa.attendance.entity.LeaveRequest;
import com.oa.attendance.service.AttendanceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 请假申请面板 — 选择假期类型 → 填写日期范围 → 查看额度 → 提交
 *
 * 注册 key: "LEAVE"
 * 组员3 负责
 *
 * 界面布局：
 *   上半部分：请假表单（类型/日期/天数/原因 + 提交按钮）
 *   下半部分：历史请假记录表格
 */
public class LeavePanel extends BasePanel {

    private AttendanceService attendanceService;

    // 表单组件
    private JComboBox<String> leaveTypeCombo; //请假类型下拉框
    private JTextField startDateField;        //开始日期（yyyy-mm--dd）
    private JComboBox<String> startHourCombo; //开始时点(09:00/14:00)
    private JTextField endDateField;          //结束日期
    private JComboBox<String> endHourCombo;   //结束时点（18：00/09：00）
    private JLabel durationLabel;             //计算出的天数
    private JLabel quotaLabel;                //剩余额度提示
    private JTextArea reasonArea;             //请假原因

    // ===== 历史记录表格 =====
    private JTable historyTable;
    private DefaultTableModel historyModel;
    
    // ===== 常量 =====
    private static final String[] LEAVE_TYPES = 
        {"年假", "事假", "病假", "婚假", "产假", "其他"};

    public LeavePanel() {
        attendanceService = new AttendanceService();
        initUI();   
        loadQuota();        //初始化时显示额度
        loadHistory();      //加载历史记录
    }

     /** 构建界面 */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ====== 上半：请假表单 ======
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("新建请假申请"));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // 第0行：请假类型 + 额度显示
        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(new JLabel("请假类型："), gbc);
        gbc.gridx = 1;
        leaveTypeCombo = new JComboBox<>(LEAVE_TYPES);
        leaveTypeCombo.addActionListener(e -> loadQuota());  // 切换类型时刷新额度
        fieldsPanel.add(leaveTypeCombo, gbc);
        gbc.gridx = 2;
        quotaLabel = new JLabel("剩余额度: -- 天");
        quotaLabel.setForeground(new Color(25, 118, 210));
        fieldsPanel.add(quotaLabel, gbc);

        // 第1行：开始时间
        gbc.gridx = 0; gbc.gridy = 1;
        fieldsPanel.add(new JLabel("开始时间："), gbc);
        gbc.gridx = 1;
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        startDateField = new JTextField(LocalDate.now().toString(), 10);
        startHourCombo = new JComboBox<>(new String[]{"09:00", "14:00"});
        startPanel.add(startDateField);
        startPanel.add(new JLabel("  "));
        startPanel.add(startHourCombo);
        fieldsPanel.add(startPanel, gbc);

        // 第2行：结束时间
        gbc.gridx = 0; gbc.gridy = 2;
        fieldsPanel.add(new JLabel("结束时间："), gbc);
        gbc.gridx = 1;
        JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        endDateField = new JTextField(LocalDate.now().toString(), 10);
        endHourCombo = new JComboBox<>(new String[]{"18:00", "09:00"});
        endPanel.add(endDateField);
        endPanel.add(new JLabel("  "));
        endPanel.add(endHourCombo);
        fieldsPanel.add(endPanel, gbc);

        // 天数计算按钮 + 显示
        gbc.gridx = 2;
        JButton calcBtn = new JButton("计算天数");
        calcBtn.addActionListener(e -> calcDuration());
        JPanel durPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        durPanel.add(calcBtn);
        durPanel.add(new JLabel(" "));
        durationLabel = new JLabel("0 天");
        durationLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        durPanel.add(durationLabel);
        fieldsPanel.add(durPanel, gbc);

        // 第3行：请假原因
        gbc.gridx = 0; gbc.gridy = 3;
        fieldsPanel.add(new JLabel("请假原因："), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        reasonArea = new JTextArea(3, 30);
        reasonArea.setLineWrap(true);
        fieldsPanel.add(new JScrollPane(reasonArea), gbc);

        formPanel.add(fieldsPanel, BorderLayout.CENTER);

        // 提交按钮
        JButton submitBtn = new JButton("提交请假申请");
        submitBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        submitBtn.setBackground(new Color(76, 175, 80));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> doSubmit());
        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        submitPanel.add(submitBtn);
        formPanel.add(submitPanel, BorderLayout.SOUTH);

        add(formPanel, BorderLayout.NORTH);

        // ====== 下半：历史请假记录表格 ======
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("请假记录"));

        String[] columns = {"ID", "类型", "开始时间", "结束时间", "天数", "原因", "状态"};
        historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(25);
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.getColumnModel().getColumn(0).setMaxWidth(40);
        historyTable.getColumnModel().getColumn(4).setMaxWidth(50);
        historyTable.getColumnModel().getColumn(6).setMaxWidth(60);

        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("刷新记录");
        refreshBtn.addActionListener(e -> loadHistory());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(refreshBtn);
        historyPanel.add(btnPanel, BorderLayout.SOUTH);

        add(historyPanel, BorderLayout.CENTER);
    }

    // ====== 业务方法 ======

    /** 根据起止日期计算请假天数 */
    private void calcDuration() {
        try {
            LocalDate start = LocalDate.parse(startDateField.getText());
            LocalDate end   = LocalDate.parse(endDateField.getText());
            if (end.isBefore(start)) {
                showError("结束日期不能早于开始日期");
                return;
            }
            long days = ChronoUnit.DAYS.between(start, end) + 1;  // 含头尾
            durationLabel.setText(days + " 天");
        } catch (Exception e) {
            showError("日期格式不正确（正确格式：yyyy-MM-dd）");
        }
    }

    /** 查询当前用户所选假期类型的剩余额度 */
    private void loadQuota() {
        try {
            String leaveType = (String) leaveTypeCombo.getSelectedItem();
            int year = LocalDate.now().getYear();
            List<Map<String, Object>> quotas =
                    attendanceService.getLeaveQuota(getCurrentUserId(), year);

            double remaining = 0;
            for (Map<String, Object> q : quotas) {
                if (leaveType.equals(q.get("leave_type"))) {
                    remaining = ((Number) q.get("remaining_days")).doubleValue();
                    break;
                }
            }
            quotaLabel.setText("剩余额度: " + String.format("%.1f", remaining) + " 天");
        } catch (Exception e) {
            quotaLabel.setText("剩余额度: -- 天");
        }
    }

    /** 提交请假申请 */
    private void doSubmit() {
        try {
            String leaveType = (String) leaveTypeCombo.getSelectedItem();
            LocalDate startDate = LocalDate.parse(startDateField.getText());
            LocalDate endDate   = LocalDate.parse(endDateField.getText());
            String reason = reasonArea.getText().trim();

            // 前端校验
            if (reason.isEmpty()) {
                showError("请填写请假原因");
                return;
            }
            if (endDate.isBefore(startDate)) {
                showError("结束日期不能早于开始日期");
                return;
            }

            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;

            // 确认弹窗
            if (!confirm("确认申请 " + leaveType + " " + days + " 天？\n"
                    + startDate + " ~ " + endDate)) return;

            // 构造实体
            LeaveRequest request = new LeaveRequest();
            request.setUserId(getCurrentUserId());
            request.setLeaveType(leaveType);
            request.setStartTime(LocalDateTime.of(startDate, LocalTime.of(9, 0)));
            request.setEndTime(LocalDateTime.of(endDate, LocalTime.of(18, 0)));
            request.setDuration((double) days);
            request.setReason(reason);
            request.setInstanceId(0L);  // 占位：后续联调审批流时替换为实际 instanceId

            // 提交（Service 内做额度+冲突校验）
            attendanceService.applyLeave(request);

            showInfo("请假申请提交成功！");
            clearForm();
            loadQuota();
            loadHistory();
        } catch (Exception e) {
            showError("提交失败：" + e.getMessage());
        }
    }

    /** 加载历史请假记录到表格 */
    private void loadHistory() {
        try {
            List<LeaveRequest> records =
                    attendanceService.getLeaveHistory(getCurrentUserId(), 1, 50);
            historyModel.setRowCount(0);
            for (LeaveRequest r : records) {
                String statusText;
                switch (r.getStatus()) {
                    case "PENDING":   statusText = "待审批"; break;
                    case "APPROVED":  statusText = "已通过"; break;
                    case "REJECTED":  statusText = "已驳回"; break;
                    case "CANCELLED": statusText = "已取消"; break;
                    default:          statusText = r.getStatus();
                }
                historyModel.addRow(new Object[]{
                        r.getId(),
                        r.getLeaveType(),
                        r.getStartTime() != null
                                ? r.getStartTime().toLocalDate().toString() : "",
                        r.getEndTime() != null
                                ? r.getEndTime().toLocalDate().toString() : "",
                        String.format("%.1f", r.getDuration()),
                        r.getReason() != null && r.getReason().length() > 20
                                ? r.getReason().substring(0, 20) + "..."   // 截断过长原因
                                : r.getReason(),
                        statusText
                });
            }
        } catch (Exception e) {
            showError("加载请假记录失败：" + e.getMessage());
        }
    }

    /** 提交成功后清空表单 */
    private void clearForm() {
        startDateField.setText(LocalDate.now().toString());
        endDateField.setText(LocalDate.now().toString());
        reasonArea.setText("");
        durationLabel.setText("0 天");
    }

    // ========== BasePanel 抽象方法 ==========
    @Override
    public String getPanelKey()   { return "LEAVE"; }

    @Override
    public String getPanelTitle() { return "请假申请"; }
}
