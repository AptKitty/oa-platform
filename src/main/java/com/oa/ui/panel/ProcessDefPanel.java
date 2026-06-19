package com.oa.ui.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.oa.common.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import com.oa.workflow.dao.ProcessDefinitionDao;
import com.oa.workflow.entity.ProcessDefinition;
import com.oa.workflow.entity.ProcessNode;
import java.util.List;

/**
 * 审批链配置面板（管理员用）
 * 管理员在此：(1)查看/新增审批流程定义  (2)选中流程后管理审批节点  (3)添加节点
 * 
 * 每个流程由多个节点组成，按 sort_order 排序形成审批链
 * 例如：请假流程 = 部门经理审批(第1步) → 总监审批(第2步) → HR备案(第3步)
 *
 * 界面结构：同 FormTemplatePanel 模式
 * - 上半：流程定义列表表格 + 工具栏
 * - 下半：选中流程后显示节点管理（节点表格 + 添加节点按钮）
 *
 * @author 成员2
 */
public class ProcessDefPanel extends BasePanel {

    /** 流程定义表格 */
    private JTable defTable;
    private DefaultTableModel defTableModel;

    /** 节点管理区（下半部分） */
    private JPanel nodePanel;
    private JTable nodeTable;
    private DefaultTableModel nodeTableModel;
    /** 当前选中的流程定义ID（用于添加节点时关联） */
    private Long selectedDefId;

    public ProcessDefPanel() {
        // ===== 工具栏 =====
        JPanel toolbar = createToolBar(this::loadDefs, this::showAddDefDialog, null);
        add(toolbar, BorderLayout.NORTH);

        // ===== 流程定义表格（上半部分） =====
        String[] defCols = {"ID", "流程名称", "编码", "关联模板ID", "状态"};
        defTable = createTable(defCols);
        defTableModel = (DefaultTableModel) defTable.getModel();

        // ===== 节点管理区（下半部分，初始占位） =====
        nodePanel = new JPanel(new BorderLayout());
        nodePanel.setBorder(BorderFactory.createTitledBorder("节点管理"));
        nodePanel.add(new JLabel("请先选择流程", SwingConstants.CENTER), BorderLayout.NORTH);

        // ===== JSplitPane 上下分割 =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
            new JScrollPane(defTable), nodePanel);
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);

        // ===== 表格选中监听 =====
        // 触发时机：用户点击流程列表中的某一行
        // 处理逻辑：(1)获取选中的流程定义ID  (2)在下半区加载该流程的节点列表
        defTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = defTable.getSelectedRow();
            if (row >= 0) {
                selectedDefId = (Long) defTableModel.getValueAt(row, 0);
                loadNodes(selectedDefId);
            }
        });

        loadDefs();
    }

    /**
     * 加载所有流程定义到表格
     * 步骤：(1)清空  (2)查库  (3)逐行填充
     */
    private void loadDefs() {
        clearTable(defTable);                                                // (1) 清空
        ProcessDefinitionDao dao = MyBatisUtil.openSession().getMapper(ProcessDefinitionDao.class);
        for (ProcessDefinition d : dao.findAll()) {                         // (2) 查库
            defTableModel.addRow(new Object[]{                               // (3) 填充
                d.getId(), d.getDefName(), d.getDefCode(), 
                d.getTemplateId(), d.getStatus() == 1 ? "启用" : "停用"
            });
        }
    }

    /**
     * 新增流程定义对话框
     * 
     * 界面字段：流程名称、流程编码、关联模板ID、描述
     * 
     * 保存流程：
     * (1)校验名称/编码必填  (2)构建 ProcessDefinition  (3)入库  (4)关闭+刷新
     */
    private void showAddDefDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "新增流程", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 流程名称（必填，如"请假标准流程"）
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("流程名称:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        panel.add(nameField, gbc);

        // 流程编码（必填，程序内部标识，如 LEAVE_FLOW）
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("流程编码:"), gbc);
        gbc.gridx = 1;
        JTextField codeField = new JTextField(15);
        panel.add(codeField, gbc);

        // 关联模板ID（决定这个流程配合哪个模板使用）
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("关联模板ID:"), gbc);
        gbc.gridx = 1;
        JTextField templateIdField = new JTextField(15);
        panel.add(templateIdField, gbc);

        // 描述（选填）
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("描述:"), gbc);
        gbc.gridx = 1;
        JTextField descriptionField = new JTextField(15);
        panel.add(descriptionField, gbc);

        // 保存按钮
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JButton saveBtn = new JButton("保存");
        panel.add(saveBtn, gbc);

        // ===== 保存按钮事件 =====
        saveBtn.addActionListener(e -> {
            // (1) 校验必填项
            String name = nameField.getText().trim();
            String code = codeField.getText().trim();
            if (name.isEmpty() || code.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "名称和编码不能为空");
                return;
            }
            // (2) 构建实体
            ProcessDefinition d = new ProcessDefinition();
            d.setDefName(name);
            d.setDefCode(code);
            d.setTemplateId(Long.parseLong(templateIdField.getText().trim()));
            d.setDescription(descriptionField.getText());
            d.setStatus(1);  // 默认启用

            // (3) 入库
            ProcessDefinitionDao dao = MyBatisUtil.openSession().getMapper(ProcessDefinitionDao.class);
            dao.insert(d);
            // (4) 关闭+刷新
            dialog.dispose();
            loadDefs();
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * 选中流程后加载其节点列表
     * 
     * 步骤：
     * (1)清空节点管理区  (2)创建节点表格+查库填充  (3)添加"添加节点"按钮  (4)刷新
     *
     * @param defId 流程定义ID
     */
    private void loadNodes(Long defId) {
        nodePanel.removeAll();                                               // (1) 清空

        // (2) 节点表格
        String[] columns = {"ID", "节点名称", "类型", "审批人类型", "审批人ID", "角色", "排序"};
        nodeTable = createTable(columns);
        nodeTableModel = (DefaultTableModel) nodeTable.getModel();

        ProcessDefinitionDao dao = MyBatisUtil.openSession().getMapper(ProcessDefinitionDao.class);
        for (ProcessNode n : dao.findNodesByDefId(defId)) {
            nodeTableModel.addRow(new Object[]{
                n.getId(), n.getNodeName(), n.getNodeType(), 
                n.getApproverType(), n.getApproverId(), n.getApproverRole(), n.getSortOrder()
            });
        }

        // (3) 添加节点按钮
        JButton addNodeBtn = new JButton("添加节点");
        addNodeBtn.addActionListener(e -> showAddNodeDialog());

        // (4) 组装并刷新
        nodePanel.add(new JScrollPane(nodeTable), BorderLayout.CENTER);
        nodePanel.add(addNodeBtn, BorderLayout.SOUTH);
        nodePanel.revalidate();
        nodePanel.repaint();
    }

    /**
     * 添加审批节点对话框
     * 
     * 界面字段：
     * - 节点名称：如"部门经理审批"
     * - 节点类型：APPROVE(单人)/CC(抄送)/SIGN(会签)/OR_SIGN(或签)/CONDITION(条件分支)
     * - 审批人类型：SPECIFIC_USER(指定人)/DEPT_LEADER(部门负责人)/ROLE(角色)
     * - 审批人ID：仅在 SPECIFIC_USER 时填写
     * - 排序：决定节点在审批链中的位置
     * 
     * 保存流程：
     * (1)校验选中流程  (2)校验节点名称必填  (3)构建 ProcessNode  (4)入库  (5)刷新
     */
    private void showAddNodeDialog() {
        if (selectedDefId == null) {
            showError("请先选择流程");
            return;
        }

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "新增节点", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 节点名称（必填）
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("节点名称:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        panel.add(nameField, gbc);

        // 节点类型下拉框
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("节点类型:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> typeBox = new JComboBox<>(
            new String[]{"APPROVE", "CC", "SIGN", "OR_SIGN", "CONDITION"}
        );
        panel.add(typeBox, gbc);

        // 审批人类型下拉框
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("审批人类型:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> approverTypeBox = new JComboBox<>(
            new String[]{"SPECIFIC_USER", "DEPT_LEADER", "ROLE"}
        );
        panel.add(approverTypeBox, gbc);

        // 审批人ID（SPECIFIC_USER时填用户ID，其他类型留空）
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("审批人ID:"), gbc);
        gbc.gridx = 1;
        JTextField approverIdField = new JTextField(15);
        panel.add(approverIdField, gbc);

        // 排序（数字越小越靠前，第1步填1）
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("排序:"), gbc);
        gbc.gridx = 1;
        JTextField sortField = new JTextField("1", 15);  // 默认值1
        panel.add(sortField, gbc);

        // 保存按钮
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JButton saveBtn = new JButton("保存");
        panel.add(saveBtn, gbc);

        // ===== 保存按钮事件 =====
        saveBtn.addActionListener(e -> {
            // (2) 校验节点名称必填
            String name = nameField.getText().trim();
            if (name.isEmpty()){
                JOptionPane.showMessageDialog(dialog, "节点名称不能为空");
                return;
            }
            // (3) 构建 ProcessNode 并设置属性
            ProcessNode n = new ProcessNode();
            n.setDefId(selectedDefId);                                       // 关联当前流程
            n.setNodeName(name);
            n.setNodeType((String) typeBox.getSelectedItem());
            n.setApproverType((String) approverTypeBox.getSelectedItem());
            // 审批人ID：如果留空则设为null，否则解析为Long
            String approverIDText = approverIdField.getText().trim();
            n.setApproverId(approverIDText.isEmpty() ? null : Long.parseLong(approverIDText));
            n.setSortOrder(Integer.parseInt(sortField.getText().trim()));

            // (4) 入库
            ProcessDefinitionDao dao = MyBatisUtil.openSession().getMapper(ProcessDefinitionDao.class);
            dao.insertNode(n);
            // (5) 关闭+刷新
            dialog.dispose();
            loadNodes(selectedDefId);
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    @Override
    public String getPanelKey() { return "PROCESS_DEF"; }
    @Override
    public String getPanelTitle() { return "审批链配置"; }
}