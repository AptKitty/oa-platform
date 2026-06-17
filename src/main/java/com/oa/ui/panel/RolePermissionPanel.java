package com.oa.ui.panel;

import com.oa.system.entity.Menu;
import com.oa.system.entity.Role;
import com.oa.system.service.MenuService;
import com.oa.system.service.RoleService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 角色权限分配面板 — 角色列表 + 菜单权限树(checkbox)
 */
public class RolePermissionPanel extends BasePanel {

    private final RoleService roleService = new RoleService();
    private final MenuService menuService = new MenuService();

    private JList<String> roleList;
    private DefaultListModel<String> roleListModel;
    private List<Role> roles;

    private JTree menuTree;
    private DefaultTreeModel menuTreeModel;
    private DefaultMutableTreeNode menuRoot;
    private Map<Long, DefaultMutableTreeNode> menuNodeMap;
    private List<Menu> allMenus;

    public RolePermissionPanel() {
        super();
        initUI();
    }

    @Override
    public String getPanelKey() { return "ROLE_PERMISSION"; }

    @Override
    public String getPanelTitle() { return "角色权限"; }

    private void initUI() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder("角色列表"));
        roleListModel = new DefaultListModel<>();
        roleList = new JList<>(roleListModel);
        roleList.addListSelectionListener(e -> onRoleSelect());
        leftPanel.add(new JScrollPane(roleList), BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("菜单权限"));
        menuRoot = new DefaultMutableTreeNode("全部菜单");
        menuTreeModel = new DefaultTreeModel(menuRoot);
        menuTree = new JTree(menuTreeModel);
        menuTree.setCellRenderer(new CheckboxTreeCellRenderer());
        menuTree.setCellEditor(new CheckboxTreeCellEditor());
        menuTree.setEditable(true);
        JScrollPane treeScroll = new JScrollPane(menuTree);
        rightPanel.add(treeScroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("保存权限");
        saveBtn.addActionListener(e -> savePermissions());
        btnPanel.add(saveBtn);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.CENTER);

        loadRoles();
        loadMenuTree();
    }

    private void loadRoles() {
        roleListModel.clear();
        roles = roleService.getAllRoles();
        for (Role r : roles) {
            roleListModel.addElement(r.getRoleName() + " (" + r.getRoleCode() + ")");
        }
    }

    private void loadMenuTree() {
        menuRoot.removeAllChildren();
        allMenus = menuService.findAll();
        menuNodeMap = new HashMap<>();
        Map<Long, Menu> menuMap = new HashMap<>();
        for (Menu m : allMenus) {
            menuMap.put(m.getId(), m);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(m);
            menuNodeMap.put(m.getId(), node);
        }
        for (Menu m : allMenus) {
            DefaultMutableTreeNode node = menuNodeMap.get(m.getId());
            if (m.getParentId() == null || m.getParentId() == 0 || !menuNodeMap.containsKey(m.getParentId())) {
                menuRoot.add(node);
            } else {
                menuNodeMap.get(m.getParentId()).add(node);
            }
        }
        menuTreeModel.reload();
        for (int i = 0; i < menuTree.getRowCount(); i++) menuTree.expandRow(i);
    }

    private void onRoleSelect() {
        int idx = roleList.getSelectedIndex();
        if (idx < 0) return;
        Role role = roles.get(idx);
        List<Menu> roleMenus = menuService.findByRoleId(role.getId());
        Set<Long> checkedIds = new HashSet<>();
        for (Menu m : roleMenus) checkedIds.add(m.getId());
        updateAllCheckStates(menuRoot, checkedIds);
        menuTree.repaint();
    }

    private void updateAllCheckStates(DefaultMutableTreeNode node, Set<Long> checkedIds) {
        Object obj = node.getUserObject();
        if (obj instanceof Menu) {
            ((Menu) obj).setStatus(checkedIds.contains(((Menu) obj).getId()) ? 1 : 0);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            updateAllCheckStates((DefaultMutableTreeNode) node.getChildAt(i), checkedIds);
        }
    }

    private void savePermissions() {
        int idx = roleList.getSelectedIndex();
        if (idx < 0) { showError("请先选择一个角色"); return; }
        Role role = roles.get(idx);
        List<Long> checkedIds = new ArrayList<>();
        collectChecked(menuRoot, checkedIds);
        try {
            menuService.assignMenus(role.getId(), checkedIds);
            showInfo("权限保存成功");
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void collectChecked(DefaultMutableTreeNode node, List<Long> ids) {
        Object obj = node.getUserObject();
        if (obj instanceof Menu && ((Menu) obj).getStatus() == 1) {
            ids.add(((Menu) obj).getId());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectChecked((DefaultMutableTreeNode) node.getChildAt(i), ids);
        }
    }

    private static class CheckboxTreeCellRenderer extends JCheckBox implements javax.swing.tree.TreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object obj = node.getUserObject();
            if (obj instanceof Menu) {
                Menu m = (Menu) obj;
                setText(m.getMenuName() + ("BUTTON".equals(m.getMenuType()) ? " [按钮]" : ""));
                setSelected(m.getStatus() != null && m.getStatus() == 1);
            } else {
                setText(obj.toString());
                setSelected(false);
            }
            setOpaque(true);
            setBackground(selected ? new Color(184, 207, 229) : Color.WHITE);
            return this;
        }
    }

    private class CheckboxTreeCellEditor extends DefaultCellEditor implements javax.swing.tree.TreeCellEditor {
        private JCheckBox checkBox;
        private DefaultMutableTreeNode currentNode;

        public CheckboxTreeCellEditor() {
            super(new JCheckBox());
            this.checkBox = (JCheckBox) editorComponent;
            checkBox.addItemListener(e -> {
                if (currentNode != null && currentNode.getUserObject() instanceof Menu) {
                    ((Menu) currentNode.getUserObject()).setStatus(checkBox.isSelected() ? 1 : 0);
                }
                stopCellEditing();
            });
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                     boolean isSelected, boolean expanded,
                                                     boolean leaf, int row) {
            currentNode = (DefaultMutableTreeNode) value;
            Object obj = currentNode.getUserObject();
            if (obj instanceof Menu) {
                checkBox.setText(((Menu) obj).getMenuName());
                checkBox.setSelected(((Menu) obj).getStatus() != null && ((Menu) obj).getStatus() == 1);
            } else {
                checkBox.setText(obj.toString());
                checkBox.setSelected(false);
            }
            return checkBox;
        }
    }
}
