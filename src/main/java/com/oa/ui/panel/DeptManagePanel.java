package com.oa.ui.panel;

import com.oa.system.entity.Dept;
import com.oa.system.service.DeptService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门管理面板 — 树形结构展示 + 增删改
 */
public class DeptManagePanel extends BasePanel {

    private final DeptService deptService = new DeptService();

    private JTree tree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;

    private JTextField nameField, codeField, leaderField, sortField;
    private JComboBox<String> statusCombo;
    private Dept currentDept;
    private Long currentParentId;

    public DeptManagePanel() {
        super();
        initUI();
    }

    @Override
    public String getPanelKey() { return "DEPT_MANAGE"; }

    @Override
    public String getPanelTitle() { return "部门管理"; }

    private void initUI() {
        rootNode = new DefaultMutableTreeNode("组织机构");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> onTreeSelect());
        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setPreferredSize(new Dimension(260, 0));
        add(treeScroll, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("部门信息"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 10, 5, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        form.add(new JLabel("部门名称:"), g);
        g.gridx = 1;
        nameField = new JTextField(20);
        form.add(nameField, g);

        g.gridx = 0; g.gridy = 1;
        form.add(new JLabel("部门编码:"), g);
        g.gridx = 1;
        codeField = new JTextField(20);
        form.add(codeField, g);

        g.gridx = 0; g.gridy = 2;
        form.add(new JLabel("负责人ID:"), g);
        g.gridx = 1;
        leaderField = new JTextField(20);
        form.add(leaderField, g);

        g.gridx = 0; g.gridy = 3;
        form.add(new JLabel("排序号:"), g);
        g.gridx = 1;
        sortField = new JTextField(20);
        form.add(sortField, g);

        g.gridx = 0; g.gridy = 4;
        form.add(new JLabel("状态:"), g);
        g.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"启用", "禁用"});
        form.add(statusCombo, g);

        rightPanel.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton addBtn = new JButton("新增子部门");
        addBtn.addActionListener(e -> prepareAdd());
        btnPanel.add(addBtn);
        JButton saveBtn = new JButton("保存");
        saveBtn.addActionListener(e -> saveDept());
        btnPanel.add(saveBtn);
        JButton delBtn = new JButton("删除");
        delBtn.addActionListener(e -> deleteDept());
        btnPanel.add(delBtn);
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> loadTree());
        btnPanel.add(refreshBtn);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.CENTER);
        loadTree();
    }

    private void loadTree() {
        rootNode.removeAllChildren();
        List<Dept> all = deptService.findAll();
        Map<Long, DefaultMutableTreeNode> map = new HashMap<>();
        for (Dept d : all) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(d);
            map.put(d.getId(), node);
        }
        for (Dept d : all) {
            DefaultMutableTreeNode node = map.get(d.getId());
            if (d.getParentId() == null || d.getParentId() == 0 || !map.containsKey(d.getParentId())) {
                rootNode.add(node);
            } else {
                map.get(d.getParentId()).add(node);
            }
        }
        treeModel.reload();
        for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
        clearForm();
    }

    private void onTreeSelect() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null || node == rootNode) { clearForm(); return; }
        Object obj = node.getUserObject();
        if (obj instanceof Dept) {
            currentDept = (Dept) obj;
            currentParentId = null;
            nameField.setText(currentDept.getDeptName());
            codeField.setText(currentDept.getDeptCode());
            leaderField.setText(currentDept.getLeaderId() != null ? currentDept.getLeaderId().toString() : "");
            sortField.setText(String.valueOf(currentDept.getSortOrder()));
            statusCombo.setSelectedIndex(currentDept.getStatus() == 1 ? 0 : 1);
        }
    }

    private void prepareAdd() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null && node.getUserObject() instanceof Dept) {
            currentParentId = ((Dept) node.getUserObject()).getId();
        } else {
            currentParentId = 0L;
        }
        currentDept = null;
        nameField.setText("");
        codeField.setText("");
        leaderField.setText("");
        sortField.setText("0");
        statusCombo.setSelectedIndex(0);
        nameField.requestFocus();
    }

    private void clearForm() {
        currentDept = null;
        currentParentId = null;
        nameField.setText("");
        codeField.setText("");
        leaderField.setText("");
        sortField.setText("");
        statusCombo.setSelectedIndex(0);
    }

    private void saveDept() {
        String name = nameField.getText().trim();
        String code = codeField.getText().trim();
        if (name.isEmpty() || code.isEmpty()) { showError("部门名称和编码不能为空"); return; }
        try {
            if (currentDept != null) {
                currentDept.setDeptName(name);
                currentDept.setDeptCode(code);
                currentDept.setLeaderId(parseLongOrNull(leaderField.getText().trim()));
                currentDept.setSortOrder(parseInt(sortField.getText().trim(), 0));
                currentDept.setStatus(statusCombo.getSelectedIndex() == 0 ? 1 : 0);
                deptService.update(currentDept);
            } else {
                Dept d = new Dept();
                d.setParentId(currentParentId != null ? currentParentId : 0L);
                d.setDeptName(name);
                d.setDeptCode(code);
                d.setLeaderId(parseLongOrNull(leaderField.getText().trim()));
                d.setSortOrder(parseInt(sortField.getText().trim(), 0));
                d.setStatus(1);
                deptService.add(d);
            }
            loadTree();
            showInfo("保存成功");
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void deleteDept() {
        if (currentDept == null) { showError("请先选择一个部门"); return; }
        if (!confirm("确认删除部门[" + currentDept.getDeptName() + "]吗？")) return;
        try { deptService.delete(currentDept.getId()); loadTree(); showInfo("删除成功"); }
        catch (Exception ex) { showError(ex.getMessage()); }
    }

    private static Long parseLongOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
