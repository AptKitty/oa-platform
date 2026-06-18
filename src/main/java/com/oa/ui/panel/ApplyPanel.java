package com.oa.ui.panel;

import javax.swing.*;
import com.oa.common.MyBatisUtil;
import com.oa.workflow.dao.FormTemplateDao;
import com.oa.workflow.dao.ProcessDefinitionDao;
import com.oa.workflow.entity.FormTemplate;
import com.oa.workflow.entity.FormField;
import com.oa.workflow.entity.ProcessDefinition;
import com.oa.workflow.service.WorkflowService;
import javax.swing.JViewport;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.awt.*;

/**
 * 发起审批面板
 * 用户在此：(1)选择模板  (2)选择流程  (3)填写动态表单  (4)提交审批申请
 * 
 * 核心技术：
 * - 动态表单渲染：根据模板字段类型(TEXT/NUMBER/DATE/SELECT/TEXTAREA)生成对应组件
 * - 数据收集：遍历所有组件获取用户输入，拼成JSON字符串传给Service
 * - 界面联动：选择模板后自动加载对应流程定义和字段
 *
 * @author 成员2
 */
public class ApplyPanel extends BasePanel {

    /** 审批模板下拉框 */
    private JComboBox<String> typeComboBox = new JComboBox<>();
    /** 流程定义下拉框(选模板后动态加载) */
    private JComboBox<String> defBox = new JComboBox<>();
    /** 动态表单区域(根据模板字段动态生成输入组件) */
    private JPanel formArea;
    /** 字段名→组件的映射：提交时通过字段名(key)找到对应组件(value)并取值 */
    private Map<String, JComponent> fieldComponents = new HashMap<>();
    /** 当前选中的模板ID，选模板时赋值 */
    private Long currentTemplateId;

    public ApplyPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // ===== 第1行：选择审批模板 =====
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("选择审批模板："), gbc);
        gbc.gridx = 1;
        add(typeComboBox, gbc);

        // ===== 第2行：选择审批流程(选模板后动态加载) =====
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel("选择审批流程："), gbc);
        gbc.gridx = 1;
        add(defBox, gbc);

        // ===== 模板选择监听 =====
        // 触发时机：用户在模板下拉框中切换选项时
        // 处理逻辑：(1)加载该模板的字段并生成动态表单  (2)加载模板对应的流程定义
        typeComboBox.addActionListener(e -> {
           String selected = (String) typeComboBox.getSelectedItem(); 
           if (!"请选择模板...".equals(selected)) {
               loadFormFields(selected);
           }
        });

        // ===== 第3行：动态表单区域(初始为空面板，选模板后填充) =====
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        formArea = new JPanel();
        formArea.setLayout(new GridBagLayout()); 
        add(formArea, gbc);

        // ===== 第4行：提交按钮 =====
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton submitButton = new JButton("提交申请");
        add(submitButton, gbc);

        // ===== 提交按钮点击事件 =====
        // 完整流程(5步)：
        // ① 校验：确保用户选了模板和流程
        // ② 查找流程定义ID
        // ③ 遍历动态表单组件，收集用户填写的所有值
        // ④ 拼接成JSON字符串（如 {"leaveType":"事假","days":"3"}）
        // ⑤ 调用 WorkflowService.submitProcess() 发起审批
        submitButton.addActionListener(e -> {
            // ① 校验：必须选了模板才能提交
            String selected = (String) typeComboBox.getSelectedItem();
            if ("请选择模板...".equals(selected)) {
                showError("请先选择一个审批模板");
                return;
            }
            // ① 校验：必须选了流程才能提交
            String selectedDef = (String) defBox.getSelectedItem();
            if ("请选择流程...".equals(selectedDef)) {
                showError("请先选择一个审批流程");
                return;
            }

            // ② 根据流程名称反查流程定义ID
            ProcessDefinitionDao defDao = MyBatisUtil.openSession().getMapper(ProcessDefinitionDao.class);
            Long defId = null;
            for (ProcessDefinition def : defDao.findAll()) {
                if (def.getDefName().equals(selectedDef)) {
                    defId = def.getId();
                    break;
                }
            }

            // ③④ 遍历表单组件取值，拼接JSON字符串
            // 遍历 fieldComponents 映射表，key=字段名、value=输入组件
            // 最终生成格式：{"leaveType":"事假","days":"3","reason":"家里有事"}
            StringBuilder json = new StringBuilder("{");
            for (Map.Entry<String, JComponent> entry : fieldComponents.entrySet()) { 
                String value = getComponentValue(entry.getValue());       // ③ 从组件取用户输入
                json.append("\"").append(entry.getKey()).append("\":\"")  // ④ 拼成 "字段名":"值"
                    .append(value).append("\",");
            }
            if (json.length() > 1) json.deleteCharAt(json.length() - 1);  // 去掉末尾多余的逗号
            json.append("}");
            String formData = json.toString();

            // ⑤ 调用Service层发起审批
            new WorkflowService().submitProcess(defId, getCurrentUserId(), formData);
            showInfo("申请提交成功！");
        });

        // 初始化：加载所有模板到下拉框
        loadTemplates();
    }

    /**
     * 加载全部可用模板到下拉框
     * 步骤：(1)添加"请选择模板..."占位项  (2)从数据库查所有启用模板  (3)逐个添加到下拉框
     */
    private void loadTemplates() { 
        typeComboBox.addItem("请选择模板...");                               // (1) 占位提示
        FormTemplateDao dao = MyBatisUtil.openSession().getMapper(FormTemplateDao.class);
        for (FormTemplate template : dao.findAll()) {                      // (2) 查数据库
            typeComboBox.addItem(template.getTemplateName());              // (3) 加入下拉框
        }
    }

    /**
     * 选择模板后触发的核心方法
     * 步骤：
     * (1) 根据模板名称查找模板ID
     * (2) 刷新流程定义下拉框（只显示该模板关联的流程）
     * (3) 查询该模板的所有字段
     * (4) 清空旧表单，遍历字段生成新的输入组件
     * (5) 刷新界面布局
     *
     * @param selected 用户选中的模板名称（如"请假申请"）
     */
    private void loadFormFields(String selected) {
        FormTemplateDao dao = MyBatisUtil.openSession().getMapper(FormTemplateDao.class);

        // (1) 根据模板名称查找模板ID
        Long templateId = null;
        for (FormTemplate template : dao.findAll()) {
            if (template.getTemplateName().equals(selected)) {
                templateId = template.getId();
                break;
            }
        }
        currentTemplateId = templateId;

        // (2) 刷新流程定义下拉框：清空 → 加提示 → 查该模板的流程定义 → 逐个加入
        defBox.removeAllItems();
        defBox.addItem("请选择流程...");
        ProcessDefinitionDao defDao = MyBatisUtil.openSession().getMapper(ProcessDefinitionDao.class);
        for (ProcessDefinition def : defDao.findAll()) {
            if (def.getTemplateId().equals(templateId)) {
                defBox.addItem(def.getDefName());
            }
        }

        // (3) 查询该模板的所有字段（如"请假类型、日期、天数、原因"）
        List<FormField> fields = dao.findFieldsByTemplateId(templateId);

        // (4) 清空旧表单并重新生成
        formArea.removeAll();
        fieldComponents.clear();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        for (FormField field : fields) { 
            // 左边：字段标签（如"请假类型："）
            gbc.gridx = 0; gbc.gridy = row;
            formArea.add(new JLabel(field.getFieldLabel() + "："), gbc);

            // 右边：根据字段类型生成对应的输入组件（文本/数字/日期/下拉/多行文本）
            gbc.gridx = 1;
            JComponent comp = createFieldComponent(field);
            formArea.add(comp, gbc);

            // 存映射关系：字段名 → 组件引用（提交时遍历取值）
            fieldComponents.put(field.getFieldName(), comp);
            row++;
        }
        // (5) 刷新界面：通知布局管理器重新计算位置并重绘
        formArea.revalidate();
        formArea.repaint();
    }

    /**
     * 根据字段类型创建对应的 Swing 输入组件
     * 组件选择规则：
     *   TEXT     → JTextField（单行文本框，15列宽）
     *   NUMBER   → JTextField（数字输入，10列宽）
     *   DATE     → JTextField（日期输入，12列宽）
     *   TEXTAREA → JScrollPane( JTextArea )（多行文本+滚动条）
     *   SELECT   → JComboBox（下拉选择框）
     *
     * @param field 表单字段定义（含类型、选项等信息）
     * @return 对应的 Swing 组件
     */
    private JComponent createFieldComponent(FormField field) {
        String fieldType = field.getFieldType();
        switch (fieldType) {
            case "TEXT":
                return new JTextField(15);           // 单行文本：宽度15列
            case "NUMBER":
                return new JTextField(10);           // 数字输入：宽度10列
            case "DATE":
                return new JTextField(12);           // 日期输入：宽度12列
            case "TEXTAREA":
                JTextArea area = new JTextArea(3, 20); // 多行文本：3行高、20列宽
                area.setLineWrap(true);                // 启用自动换行
                return new JScrollPane(area);          // 包在滚动面板里（超出时显示滚动条）
            case "SELECT":
                String[] options = parseOptions(field.getOptions());  // 解析 JSON 选项
                return new JComboBox<>(options);                     // 下拉选择框
            case "ATTACHMENT":
                JPanel attPanel = new JPanel(new BorderLayout(5, 0));
                attPanel.setOpaque(false);
                JLabel fileLabel = new JLabel("未选择文件");
                fileLabel.setForeground(Color.GRAY);
                JButton chooseBtn = new JButton("选择文件");
                chooseBtn.addActionListener(ev -> {
                    JFileChooser fc = new JFileChooser();
                    if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                        fileLabel.setText(fc.getSelectedFile().getAbsolutePath());
                        fileLabel.setForeground(Color.BLACK);
                    }
                });
                attPanel.add(fileLabel, BorderLayout.CENTER);
                attPanel.add(chooseBtn, BorderLayout.EAST);
                attPanel.putClientProperty("fileLabel", fileLabel);
                return attPanel;
            default:
                return new JTextField(15);           // 未知类型默认当文本处理
        }
    }

    /**
     * 解析 SELECT 字段的 options JSON 字符串为数组
     * 
     * 示例：
     *   输入: '["事假","病假","年假"]'
     *   步骤：(1)去掉方括号和引号 → "事假,病假,年假"  (2)按逗号切分
     *   输出: ["事假", "病假", "年假"]
     *
     * @param optionsJson 数据库中的 options 字段值
     * @return 选项字符串数组
     */
    private String[] parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isEmpty()) return new String[0];
        // (1) 用正则去掉 [ ] " 三种符号
        String inner = optionsJson.replaceAll("[\\[\\]\"]", "");
        // (2) 按逗号切分成数组
        return inner.split(",");
    }

    /**
     * 从输入组件中获取用户填写/选择的值
     * 
     * 支持4种组件类型（因为 TEXTAREA 包在 JScrollPane 里，需要拆包取内层的 JTextArea）：
     *   JTextField   → 直接取文本
     *   JTextArea    → 直接取文本
     *   JComboBox    → 取选中项
     *   JScrollPane  → 拆包取内层 JTextArea 的文本
     *
     * @param comp 表单中的输入组件
     * @return 用户输入/选中的字符串值
     */
    private String getComponentValue(JComponent comp) {
        if (comp instanceof JTextField) {
            return ((JTextField) comp).getText();                        // 单行文本
        } else if (comp instanceof JTextArea) {
            return ((JTextArea) comp).getText();                         // 多行文本（未包在ScrollPane中时）
        } else if (comp instanceof JComboBox) {
            Object item = ((JComboBox<?>) comp).getSelectedItem();       // 下拉选中值
            return item != null ? item.toString() : "";
        } else if (comp instanceof JScrollPane) {
            // TEXTAREA 包在 JScrollPane 里面，需要拆包
            // 层级：JScrollPane → JViewport → JTextArea
            JViewport viewport = ((JScrollPane) comp).getViewport();
            JTextArea area = (JTextArea) viewport.getView();
            return area.getText();
        } else if (comp instanceof JPanel) {
            // ATTACHMENT: JPanel 内含 fileLabel
            JLabel fileLabel = (JLabel) ((JPanel) comp).getClientProperty("fileLabel");
            if (fileLabel != null) {
                String path = fileLabel.getText();
                return "未选择文件".equals(path) ? "" : path;
            }
            return "";
        }
        return "";
    }
    
    @Override
    public String getPanelKey() { return "apply"; }
    @Override
    public String getPanelTitle() { return "申请"; }
}