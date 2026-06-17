package com.oa.ui.panel;                          // ✅ 修复：加上 com.oa 前缀

import com.oa.attendance.service.AttendanceService; // ✅ 修复：完整包路径

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 打卡面板 — 上班/下班打卡 + 最近7天打卡记录
 *
 * 注册 key: "ATTENDANCE"
 * 组员3 负责
 */
public class ClockPanel extends BasePanel {

    private AttendanceService attendanceService;

    // ====== UI 组件 ======
    private JLabel  timeLabel;          // 实时时间显示
    private JLabel  statusLabel;        // 今日打卡状态文字
    private JButton checkInBtn;         // 上班打卡按钮
    private JButton checkOutBtn;        // 下班打卡按钮
    private JTable  recordTable;        // 最近打卡记录表格
    private DefaultTableModel tableModel;
    private Timer timer;                // 每秒刷新时钟

    // ====== 常量 ======
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final LocalTime STANDARD_START_TIME = LocalTime.of(9, 0, 0);
    private static final LocalTime STANDARD_END_TIME   = LocalTime.of(18, 0, 0);

    // ====== 构造 ======
    public ClockPanel() {
        attendanceService = new AttendanceService();   // ✅ 修复：直接用具体类
        initUI();
        loadTodayStatus(getCurrentUserId());
        loadRecentRecords(getCurrentUserId());
        startClock();
    }

    /** 构建界面布局：顶部时钟 → 中部打卡按钮 → 底部记录表格 */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- 顶部：欢迎语 + 实时时钟 ----
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("欢迎您， " + getCurrentUsername() + " ！");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(timeLabel, BorderLayout.EAST);

        // ---- 中部：打卡状态 + 两个打卡按钮 ----
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        statusLabel = new JLabel("今日尚未打卡", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        statusLabel.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        centerPanel.add(statusLabel, gbc);

        checkInBtn = new JButton("上班打卡");
        checkInBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        checkInBtn.setBackground(new Color(76, 175, 80));
        checkInBtn.setForeground(Color.WHITE);
        checkInBtn.addActionListener(e -> doCheckIn());

        checkOutBtn = new JButton("下班打卡");
        checkOutBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        checkOutBtn.setBackground(new Color(33, 150, 243));
        checkOutBtn.setForeground(Color.WHITE);
        checkOutBtn.addActionListener(e -> doCheckOut());

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        centerPanel.add(checkInBtn, gbc);
        gbc.gridx = 1;
        centerPanel.add(checkOutBtn, gbc);

        // ---- 底部：最近7天打卡记录表格 ----
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("最近打卡记录"));

        String[] columns = {"日期", "上班时间", "下班时间", "状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        recordTable = new JTable(tableModel);
        recordTable.setRowHeight(25);
        recordTable.getTableHeader().setReorderingAllowed(false);
        bottomPanel.add(new JScrollPane(recordTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("刷新记录");
        refreshBtn.addActionListener(e -> loadRecentRecords(getCurrentUserId()));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(refreshBtn);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        add(topPanel,    BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ====== 时钟 ======

    /** 启动每秒刷新的实时时钟 */
    private void startClock() {
        timer = new Timer(1000, e ->
                timeLabel.setText(LocalDateTime.now().format(TIME_FORMATTER)));
        timer.start();
    }

    // ====== 打卡状态控制 ======

    /** 查询今日打卡状态，控制按钮的启用/禁用 */
    private void loadTodayStatus(Long userId) {
        try {
            Map<String, Object> todayRecord =
                    attendanceService.getTodayRecord(userId);

            if (todayRecord == null) {
                // 今日完全未打卡
                statusLabel.setText("今日尚未打卡，请点击上班打卡");
                checkInBtn.setEnabled(true);
                checkOutBtn.setEnabled(false);
            } else {
                LocalTime checkInTime  = (LocalTime) todayRecord.get("checkInTime");
                LocalTime checkOutTime = (LocalTime) todayRecord.get("checkOutTime");

                if (checkInTime != null && checkOutTime != null) {
                    // 上下班都已打卡
                    statusLabel.setText("已完成今日打卡（上班：" + checkInTime
                            + "，下班：" + checkOutTime + "）");
                    checkInBtn.setEnabled(false);
                    checkOutBtn.setEnabled(false);
                } else if (checkInTime != null) {
                    // 只打了上班卡
                    statusLabel.setText("已上班打卡（" + checkInTime + "），尚未下班打卡");
                    checkInBtn.setEnabled(false);
                    checkOutBtn.setEnabled(true);
                }
            }
        } catch (Exception e) {
            showError("加载打卡状态失败：" + e.getMessage());
        }
    }

    // ====== 打卡操作 ======

    /** 上班打卡 */
    private void doCheckIn() {
        final Long userId = getCurrentUserId();
        runAsync(() -> {
            LocalDateTime now = LocalDateTime.now();
            try {
                String result = attendanceService.checkIn(userId, "上班", now);
                if (result.startsWith("成功")) {
                    loadTodayStatus(userId);
                    loadRecentRecords(userId);
                    SwingUtilities.invokeLater(() -> showInfo(result));
                } else {
                    SwingUtilities.invokeLater(() -> showError(result));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showError("打卡异常：" + e.getMessage()));
            }
        });
    }

    /** 下班打卡 */
    private void doCheckOut() {
        final Long userId = getCurrentUserId();
        runAsync(() -> {
            LocalDateTime now = LocalDateTime.now();
            try {
                String result = attendanceService.checkIn(userId, "下班", now);
                if (result.startsWith("成功")) {
                    loadTodayStatus(userId);
                    loadRecentRecords(userId);
                    SwingUtilities.invokeLater(() -> showInfo(result));
                } else {
                    SwingUtilities.invokeLater(() -> showError(result));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showError("打卡异常：" + e.getMessage()));
            }
        });
    }

    // ====== 打卡记录加载 ======

    /** 加载最近7天打卡记录到表格 */
    private void loadRecentRecords(Long userId) {
        try {
            List<Map<String, Object>> records =
                    attendanceService.getRecentRecords(userId, 7);
            tableModel.setRowCount(0);
            for (Map<String, Object> rec : records) {
                Object date    = rec.get("attendanceDate");
                Object checkIn = rec.get("checkInTime");
                Object checkOut= rec.get("checkOutTime");
                String status  = (String) rec.getOrDefault("status", "正常");
                tableModel.addRow(new Object[]{
                        date     != null ? date.toString()     : "",
                        checkIn  != null ? checkIn.toString()  : "未打卡",
                        checkOut != null ? checkOut.toString() : "未打卡",
                        status
                });
            }
        } catch (Exception e) {
            showError("加载打卡记录失败：" + e.getMessage());
        }
    }

    // ========== BasePanel 抽象方法 ==========
    @Override
    public String getPanelKey()   { return "CLOCK"; }

    @Override
    public String getPanelTitle() { return "考勤打卡"; }
}
