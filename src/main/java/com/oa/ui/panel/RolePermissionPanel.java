package com.oa.ui.panel;

import com.oa.system.entity.Menu;
import com.oa.system.entity.Role;
import com.oa.system.service.MenuService;
import com.oa.system.service.RoleService;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * 角色权限分配面板 — 角色列表 + 菜单权限树(checkbox)
 * 单击即可切换勾选状态，无需双击
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
        // 左侧：角色列表
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder("角色列表"));
        roleListModel = new DefaultListModel<>();
        roleList = new JList<>(roleListModel);
        roleList.addListSelectionListener(e -> onRoleSelect());
        leftPanel.add(new JScrollPane(roleList), BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);

        // 右侧：菜单权限树
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("菜单权限（单击勾选/取消）"));
        menuRoot = new DefaultMutableTreeNode("全部菜单");
        menuTreeModel = new DefaultTreeModel(menuRoot);
        menuTree = new JTree(menuTreeModel);
        menuTree.setCellRenderer(new CheckboxTreeRenderer());
        menuTree.setToggleClickCount(0); // 展开/折叠也只需单击
        JScrollPane treeScroll = new JScrollPane(menuTree);
        rightPanel.add(treeScroll, BorderLayout.CENTER);

        // 单击切换checkbox状态
        menuTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = menuTree.getRowForLocation(e.getX(), e.getY());
                if (row < 0) return;
                TreePath path = menuTree.getPathForRow(row);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof Menu) {
                    Menu m = (Menu) node.getUserObject();
                    m.setStatus(m.getStatus() == 1 ? 0 : 1); // 切换勾选
                    menuTree.repaint();
                }
            }
        });

        // 保存按钮
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

        // 创建所有节点
        for (Menu m : allMenus) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(m);
            menuNodeMap.put(m.getId(), node);
        }

        // 建立父子关系
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

        // 先全部取消勾选
        for (Menu m : allMenus) m.setStatus(0);

        // 再勾选该角色已有的菜单
        List<Menu> roleMenus = menuService.findByRoleId(role.getId());
        Set<Long> checkedIds = new HashSet<>();
        for (Menu m : roleMenus) checkedIds.add(m.getId());
        for (Menu m : allMenus) {
            if (checkedIds.contains(m.getId())) m.setStatus(1);
        }

        menuTree.repaint();
    }

    private void savePermissions() {
        int idx = roleList.getSelectedIndex();
        if (idx < 0) { showError("请先选择一个角色"); return; }
        Role role = roles.get(idx);

        List<Long> checkedIds = new ArrayList<>();
        for (Menu m : allMenus) {
            if (m.getStatus() != null && m.getStatus() == 1) {
                checkedIds.add(m.getId());
            }
        }

        try {
            menuService.assignMenus(role.getId(), checkedIds);
            showInfo("权限保存成功");
        } catch (Exception ex) {
            showError("保存失败: " + ex.getMessage());
        }
    }

    // ==================== Checkbox 树渲染器 ====================

    private static class CheckboxTreeRenderer extends JCheckBox implements TreeCellRenderer {
        private static final Color SELECTION_BG = new Color(184, 207, 229);

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object obj = node.getUserObject();

            if (obj instanceof Menu) {
                Menu m = (Menu) obj;
                String label = m.getMenuName();
                if ("BUTTON".equals(m.getMenuType())) label += " [按钮]";
                setText(label);
                setSelected(m.getStatus() != null && m.getStatus() == 1);
            } else {
                setText(obj.toString());
                setSelected(false);
            }

            setOpaque(true);
            setBackground(selected ? SELECTION_BG : Color.WHITE);
            setForeground(Color.BLACK);
            return this;
        }
    }
}
