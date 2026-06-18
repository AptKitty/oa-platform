package com.oa.ui.panel;

import com.oa.common.ExportUtil;
import com.oa.common.PageResult;
import com.oa.system.entity.Dept;
import com.oa.system.entity.User;
import com.oa.system.service.DeptService;
import com.oa.system.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 用户管理面板 — 增删改查 + 分页 + 导出
 */
public class UserManagePanel extends BasePanel {

    private final UserService userService = new UserService();
    private final DeptService deptService = new DeptService();

    private JTextField keywordField;
    private JComboBox<String> deptCombo;
    private JComboBox<String> statusCombo;
    private JTable table;
    private DefaultTableModel tableModel;

    private int currentPage = 1;
    private final int pageSize = 15;
    private long totalRows;

    private static final String[] COLUMNS = {"ID", "用户名", "姓名", "手机", "邮箱", "部门", "岗位", "状态", "创建时间"};

    public UserManagePanel() {
        super();
        initUI();
    }

    @Override
    public String getPanelKey() { return "USER_MANAGE"; }

    @Override
    public String getPanelTitle() { return "用户管理"; }

    private void initUI() {
        // === 顶部搜索栏 ===
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchBar.add(new JLabel("关键词:"));
        keywordField = new JTextField(TEXT_FIELD_WIDTH);
        searchBar.add(keywordField);
        searchBar.add(new JLabel("部门:"));
        deptCombo = new JComboBox<>();
        deptCombo.addItem("全部");
        searchBar.add(deptCombo);
        searchBar.add(new JLabel("状态:"));
        statusCombo = new JComboBox<>(new String[]{"全部", "正常", "禁用"});
        searchBar.add(statusCombo);
        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> { currentPage = 1; loadData(); });
        searchBar.add(searchBtn);
        add(searchBar, BorderLayout.NORTH);

        // === 中间表格 ===
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // === 底部: 工具栏 + 翻页 ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel toolbar = createToolBar(
                this::loadData,
                this::showAddDialog,
                () -> ExportUtil.exportToExcel(table, "用户列表")
        );
        JButton editBtn = new JButton("编辑");
        editBtn.addActionListener(e -> showEditDialog());
        toolbar.add(editBtn);
        JButton delBtn = new JButton("删除");
        delBtn.addActionListener(e -> deleteUser());
        toolbar.add(delBtn);
        JButton resetBtn = new JButton("重置密码");
        resetBtn.addActionListener(e -> resetPassword());
        toolbar.add(resetBtn);
        bottomPanel.add(toolbar, BorderLayout.WEST);

        JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton prevBtn = new JButton("上一页");
        JButton nextBtn = new JButton("下一页");
        JLabel pageLabel = new JLabel("第 1 页");
        prevBtn.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        nextBtn.addActionListener(e -> { if (currentPage * pageSize < totalRows) { currentPage++; loadData(); } });
        pagePanel.add(prevBtn);
        pagePanel.add(pageLabel);
        pagePanel.add(nextBtn);
        bottomPanel.add(pagePanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        loadDeptCombo();
        loadData();
    }

    private void loadDeptCombo() {
        deptCombo.removeAllItems();
        deptCombo.addItem("全部");
        for (Dept d : deptService.findAll()) {
            deptCombo.addItem(d.getDeptName());
        }
    }

    private void loadData() {
        String keyword = keywordField.getText().trim();
        if (keyword.isEmpty()) keyword = null;
        Long deptId = null;
        if (deptCombo.getSelectedIndex() > 0) {
            List<Dept> depts = deptService.findAll();
            int idx = deptCombo.getSelectedIndex() - 1;
            if (idx < depts.size()) deptId = depts.get(idx).getId();
        }
        // 数据权限: 非管理员只能看本部门
        if (deptId == null && getCurrentUserDeptId() != null && getCurrentUserDeptId() != 1L) {
            deptId = getCurrentUserDeptId();
        }
        Integer status = null;
        int si = statusCombo.getSelectedIndex();
        if (si == 1) status = 1;
        else if (si == 2) status = 0;

        PageResult<User> page = userService.findByPage(keyword, deptId, status, currentPage, pageSize);
        totalRows = page.getTotal();
        clearTable(table);
        for (User u : page.getRows()) {
            tableModel.addRow(new Object[]{
                    u.getId(), u.getUsername(), u.getRealName(), u.getPhone(), u.getEmail(),
                    getDeptName(u.getDeptId()), u.getPosition(),
                    u.getStatus() == 1 ? "正常" : "禁用",
                    u.getCreateTime()
            });
        }
    }

    private String getDeptName(Long deptId) {
        if (deptId == null) return "";
        Dept d = deptService.findById(deptId);
        return d != null ? d.getDeptName() : "";
    }

    private void showAddDialog() { showEditDialogInternal(null); }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("请先选择一条记录"); return; }
        Long userId = (Long) tableModel.getValueAt(row, 0);
        User user = userService.findById(userId);
        if (user == null) { showError("用户不存在"); return; }
        showEditDialogInternal(user);
    }

    private void showEditDialogInternal(User user) {
        boolean isEdit = (user != null);
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "编辑用户" : "新增用户", true);
        dialog.setSize(420, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        JTextField usernameF = new JTextField(TEXT_FIELD_WIDTH);
        if (isEdit) { usernameF.setText(user.getUsername()); usernameF.setEnabled(false); }
        form.add(usernameF, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel(isEdit ? "新密码(留空不变):" : "密码:"), gbc);
        gbc.gridx = 1;
        JPasswordField pwdF = new JPasswordField(TEXT_FIELD_WIDTH);
        form.add(pwdF, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("姓名:"), gbc);
        gbc.gridx = 1;
        JTextField nameF = new JTextField(TEXT_FIELD_WIDTH);
        if (isEdit) nameF.setText(user.getRealName());
        form.add(nameF, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("手机:"), gbc);
        gbc.gridx = 1;
        JTextField phoneF = new JTextField(TEXT_FIELD_WIDTH);
        if (isEdit) phoneF.setText(user.getPhone() != null ? user.getPhone() : "");
        form.add(phoneF, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(new JLabel("邮箱:"), gbc);
        gbc.gridx = 1;
        JTextField emailF = new JTextField(TEXT_FIELD_WIDTH);
        if (isEdit) emailF.setText(user.getEmail() != null ? user.getEmail() : "");
        form.add(emailF, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        form.add(new JLabel("部门:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> deptBox = new JComboBox<>();
        List<Dept> depts = deptService.findAll();
        int selectedIdx = 0;
        for (int i = 0; i < depts.size(); i++) {
            Dept d = depts.get(i);
            deptBox.addItem(d.getDeptName());
            if (isEdit && d.getId().equals(user.getDeptId())) selectedIdx = i;
        }
        deptBox.setSelectedIndex(selectedIdx);
        form.add(deptBox, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        form.add(new JLabel("岗位:"), gbc);
        gbc.gridx = 1;
        JTextField posF = new JTextField(TEXT_FIELD_WIDTH);
        if (isEdit) posF.setText(user.getPosition() != null ? user.getPosition() : "");
        form.add(posF, gbc);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton saveBtn = new JButton("保存");
        saveBtn.addActionListener(e -> {
            String username = usernameF.getText().trim();
            String name = nameF.getText().trim();
            if (username.isEmpty() || name.isEmpty()) { showError("用户名和姓名不能为空"); return; }
            if (!isEdit && new String(pwdF.getPassword()).isEmpty()) { showError("密码不能为空"); return; }
            User u = isEdit ? user : new User();
            u.setUsername(username);
            if (new String(pwdF.getPassword()).length() > 0) {
                u.setPassword(com.oa.common.MD5Util.md5(new String(pwdF.getPassword())));
            }
            u.setRealName(name);
            u.setPhone(phoneF.getText().trim());
            u.setEmail(emailF.getText().trim());
            if (deptBox.getSelectedIndex() >= 0 && deptBox.getSelectedIndex() < depts.size()) {
                u.setDeptId(depts.get(deptBox.getSelectedIndex()).getId());
            }
            u.setPosition(posF.getText().trim());
            if (!isEdit) u.setStatus(1);
            try {
                if (isEdit) userService.update(u);
                else userService.add(u);
                dialog.dispose();
                loadData();
                showInfo(isEdit ? "编辑成功" : "新增成功");
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        btnPanel.add(saveBtn);
        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("请先选择一条记录"); return; }
        Long id = (Long) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);
        if (!confirm("确认删除用户[" + name + "]吗？")) return;
        try { userService.delete(id); loadData(); showInfo("删除成功"); }
        catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void resetPassword() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("请先选择一条记录"); return; }
        Long id = (Long) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);
        String newPwd = JOptionPane.showInputDialog(this, "请输入新密码（用户: " + name + "）:");
        if (newPwd == null || newPwd.trim().isEmpty()) return;
        try {
            userService.resetPassword(id, com.oa.common.MD5Util.md5(newPwd.trim()));
            showInfo("密码重置成功");
        } catch (Exception ex) { showError(ex.getMessage()); }
    }
}
