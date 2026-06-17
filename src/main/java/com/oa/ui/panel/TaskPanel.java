package com.oa.ui.panel;

import com.oa.schedule.service.ScheduleService;
import com.oa.schedule.entity.Task;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class TaskPanel extends BasePanel {

    private ScheduleService scheduleService = new ScheduleService();
    private JTable taskTable;

    public TaskPanel() {
        initUI();
    }

    @Override
    public String getPanelKey() { return "TASK"; }

    @Override
    public String getPanelTitle() { return "任务管理"; }

    private void initUI() {
        add(createToolBar(this::refresh, this::addTask, null), BorderLayout.NORTH);

        String[] columns = {"ID", "任务标题", "截止日期", "优先级", "状态"};
        taskTable = createTable(columns);
        add(new JScrollPane(taskTable), BorderLayout.CENTER);

        // 底部操作按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editBtn = new JButton("修改");
        editBtn.addActionListener(e -> editTask());
        JButton delBtn = new JButton("删除");
        delBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteTask();
            }
        });
        JButton startBtn = new JButton("开始");
        startBtn.addActionListener(e -> updateStatus("IN_PROGRESS"));
        JButton doneBtn = new JButton("完成");
        doneBtn.addActionListener(e -> updateStatus("DONE"));
        bottomPanel.add(editBtn);
        bottomPanel.add(delBtn);
        bottomPanel.add(startBtn);
        bottomPanel.add(doneBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        java.util.List<Task> tasks = scheduleService.getTasksByAssignee(getCurrentUserId(), null);

        clearTable(taskTable);
        DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
        for (Task t : tasks) {
            // 不显示已取消的任务
            if ("CANCELLED".equals(t.getStatus())) continue;
            model.addRow(new Object[]{
                    t.getId(), t.getTitle(), t.getDueDate(), t.getPriority(), t.getStatus()
            });
        }
    }

    private void addTask() {
        JTextField titleField = new JTextField(20);
        JTextField descField = new JTextField(20);
        JTextField assigneeField = new JTextField(20);
        JTextField dueField = new JTextField("2026-06-15", 20);
        JComboBox<String> priorityBox = new JComboBox<>(new String[]{"NORMAL", "HIGH", "URGENT"});

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("任务标题:"));
        form.add(titleField);
        form.add(new JLabel("描述:"));
        form.add(descField);
        form.add(new JLabel("执行人ID:"));
        form.add(assigneeField);
        form.add(new JLabel("截止日期:"));
        form.add(dueField);
        form.add(new JLabel("优先级:"));
        form.add(priorityBox);

        if (JOptionPane.showConfirmDialog(this, form, "新增任务",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            Task task = new Task();
            task.setTitle(titleField.getText().trim());
            task.setDescription(descField.getText().trim());
            task.setAssignerId(getCurrentUserId());
            task.setAssigneeId(Long.parseLong(assigneeField.getText().trim()));
            task.setDueDate(java.time.LocalDate.parse(dueField.getText().trim()));
            task.setPriority((String) priorityBox.getSelectedItem());
            task.setStatus("TODO");

            scheduleService.addTask(task);
            refresh();
        } catch (Exception ex) {
            showError("新增失败: " + ex.getMessage());
        }
    }

    private void updateStatus(String status) {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow < 0) { showError("请先选择一条任务"); return; }
        try {
            Long taskId = (Long) taskTable.getValueAt(selectedRow, 0);
            scheduleService.updateTaskStatus(taskId, status);
            refresh();
        } catch (Exception ex) {
            showError("操作失败: " + ex.getMessage());
        }
    }

    private void editTask() {
        int row = taskTable.getSelectedRow();
        if (row < 0) { showError("请先选择一条任务"); return; }

        JTextField titleField = new JTextField((String) taskTable.getValueAt(row, 1), 20);
        JTextField dueField = new JTextField(taskTable.getValueAt(row, 2).toString(), 20);
        JComboBox<String> priorityBox = new JComboBox<>(new String[]{"LOW", "NORMAL", "HIGH", "URGENT"});
        priorityBox.setSelectedItem(taskTable.getValueAt(row, 3));
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"TODO", "IN_PROGRESS", "DONE"});
        statusBox.setSelectedItem(taskTable.getValueAt(row, 4));

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("标题:")); form.add(titleField);
        form.add(new JLabel("截止日期:")); form.add(dueField);
        form.add(new JLabel("优先级:")); form.add(priorityBox);
        form.add(new JLabel("状态:")); form.add(statusBox);

        if (JOptionPane.showConfirmDialog(this, form, "修改任务",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            Task task = new Task();
            task.setId((Long) taskTable.getValueAt(row, 0));
            task.setTitle(titleField.getText().trim());
            task.setDueDate(java.time.LocalDate.parse(dueField.getText().trim()));
            task.setPriority((String) priorityBox.getSelectedItem());
            task.setStatus((String) statusBox.getSelectedItem());
            scheduleService.updateTask(task);
            refresh();
        } catch (Exception ex) {
            showError("修改失败: " + ex.getMessage());
        }
    }

    private void deleteTask() {
        int row = taskTable.getSelectedRow();
        if (row < 0) { showError("请先选择一条任务"); return; }
        if (!confirm("确定取消该任务吗？")) return;
        try {
            Long id = (Long) taskTable.getValueAt(row, 0);
            scheduleService.updateTaskStatus(id, "CANCELLED");
            refresh();
        } catch (Exception ex) {
            showError("操作失败: " + ex.getMessage());
        }
    }

    // ===== 测试入口 =====
    public static void main(String[] args) {
        JFrame frame = new JFrame("任务管理");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        TaskPanel panel = new TaskPanel();
        panel.setCurrentUser(1L, "测试用户");
        frame.add(panel);
        frame.setVisible(true);
    }
}