package com.oa.ui.panel;

import com.oa.schedule.service.ScheduleService;
import com.oa.schedule.entity.Task;
import com.oa.common.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import com.oa.system.dao.UserDao;
import com.oa.system.entity.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 * 任务管理面板 — 我的任务 / 我分配的任务 / 分配下属 / 状态追踪
 */
public class TaskPanel extends BasePanel {

    private ScheduleService scheduleService = new ScheduleService();
    private JTable myTaskTable, assignedTaskTable;
    private DefaultTableModel myTaskModel, assignedTaskModel;
    private JTabbedPane tabPane;

    public TaskPanel() { initUI(); }

    @Override public String getPanelKey()   { return "TASK"; }
    @Override public String getPanelTitle() { return "任务管理"; }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // 顶部工具栏
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton addBtn = new JButton("分配新任务");
        addBtn.addActionListener(e -> addTask());
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> refreshAll());
        toolbar.add(addBtn);
        toolbar.add(refreshBtn);
        add(toolbar, BorderLayout.NORTH);

        // 标签页
        tabPane = new JTabbedPane();

        // 标签页1：我的任务
        String[] myCols = {"ID", "任务标题", "分配人", "截止日期", "优先级", "状态"};
        myTaskTable = createTable(myCols);
        myTaskModel = (DefaultTableModel) myTaskTable.getModel();

        JPanel myTaskPanel = new JPanel(new BorderLayout());
        myTaskPanel.add(new JScrollPane(myTaskTable), BorderLayout.CENTER);

        JPanel myActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton startBtn = new JButton("开始");
        startBtn.addActionListener(e -> updateMyTaskStatus("IN_PROGRESS"));
        JButton doneBtn = new JButton("完成");
        doneBtn.addActionListener(e -> updateMyTaskStatus("DONE"));
        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> updateMyTaskStatus("CANCELLED"));
        myActions.add(startBtn); myActions.add(doneBtn); myActions.add(cancelBtn);
        myTaskPanel.add(myActions, BorderLayout.SOUTH);
        tabPane.addTab("我的任务", myTaskPanel);

        // 标签页2：我分配的任务
        String[] assignedCols = {"ID", "任务标题", "执行人", "截止日期", "优先级", "状态"};
        assignedTaskTable = createTable(assignedCols);
        assignedTaskModel = (DefaultTableModel) assignedTaskTable.getModel();

        JPanel assignedTaskPanel = new JPanel(new BorderLayout());
        assignedTaskPanel.add(new JScrollPane(assignedTaskTable), BorderLayout.CENTER);

        JPanel assignedActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton editAssignedBtn = new JButton("修改");
        editAssignedBtn.addActionListener(e -> editAssignedTask());
        JButton delAssignedBtn = new JButton("取消任务");
        delAssignedBtn.addActionListener(e -> cancelAssignedTask());
        assignedActions.add(editAssignedBtn); assignedActions.add(delAssignedBtn);
        assignedTaskPanel.add(assignedActions, BorderLayout.SOUTH);
        tabPane.addTab("我分配的任务", assignedTaskPanel);

        add(tabPane, BorderLayout.CENTER);
        refreshAll();
    }

    private void refreshAll() {
        refreshMyTasks();
        refreshAssignedTasks();
    }

    private void refreshMyTasks() {
        clearTable(myTaskTable);
        List<Task> tasks = scheduleService.getTasksByAssignee(getCurrentUserId(), null);
        for (Task t : tasks) {
            if ("CANCELLED".equals(t.getStatus())) continue;
            String assignerName = "用户#" + t.getAssignerId();
            try {
                UserDao dao = MyBatisUtil.openSession().getMapper(UserDao.class);
                User u = dao.findById(t.getAssignerId());
                if (u != null) assignerName = u.getRealName();
            } catch (Exception ignored) {}
            String statusText = getStatusText(t.getStatus());
            String priorityText = getPriorityText(t.getPriority());
            myTaskModel.addRow(new Object[]{
                    t.getId(), t.getTitle(), assignerName, t.getDueDate(), priorityText, statusText
            });
        }
    }

    /** 刷新"我分配的任务"列表 */
    private void refreshAssignedTasks() {
        clearTable(assignedTaskTable);
        // 查询所有任务然后过滤出当前用户分配的
        // 由于DAO没有按assigner查询的方法，我们在service层处理
        // 这里用 getAllTasks 方式（通过查询所有用户的待办来获取自己分配的）
        // 简单方案：查所有用户的所有任务，过滤 assignerId == currentUserId
        // 当前DAO只有 findTasksByAssignee，没有 findTasksByAssigner
        // 暂时在SQL层面补充：在ScheduleMapper中添加 findTasksByAssigner
        try {
            List<Task> allTasks = new ArrayList<>();
            UserDao userDao = MyBatisUtil.openSession().getMapper(UserDao.class);
            List<User> allUsers = userDao.findByCondition(null, null, 1, 0, 999);
            for (User u : allUsers) {
                List<Task> userTasks = scheduleService.getTasksByAssignee(u.getId(), null);
                for (Task t : userTasks) {
                    if (t.getAssignerId().equals(getCurrentUserId()) && !"CANCELLED".equals(t.getStatus())) {
                        allTasks.add(t);
                    }
                }
            }
            for (Task t : allTasks) {
                String assigneeName = "用户#" + t.getAssigneeId();
                try {
                    User u = userDao.findById(t.getAssigneeId());
                    if (u != null) assigneeName = u.getRealName();
                } catch (Exception ignored) {}
                assignedTaskModel.addRow(new Object[]{
                        t.getId(), t.getTitle(), assigneeName, t.getDueDate(),
                        getPriorityText(t.getPriority()), getStatusText(t.getStatus())
                });
            }
        } catch (Exception ignored) {}
    }

    /** 分配新任务：选执行人、设截止日期、优先级 */
    private void addTask() {
        JTextField titleField = new JTextField(20);
        JTextArea descField = new JTextArea(3, 20);
        JTextField dueField = new JTextField("2026-06-30", 20);
        JComboBox<String> priorityBox = new JComboBox<>(new String[]{"NORMAL", "HIGH", "URGENT"});

        // 用户下拉框：选执行人
        JComboBox<String> userCombo = new JComboBox<>();
        java.util.Map<String, Long> userMap = new java.util.HashMap<>();
        try {
            UserDao userDao = MyBatisUtil.openSession().getMapper(UserDao.class);
            for (User u : userDao.findByCondition(null, null, 1, 0, 999)) {
                String label = u.getRealName() + " (" + u.getUsername() + ")";
                userCombo.addItem(label);
                userMap.put(label, u.getId());
            }
        } catch (Exception ignored) {}

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.add(new JLabel("任务标题:")); form.add(titleField);
        form.add(new JLabel("描述:")); form.add(new JScrollPane(descField));
        form.add(new JLabel("执行人:")); form.add(userCombo);
        form.add(new JLabel("截止日期:")); form.add(dueField);
        form.add(new JLabel("优先级:")); form.add(priorityBox);

        if (JOptionPane.showConfirmDialog(this, form, "分配新任务",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            Task task = new Task();
            task.setTitle(titleField.getText().trim());
            task.setDescription(descField.getText().trim());
            task.setAssignerId(getCurrentUserId());
            String selectedUser = (String) userCombo.getSelectedItem();
            Long assigneeId = userMap.get(selectedUser);
            if (assigneeId == null) { showError("请选择执行人"); return; }
            task.setAssigneeId(assigneeId);
            task.setDueDate(java.time.LocalDate.parse(dueField.getText().trim()));
            task.setPriority((String) priorityBox.getSelectedItem());
            task.setStatus("TODO");
            scheduleService.addTask(task);

            // 发送通知给执行人
            try {
                com.oa.notice.entity.Message msg = new com.oa.notice.entity.Message();
                msg.setSenderId(getCurrentUserId());
                msg.setReceiverId(assigneeId);
                msg.setTitle("新任务分配");
                msg.setContent("您被分配了新任务：【" + task.getTitle() + "】\n截止日期：" + task.getDueDate());
                msg.setMsgType("SYSTEM");
                new com.oa.notice.service.MessageService().send(msg);
            } catch (Exception ignored) {}

            showInfo("任务已分配");
            refreshAll();
        } catch (Exception ex) {
            showError("分配失败: " + ex.getMessage());
        }
    }

    private void updateMyTaskStatus(String status) {
        int row = myTaskTable.getSelectedRow();
        if (row < 0) { showError("请先在「我的任务」中选择一条"); return; }
        try {
            Long id = (Long) myTaskModel.getValueAt(row, 0);
            scheduleService.updateTaskStatus(id, status);
            refreshAll();
        } catch (Exception ex) { showError("操作失败: " + ex.getMessage()); }
    }

    private void editAssignedTask() {
        int row = assignedTaskTable.getSelectedRow();
        if (row < 0) { showError("请先在「我分配的任务」中选择一条"); return; }

        Long id = (Long) assignedTaskModel.getValueAt(row, 0);
        JTextField titleField = new JTextField((String) assignedTaskModel.getValueAt(row, 1), 20);
        JTextField dueField = new JTextField(String.valueOf(assignedTaskModel.getValueAt(row, 3)), 20);
        JComboBox<String> priorityBox = new JComboBox<>(new String[]{"LOW", "NORMAL", "HIGH", "URGENT"});
        priorityBox.setSelectedItem(getPriorityCode(assignedTaskModel.getValueAt(row, 4)));

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("标题:")); form.add(titleField);
        form.add(new JLabel("截止日期:")); form.add(dueField);
        form.add(new JLabel("优先级:")); form.add(priorityBox);

        if (JOptionPane.showConfirmDialog(this, form, "修改任务",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            Task task = new Task();
            task.setId(id);
            task.setTitle(titleField.getText().trim());
            task.setDueDate(java.time.LocalDate.parse(dueField.getText().trim()));
            task.setPriority((String) priorityBox.getSelectedItem());
            scheduleService.updateTask(task);
            refreshAll();
        } catch (Exception ex) { showError("修改失败: " + ex.getMessage()); }
    }

    private void cancelAssignedTask() {
        int row = assignedTaskTable.getSelectedRow();
        if (row < 0) { showError("请先在「我分配的任务」中选择一条"); return; }
        if (!confirm("确定取消该任务吗？")) return;
        Long id = (Long) assignedTaskModel.getValueAt(row, 0);
        scheduleService.updateTaskStatus(id, "CANCELLED");
        refreshAll();
    }

    private String getStatusText(String status) {
        switch (status) {
            case "TODO":        return "待办";
            case "IN_PROGRESS": return "进行中";
            case "DONE":        return "已完成";
            case "CANCELLED":   return "已取消";
            default: return status;
        }
    }

    private String getPriorityText(String priority) {
        switch (priority) {
            case "LOW":    return "低";
            case "NORMAL": return "普通";
            case "HIGH":   return "高";
            case "URGENT": return "紧急";
            default: return priority;
        }
    }

    private String getPriorityCode(Object displayText) {
        String text = String.valueOf(displayText);
        switch (text) {
            case "低":  return "LOW";
            case "普通": return "NORMAL";
            case "高":  return "HIGH";
            case "紧急": return "URGENT";
            default: return text;
        }
    }
}
