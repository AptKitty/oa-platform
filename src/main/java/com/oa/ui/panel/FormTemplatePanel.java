package com.oa.ui.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.oa.common.MyBatisUtil;
import com.oa.workflow.dao.FormTemplateDao;
import com.oa.workflow.entity.FormTemplate;
import com.oa.workflow.entity.FormField;
import java.util.List;

/**
 * 表单模板管理面板（管理员用）
 * 管理员在此：(1)查看/新增模板  (2)选中模板后管理字段  (3)添加字段
 *
 * 界面结构：JSplitPane 上下分割
 * - 上半：模板列表表格 + 工具栏（刷新、新增）
 * - 下半：选中模板后显示字段管理（字段表格 + 添加字段按钮）
 *
 * @author 成员2
 */
public class FormTemplatePanel extends BasePanel{

    /** 模板列表表格 */
    private JTable table;
    private DefaultTableModel tableModel;

    /** 字段管理区（下半部分，初始显示"请选择模板"提示） */
    private JPanel fieldPanel;
    private JTable fieldTable;
    private DefaultTableModel fieldTableModel;
    /** 当前选中的模板ID（用于添加字段时关联） */
    private Long selectedTemplateId;

    public FormTemplatePanel() {
        // ===== 工具栏：刷新 + 新增模板 =====
        JPanel toolbar = createToolBar(this::loadData, this::showAddDialog, null);
        add(toolbar, BorderLayout.NORTH);

        // ===== 模板列表表格（上半部分） =====
        String[] columns = {"ID", "模板名称", "编码", "分类", "状态"};
        table = createTable(columns);
        tableModel = (DefaultTableModel) table.getModel();

        // ===== 字段管理区（下半部分，初始占位） =====
        fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setBorder(BorderFactory.createTitledBorder("字段管理"));
        fieldPanel.add(new JLabel("请选择模板", SwingConstants.CENTER), BorderLayout.CENTER);

        // ===== JSplitPane 上下分割 =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
             new JScrollPane(table), fieldPanel);
        splitPane.setDividerLocation(250);   // 分割线位置
        add(splitPane, BorderLayout.CENTER);

        // ===== 表格选中监听 =====
        // 触发时机：用户点击模板列表中的某一行
        // 处理逻辑：(1)获取选中模板ID  (2)在下半区加载该模板的字段列表
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;  // 忽略选择过程中的中间事件
            int row = table.getSelectedRow();
            if (row >= 0) {
                Long id = (Long) tableModel.getValueAt(row, 0);  // 第0列是ID
                selectedTemplateId = id;
                loadFields(id);
            }
        });

        loadData();
    }

    /**
     * 加载所有模板到表格
     * 步骤：(1)清空表格  (2)查数据库  (3)逐行填充
     */
    private void loadData() {
        clearTable(table);                                                    // (1) 清空
        FormTemplateDao dao = MyBatisUtil.openSession().getMapper(FormTemplateDao.class);
        for (FormTemplate t : dao.findAll()) {                               // (2) 查库
            tableModel.addRow(new Object[]{                                  // (3) 逐行填充
                t.getId(), t.getTemplateName(), t.getTemplateCode(),
                t.getCategory(), t.getStatus() == 1 ? "启用" : "停用"         // 状态1→启用，0→停用
            });
        }
    }

    /**
     * 弹出新增模板对话框（JDialog模态窗口）
     * 
     * 界面字段：模板名称、模板编码、分类、描述
     * 
     * 保存流程：
     * (1)校验：名称和编码不能为空
     * (2)构建 FormTemplate 对象（status默认启用）
     * (3)调用 DAO.insert() 入库
     * (4)关闭弹窗 + 刷新表格
     */
    private void showAddDialog() {
        // 创建模态对话框（true=模态，必须先关闭此窗口才能操作主窗口）
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "新增模板", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);  // 居中显示

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 模板名称（必填）
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("模板名称:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        panel.add(nameField, gbc);

        // 模板编码（必填，程序内部标识，如 LEAVE/EXPENSE）
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("模板编码:"), gbc);
        gbc.gridx = 1;
        JTextField codeField = new JTextField(15);
        panel.add(codeField, gbc);

        // 分类下拉框
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("分类:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> categoryBox = new JComboBox<>(
            new String[]{"请假", "报销", "出差", "加班", "用章", "采购", "自定义"}
        );
        panel.add(categoryBox, gbc);

        // 描述（选填）
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("描述:"), gbc);
        gbc.gridx = 1;
        JTextField descField = new JTextField(15);
        panel.add(descField, gbc);

        // 保存按钮
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JButton saveBtn = new JButton("保存");
        panel.add(saveBtn, gbc);

        // ===== 保存按钮事件 =====
        saveBtn.addActionListener(e -> {
            // (1) 校验：名称和编码不能为空
            String name = nameField.getText().trim();
            String code = codeField.getText().trim();
            if(name.isEmpty() || code.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "名称和编码不能为空");
                return;
            }
            // (2) 构建实体对象
            FormTemplate t = new FormTemplate();
            t.setTemplateName(name);
            t.setTemplateCode(code);
            t.setCategory((String) categoryBox.getSelectedItem());
            t.setDescription(descField.getText());
            t.setStatus(1);   // 默认启用

            // (3) 入库
            FormTemplateDao dao = MyBatisUtil.openSession().getMapper(FormTemplateDao.class);
            dao.insert(t);
            // (4) 关闭弹窗 + 刷新
            dialog.dispose();
            loadData();
        });

        dialog.add(panel);
        dialog.setVisible(true);  // 显示模态窗口（阻塞，直到用户关闭）
    }

    /**
     * 选中模板后加载其字段列表
     * 
     * 步骤：
     * (1) 清空字段管理区
     * (2) 创建字段表格，查库填充数据
     * (3) 添加"添加字段"按钮
     * (4) 刷新界面
     *
     * @param templateId 所选模板ID
     */
    private void loadFields(Long templateId) {
        fieldPanel.removeAll();                                              // (1) 清空

        // (2) 创建字段表格并填充
        String[] columns = {"ID", "字段名", "标签", "类型", "必填", "排序"};
        fieldTable = createTable(columns);
        fieldTableModel = (DefaultTableModel) fieldTable.getModel();

        FormTemplateDao dao = MyBatisUtil.openSession().getMapper(FormTemplateDao.class);
        for (FormField f : dao.findFieldsByTemplateId(templateId)) {
            fieldTableModel.addRow(new Object[]{
                f.getId(), f.getFieldName(), f.getFieldLabel(),
                f.getFieldType(), 
                f.getIsRequired() == 1 ? "是" : "否",   // 1=必填，0=选填
                f.getSortOrder()
            });
        }

        // (3) 添加字段按钮
        JButton addFieldBtn = new JButton("添加字段");
        addFieldBtn.addActionListener(e -> showAddFieldDialog());

        // (4) 组装并刷新
        fieldPanel.add(new JScrollPane(fieldTable), BorderLayout.CENTER);
        fieldPanel.add(addFieldBtn, BorderLayout.SOUTH);
        fieldPanel.revalidate();
        fieldPanel.repaint();
    }

    /**
     * 弹出添加字段对话框（JDialog模态窗口）
     * 
     * 界面字段：字段名、字段标签、字段类型、是否必填
     * 
     * 保存流程：
     * (1)校验选中模板  (2)校验字段名和标签必填  (3)构建 FormField  (4)入库  (5)刷新
     */
    private void showAddFieldDialog() {
        if (selectedTemplateId == null) {
            showError("请先选择模板");
            return;
        }

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "添加字段", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 字段名（程序用，驼峰命名，如 leaveType）
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("字段名:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        panel.add(nameField, gbc);

        // 字段标签（显示用，如"请假类型"）
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("字段标签:"), gbc);
        gbc.gridx = 1;
        JTextField labelField = new JTextField(15);
        panel.add(labelField, gbc);

        // 字段类型下拉框：TEXT/NUMBER/DATE/SELECT/TEXTAREA
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("字段类型:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> typeBox = new JComboBox<>(
            new String[]{"TEXT", "NUMBER", "DATE", "SELECT", "TEXTAREA"}
        );
        panel.add(typeBox, gbc);

        // 是否必填复选框
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("是否必填:"), gbc);
        gbc.gridx = 1;
        JCheckBox requiredBox = new JCheckBox();
        panel.add(requiredBox, gbc);

        // 保存按钮
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JButton saveBtn = new JButton("保存");
        panel.add(saveBtn, gbc);

        // ===== 保存按钮事件 =====
        saveBtn.addActionListener(e -> {
            // (2) 校验：字段名和标签不能为空
            String name = nameField.getText().trim();
            String label = labelField.getText().trim();
            if(name.isEmpty() || label.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "字段名和标签不能为空");
                return;
            }
            // (3) 构建 FormField 对象并设置属性
            FormField f = new FormField();
            f.setTemplateId(selectedTemplateId);                            // 关联当前模板
            f.setFieldName(name);
            f.setFieldLabel(label);
            f.setFieldType((String) typeBox.getSelectedItem());
            f.setIsRequired(requiredBox.isSelected() ? 1 : 0);             // 复选框选中→1，否则→0
            f.setSortOrder(fieldTableModel.getRowCount() + 1);             // 排序追加到末尾

            // (4) 入库
            FormTemplateDao dao = MyBatisUtil.openSession().getMapper(FormTemplateDao.class);
            dao.insertField(f);
            // (5) 关闭弹窗 + 刷新字段表格
            dialog.dispose();
            loadFields(selectedTemplateId);
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    @Override
    public String getPanelKey() { return "form_template"; }
    @Override
    public String getPanelTitle() { return "表单模板管理"; }
}