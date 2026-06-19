package com.oa.ui.panel;

import com.oa.schedule.service.ScheduleService;
import com.oa.schedule.entity.CalendarEvent;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * 日程日历面板 — 月/周/日三视图 + 增删改日程
 */
public class CalendarPanel extends BasePanel {

    private ScheduleService scheduleService = new ScheduleService();
    private JPanel calendarGrid;
    private JLabel monthLabel;
    private YearMonth currentMonth;
    private String viewMode = "MONTH"; // MONTH / WEEK / DAY
    private LocalDate selectedDate = LocalDate.now();

    public CalendarPanel() { initUI(); }

    @Override public String getPanelKey()   { return "SCHEDULE"; }
    @Override public String getPanelTitle() { return "日程日历"; }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // === 顶部工具栏 ===
        JPanel topBar = new JPanel(new BorderLayout());
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton prevBtn = new JButton("<<");
        prevBtn.addActionListener(e -> { navigate(-1); });
        JButton nextBtn = new JButton(">>");
        nextBtn.addActionListener(e -> { navigate(1); });
        JButton todayBtn = new JButton("今天");
        todayBtn.addActionListener(e -> { currentMonth = YearMonth.now(); selectedDate = LocalDate.now(); renderView(); });
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        navPanel.add(prevBtn); navPanel.add(monthLabel); navPanel.add(nextBtn); navPanel.add(todayBtn);

        JPanel viewToggle = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 5));
        for (String mode : new String[]{"MONTH", "WEEK", "DAY"}) {
            JButton btn = new JButton(mode.equals("MONTH") ? "月" : mode.equals("WEEK") ? "周" : "日");
            btn.addActionListener(e -> { viewMode = mode; renderView(); });
            viewToggle.add(btn);
        }
        JButton addBtn = new JButton("+ 新建日程");
        addBtn.addActionListener(e -> addEvent());
        viewToggle.add(addBtn);

        topBar.add(navPanel, BorderLayout.WEST);
        topBar.add(viewToggle, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // === 日历网格区域 ===
        calendarGrid = new JPanel();
        add(new JScrollPane(calendarGrid), BorderLayout.CENTER);

        currentMonth = YearMonth.now();
        renderView();
    }

    private void navigate(int delta) {
        switch (viewMode) {
            case "MONTH": currentMonth = currentMonth.plusMonths(delta); break;
            case "WEEK":  selectedDate = selectedDate.plusWeeks(delta); break;
            case "DAY":   selectedDate = selectedDate.plusDays(delta); break;
        }
        renderView();
    }

    /** 根据 viewMode 渲染不同视图 */
    private void renderView() {
        switch (viewMode) {
            case "MONTH": renderMonthView(); break;
            case "WEEK":  renderWeekView(); break;
            case "DAY":   renderDayView(); break;
        }
    }

    // ==================== 月视图 ====================
    private void renderMonthView() {
        calendarGrid.removeAll();
        monthLabel.setText(currentMonth.getYear() + "年 " + currentMonth.getMonthValue() + "月");

        calendarGrid.setLayout(new GridLayout(0, 7, 1, 1));
        calendarGrid.setBackground(Color.LIGHT_GRAY);

        // 星期头
        DayOfWeek[] days = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};
        for (DayOfWeek dow : days) {
            JLabel header = new JLabel(dow.getDisplayName(TextStyle.SHORT, Locale.CHINESE), SwingConstants.CENTER);
            header.setOpaque(true);
            header.setBackground(new Color(63, 81, 181));
            header.setForeground(Color.WHITE);
            header.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
            header.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            calendarGrid.add(header);
        }

        // 计算本月第一天是周几
        LocalDate firstDay = currentMonth.atDay(1);
        int startDow = firstDay.getDayOfWeek().getValue(); // Mon=1
        int daysInMonth = currentMonth.lengthOfMonth();

        // 获取本月事件
        LocalDateTime rangeStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime rangeEnd = currentMonth.atEndOfMonth().atTime(23, 59);
        List<CalendarEvent> events = scheduleService.getUserEvents(getCurrentUserId(), rangeStart, rangeEnd);

        // 填充前面的空白格
        for (int i = 1; i < startDow; i++) {
            calendarGrid.add(new JPanel());
        }

        // 每一天的格子
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            JPanel dayCell = createDayCell(date, events, day);
            calendarGrid.add(dayCell);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JPanel createDayCell(LocalDate date, List<CalendarEvent> events, int dayNum) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(2, 3, 2, 3)));
        cell.setBackground(Color.WHITE);

        // 今天高亮
        if (date.equals(LocalDate.now())) {
            cell.setBackground(new Color(232, 245, 253));
        }

        // 日期标签
        JLabel dayLabel = new JLabel(String.valueOf(dayNum));
        dayLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        if (date.equals(LocalDate.now())) {
            dayLabel.setForeground(new Color(25, 118, 210));
            dayLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        }
        dayLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cell.add(dayLabel);

        // 当天事件（最多显示3个）
        int count = 0;
        for (CalendarEvent e : events) {
            LocalDate eventDate = e.getStartTime().toLocalDate();
            if (eventDate.equals(date)) {
                if (count < 3) {
                    JLabel eventLabel = new JLabel("· " + e.getTitle());
                    eventLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
                    eventLabel.setForeground(new Color(63, 81, 181));
                    eventLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    cell.add(eventLabel);
                }
                count++;
            }
        }
        if (count > 3) {
            JLabel more = new JLabel("  +" + (count - 3) + "条");
            more.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            more.setForeground(Color.GRAY);
            more.setAlignmentX(Component.LEFT_ALIGNMENT);
            cell.add(more);
        }

        // 点击当天显示日程列表
        cell.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectedDate = date;
                    viewMode = "DAY";
                    renderView();
                } else {
                    selectedDate = date;
                    showDayEvents(date);
                }
            }
        });

        cell.setPreferredSize(new Dimension(100, 80));
        return cell;
    }

    // ==================== 周视图 ====================
    private void renderWeekView() {
        calendarGrid.removeAll();
        LocalDate weekStart = selectedDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        monthLabel.setText(weekStart + " ~ " + weekEnd);

        LocalDateTime rangeStart = weekStart.atStartOfDay();
        LocalDateTime rangeEnd = weekEnd.atTime(23, 59);
        List<CalendarEvent> events = scheduleService.getUserEvents(getCurrentUserId(), rangeStart, rangeEnd);

        calendarGrid.setLayout(new GridLayout(0, 8, 1, 1));
        calendarGrid.setBackground(Color.LIGHT_GRAY);

        // 时间列头
        calendarGrid.add(new JLabel(""));
        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStart.plusDays(i);
            JLabel header = new JLabel(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.CHINESE)
                    + " " + d.getDayOfMonth(), SwingConstants.CENTER);
            header.setOpaque(true);
            header.setBackground(new Color(63, 81, 181));
            header.setForeground(Color.WHITE);
            header.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
            calendarGrid.add(header);
        }

        // 时间行 (8:00 - 20:00)
        for (int hour = 8; hour <= 20; hour++) {
            JLabel hourLabel = new JLabel(String.format("%02d:00", hour), SwingConstants.CENTER);
            hourLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            calendarGrid.add(hourLabel);

            for (int i = 0; i < 7; i++) {
                LocalDate d = weekStart.plusDays(i);
                JPanel slot = new JPanel(new BorderLayout());
                slot.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                slot.setBackground(Color.WHITE);
                slot.setPreferredSize(new Dimension(100, 40));

                // 检查这个时间段有没有事件
                for (CalendarEvent e : events) {
                    if (e.getStartTime().toLocalDate().equals(d)
                            && e.getStartTime().getHour() == hour) {
                        JLabel ev = new JLabel(e.getTitle());
                        ev.setFont(new Font("Microsoft YaHei", Font.PLAIN, 9));
                        ev.setBackground(new Color(187, 222, 251));
                        ev.setOpaque(true);
                        slot.add(ev, BorderLayout.CENTER);
                        break;
                    }
                }
                calendarGrid.add(slot);
            }
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    // ==================== 日视图 ====================
    private void renderDayView() {
        calendarGrid.removeAll();
        monthLabel.setText(selectedDate + " " + selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINESE));

        LocalDateTime rangeStart = selectedDate.atStartOfDay();
        LocalDateTime rangeEnd = selectedDate.atTime(23, 59);
        List<CalendarEvent> dayEvents = scheduleService.getUserEvents(getCurrentUserId(), rangeStart, rangeEnd);

        calendarGrid.setLayout(new BorderLayout());

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        if (dayEvents.isEmpty()) {
            JLabel empty = new JLabel("当天没有日程", SwingConstants.CENTER);
            empty.setForeground(Color.GRAY);
            listPanel.add(empty);
        } else {
            for (CalendarEvent e : dayEvents) {
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

                String timeStr = e.getStartTime().toLocalTime().toString().substring(0, 5)
                        + " - " + e.getEndTime().toLocalTime().toString().substring(0, 5);
                JLabel timeLabel = new JLabel(timeStr);
                timeLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
                row.add(timeLabel, BorderLayout.WEST);

                JLabel titleLabel = new JLabel(e.getTitle());
                titleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
                row.add(titleLabel, BorderLayout.CENTER);

                JLabel typeLabel = new JLabel(e.getEventType());
                typeLabel.setForeground(Color.GRAY);
                row.add(typeLabel, BorderLayout.EAST);

                // 删除按钮 X
                JButton delBtn = new JButton("X");
                delBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
                delBtn.setForeground(Color.RED);
                delBtn.setBorderPainted(false);
                delBtn.setContentAreaFilled(false);
                delBtn.setFocusPainted(false);
                delBtn.setToolTipText("删除: " + e.getTitle());
                delBtn.addActionListener(ev -> {
                    if (JOptionPane.showConfirmDialog(CalendarPanel.this,
                            "确认删除日程\n\"" + e.getTitle() + "\" ?",
                            "确认删除", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        scheduleService.deleteEvent(e.getId());
                        renderView();
                    }
                });
                JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
                eastPanel.setOpaque(false);
                eastPanel.add(typeLabel);
                eastPanel.add(delBtn);
                row.add(eastPanel, BorderLayout.EAST);

                // 双击编辑
                row.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent ev) {
                        if (ev.getClickCount() == 2) editEventDialog(e);
                    }
                });

                listPanel.add(row);
            }
        }

        calendarGrid.add(new JScrollPane(listPanel), BorderLayout.CENTER);

        // 底部按钮
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("+ 当天新增日程");
        addBtn.addActionListener(e -> addEventForDate(selectedDate));
        bottom.add(addBtn);
        bottom.add(new JLabel("  双击日程可编辑，点击右侧 X 可删除"));
        calendarGrid.add(bottom, BorderLayout.SOUTH);

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    // ==================== 弹窗：显示某天事件列表 ====================
    private void showDayEvents(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59);
        List<CalendarEvent> events = scheduleService.getUserEvents(getCurrentUserId(), start, end);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        DefaultListModel<String> model = new DefaultListModel<>();
        for (CalendarEvent e : events) {
            String time = e.getStartTime().toLocalTime().toString().substring(0, 5);
            model.addElement("[" + time + "] " + e.getTitle() + " (" + e.getEventType() + ")");
        }
        if (events.isEmpty()) model.addElement("（无日程）");
        JList<String> list = new JList<>(model);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton addBtn = new JButton("新增");
        addBtn.addActionListener(e -> { addEventForDate(date); });
        panel.add(addBtn, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, date + " 日程", JOptionPane.PLAIN_MESSAGE);
        renderView();
    }

    // ==================== 新增日程 ====================
    private void addEvent() {
        addEventForDate(selectedDate);
    }

    private void addEventForDate(LocalDate date) {
        JTextField titleField = new JTextField(20);
        JTextArea descField = new JTextArea(3, 20);
        JTextField startTimeField = new JTextField("09:00", 10);
        JTextField endTimeField = new JTextField("10:00", 10);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"PERSONAL", "MEETING", "TASK"});

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("标题:")); form.add(titleField);
        form.add(new JLabel("描述:")); form.add(new JScrollPane(descField));
        form.add(new JLabel("开始时间:")); form.add(startTimeField);
        form.add(new JLabel("结束时间:")); form.add(endTimeField);
        form.add(new JLabel("类型:")); form.add(typeBox);

        if (JOptionPane.showConfirmDialog(this, form, "新建日程 - " + date,
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            CalendarEvent event = new CalendarEvent();
            event.setUserId(getCurrentUserId());
            event.setTitle(titleField.getText().trim());
            event.setDescription(descField.getText().trim());
            event.setStartTime(LocalDateTime.of(date, LocalTime.parse(startTimeField.getText().trim())));
            event.setEndTime(LocalDateTime.of(date, LocalTime.parse(endTimeField.getText().trim())));
            event.setEventType((String) typeBox.getSelectedItem());
            scheduleService.addEvent(event);
            renderView();
        } catch (Exception ex) { showError("新建失败: " + ex.getMessage()); }
    }

    /** 编辑已有日程 */
    private void editEventDialog(CalendarEvent e) {
        JTextField titleField = new JTextField(e.getTitle(), 20);
        JTextArea descField = new JTextArea(e.getDescription() != null ? e.getDescription() : "", 3, 20);
        JTextField startField = new JTextField(e.getStartTime().toString().replace("T", " "), 20);
        JTextField endField = new JTextField(e.getEndTime().toString().replace("T", " "), 20);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"PERSONAL", "MEETING", "TASK"});
        typeBox.setSelectedItem(e.getEventType());

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("标题:")); form.add(titleField);
        form.add(new JLabel("描述:")); form.add(new JScrollPane(descField));
        form.add(new JLabel("开始时间:")); form.add(startField);
        form.add(new JLabel("结束时间:")); form.add(endField);
        form.add(new JLabel("类型:")); form.add(typeBox);

        if (JOptionPane.showConfirmDialog(this, form, "编辑日程",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            e.setTitle(titleField.getText().trim());
            e.setDescription(descField.getText().trim());
            e.setStartTime(LocalDateTime.parse(startField.getText().trim().replace(" ", "T")));
            e.setEndTime(LocalDateTime.parse(endField.getText().trim().replace(" ", "T")));
            e.setEventType((String) typeBox.getSelectedItem());
            scheduleService.updateEvent(e);
            renderView();
        } catch (Exception ex) { showError("编辑失败: " + ex.getMessage()); }
    }
}