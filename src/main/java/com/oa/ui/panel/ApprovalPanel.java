package com.oa.ui.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.oa.common.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import com.oa.workflow.service.WorkflowService;
import com.oa.workflow.entity.ProcessInstance;
import com.oa.workflow.entity.ProcessNode;
import com.oa.workflow.dao.ProcessDefinitionDao;
import com.oa.workflow.dao.ProcessInstanceDao;
import com.oa.workflow.entity.ApprovalRecord;
import java.util.List;

/**
 * 审批处理面板
 * 当前登录用户在此：(1)查看待审批列表  (2)选中一条查看详情  (3)进行通过/驳回操作
 *
 * 界面结构：JSplitPane 上下分割
 * - 上半：待审批列表表格（只显示分配给当前用户、状态为 PENDING 的任务）
 * - 下半：选中后显示审批操作区（表单数据展示、审批意见输入、通过/驳回按钮）
 *
 * @author 成员2
 */
public class ApprovalPanel extends BasePanel {

    /** 待审批列表表格 */
    private JTable pendingTable;
    private DefaultTableModel pendingTableModel;

    /** 审批操作区（下半部分，初始显示提示文字，选中后动态生成） */
    private JPanel detailPanel;  
    /** 当前选中的流程实例ID（用于审批操作） */
    private Long selectedInstanceId;
    /** 审批服务实例（复用，避免每次创建） */
    private WorkflowService workflowService = new WorkflowService();

    public ApprovalPanel() { 
        // ===== 工具栏：刷新按钮 =====
        JPanel toolbar = createToolBar(this::loadPending, null, null);
        add(toolbar, BorderLayout.NORTH);

        // ===== 待审批表格（上半部分） =====
        String[] pendingColumns = {"ID", "流程名称", "状态", "提交时间"};
        pendingTable = createTable(pendingColumns);
        pendingTableModel = (DefaultTableModel) pendingTable.getModel();
        
        // ===== 审批操作区（下半部分，初始为空占位） =====
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("审批操作"));
        detailPanel.add(new JLabel("请选择待审批项", SwingConstants.CENTER), BorderLayout.CENTER);

        // ===== JSplitPane：上下分割 =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
            new JScrollPane(pendingTable), detailPanel);
        splitPane.setDividerLocation(200);   // 分割线在200像素处
        add(splitPane, BorderLayout.CENTER);

        // ===== 表格选中监听 =====
        // 触发时机：用户点击待审批列表中的某一行
        // 处理逻辑：(1)获取选中的实例ID  (2)在下半区显示详情和操作按钮
        pendingTable.getSelectionModel().addListSelectionListener(e -> { 
            if (e.getValueIsAdjusting()) return;  // 忽略选择过程中的中间事件，只响应最终选择
            int row = pendingTable.getSelectedRow();
            if (row >= 0) {
                selectedInstanceId = (Long) pendingTableModel.getValueAt(row, 0);  // 第0列是ID
                showDetail(selectedInstanceId);
            }
        });

        // 初始化：加载数据
        loadPending();
    }

    /**
     * 加载当前用户的待审批列表
     * 调用 WorkflowService.getPendingApprovals()
     * SQL 层面通过 JOIN wf_task 表，筛选 assignee_id=当前用户 且 status='PENDING' 的记录
     */
    private void loadPending() { 
        clearTable(pendingTable);                                            // 清空旧数据
        List<ProcessInstance> pendingInstances = 
            workflowService.getPendingApprovals(getCurrentUserId(), 1, 100);     // 查第1页，每页100条
        for (ProcessInstance p : pendingInstances) {
            pendingTableModel.addRow(new Object[]{
                p.getId(),                                                  // ID
                p.getDefName(),                                             // 流程名称（快照）
                p.getStatus(),                                              // 当前状态
                p.getCreateTime() != null ? p.getCreateTime().toString() : "" // 提交时间
            });
        }
    }

    /**
     * 选中待审批项后显示详情和审批操作
     * 
     * 步骤：
     * (1) 查数据库获取流程实例详情（含表单数据JSON）
     * (2) 显示表单数据（只读文本域）
     * (3) 显示审批意见输入框
     * (4) 添加"通过"和"驳回"按钮，各自绑定审批逻辑
     * (5) 刷新下半区界面
     *
     * @param instanceId 流程实例ID
     */
    private void showDetail(Long instanceId) {
        detailPanel.removeAll();  // 清空之前的内容

        // (1) 查数据库获取流程实例详情
        ProcessInstanceDao dao = MyBatisUtil.openSession().getMapper(ProcessInstanceDao.class);
        ProcessInstance instance = dao.findById(instanceId);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // (2) 表单数据展示区（只读，不可编辑）
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("表单数据:"), gbc);
        gbc.gridx = 1;
        JTextArea dataArea = new JTextArea(instance.getFormData(), 4, 30);
        dataArea.setEditable(false);     // 只读：审批人只能看不能改
        dataArea.setLineWrap(true);
        formPanel.add(new JScrollPane(dataArea), gbc);

        // (3) 审批意见输入框
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("审批意见:"), gbc);
        gbc.gridx = 1;
        JTextArea commentArea = new JTextArea(2, 30);
        JTextField attachPathField2 = new JTextField(30);
        attachPathField2.setEditable(false);
        JButton attachBtn2 = new JButton("审批通过");
        final String[] attachPath2 = {""};
        attachBtn2.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                attachPath2[0] = fc.getSelectedFile().getAbsolutePath();
                attachPathField2.setText(fc.getSelectedFile().getName());
            }
        });
        commentArea.setLineWrap(true);
        formPanel.add(new JScrollPane(commentArea), gbc);

                                            // 审批通过
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("??:"), gbc);
        gbc.gridx = 1;
        JPanel attachRow = new JPanel(new BorderLayout(5, 0));
        attachRow.add(attachPathField2, BorderLayout.CENTER);
        attachRow.add(attachBtn2, BorderLayout.EAST);
        formPanel.add(attachRow, gbc);

        // (4) 通过/驳回按钮
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton approveBtn = new JButton("通过");
        JButton rejectBtn = new JButton("驳回");
        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        formPanel.add(btnPanel, gbc);

        // ===== 通过按钮事件 =====
        // 流程：(a)调用 Service.approve()  →  (b)提示成功  →  (c)刷新列表  →  (d)重置操作区
        approveBtn.addActionListener(e -> { 
            try {
                workflowService.approve(instanceId, getCurrentUserId(), commentArea.getText(), attachPath2[0]);  // (a)
                showInfo("审批通过");                                                         // (b)
                loadPending();                                                                // (c) 刷新列表
                resetDetailPanel();                                                           // (d) 重置操作区
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        // ===== 驳回按钮事件 =====
        // 流程：同通过，但调用 Service.reject()，状态变为 REJECTED
        rejectBtn.addActionListener(e -> {
            try { 
                workflowService.reject(instanceId, getCurrentUserId(), commentArea.getText(), attachPath2[0]);
                showInfo("已驳回");
                loadPending();
                resetDetailPanel();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        // (5) 刷新界面
        // 查询审批流程图数据
        JPanel flowPanel = buildFlowChart(instance);
        detailPanel.add(flowPanel, BorderLayout.NORTH);
        detailPanel.add(formPanel, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private JPanel buildFlowChart(ProcessInstance instance) { 
        //1. 取数据
        ProcessDefinitionDao defDao = MyBatisUtil.openSession().getMapper(ProcessDefinitionDao.class);
        ProcessInstanceDao processInstanceDao = MyBatisUtil.openSession().getMapper(ProcessInstanceDao.class);

        List<ProcessNode> nodes = defDao.findNodesByDefId(instance.getDefId());
        List<ApprovalRecord> records = processInstanceDao.findRecordsByInstanceId(instance.getId());

        //2. 构建面板（水平排列的节点卡片）
        JPanel chart = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
        chart.setBackground(Color.WHITE);

        //3. 遍历每个节点，画节点卡片
        for (int i = 0; i < nodes.size(); i++) { 
            ProcessNode node = nodes.get(i);

            //3a. 判断当前节点状态
            String status = getNodeStatus(node, records, instance.getStatus());

            //3b. 创建节点卡片JPanel
            chart.add(createNodeCard(node, status));

            //3c. 节点之间加箭头（最后一个不加）
            if (i < nodes.size() - 1) { 
                chart.add(createArrow());
            }
        }
        return chart;
    }

    private String getNodeStatus(ProcessNode node, List<ApprovalRecord> records, String instanceStatus) { 
        //找这个节点对应的审批记录
        for (ApprovalRecord rec : records) {
            if (rec.getNodeId().equals(node.getId())){ 
                //审批过了
                if ("APPROVE".equals(rec.getAction())) {
                    return "PASSED";
                }
                if ("REJECT".equals(rec.getAction())) {
                    return "REJECTED";
                }
                if ("CC".equals(rec.getAction())) {
                    return "CC_DONE";
                }
            }
        }
        //没找到记录->判断是不是当前正在等的节点
        if ("APPROVING".equals(instanceStatus)) {
            return "CURRENT"; // 当前卡在这
        }
        return "PENDING"; // 还没走到
    }

    private JPanel createNodeCard(ProcessNode node, String status) { 
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(130, 80));

        //颜色： 用过 = 绿、当前 = 黄、驳回 = 红、等待 = 灰
        Color bgColor;
        String icon;
        switch (status) {
            case "PASSED":   bgColor = new Color(200, 255, 200); icon = "已通过"; break;
            case "CURRENT":  bgColor = new Color(255, 255, 200); icon = "审批中"; break;
            case "REJECTED": bgColor = new Color(255, 200, 200); icon = "已驳回"; break;
            case "CC_DONE":  bgColor = new Color(200, 220, 255); icon = "已抄送"; break;
            default:         bgColor = new Color(230, 230, 230); icon = "待处理";
        }
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel nameLabel = new JLabel(node.getNodeName());
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel typeLabel = new JLabel("[" + node.getNodeType() + "]");
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel(icon);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(nameLabel);
        card.add(typeLabel);
        card.add(statusLabel);

        return card;
    }

    private JLabel createArrow() {
        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font("Arial", Font.BOLD, 20));
        arrow.setForeground(Color.GRAY);
        return arrow;
    }
    /**
     * 审批完成后重置操作区为初始状态
     * 清空表单数据和按钮，恢复"请选择待审批项"提示
     */
    private void resetDetailPanel() {
        detailPanel.removeAll();
        detailPanel.add(new JLabel("请选择待审批项", SwingConstants.CENTER), BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }



    @Override
    public String getPanelKey() { return "APPROVAL"; }
    @Override
    public String getPanelTitle() { return "审批处理"; }
}