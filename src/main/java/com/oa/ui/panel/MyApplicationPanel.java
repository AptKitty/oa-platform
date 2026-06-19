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
 * 我的申请面板 —— 查看自己发起的审批申请及其状态
 *
 * 界面结构：JSplitPane 上下分割
 * - 上半：我发起的申请列表表格（状态列高亮显示）
 * - 下半：选中后显示审批流程图 + 表单数据
 *
 * @author 成员2
 */
public class MyApplicationPanel extends BasePanel {

    private JTable appTable;
    private DefaultTableModel appTableModel;
    private JPanel detailPanel;
    private Long selectedInstanceId;
    private WorkflowService workflowService = new WorkflowService();

    public MyApplicationPanel() {
        // 工具栏：刷新按钮
        JPanel toolbar = createToolBar(this::loadMyApplications, null, null);
        add(toolbar, BorderLayout.NORTH);

        // 上半：我的申请列表表格
        String[] columns = {"ID", "流程名称", "状态", "提交时间"};
        appTable = createTable(columns);
        appTableModel = (DefaultTableModel) appTable.getModel();

        // 下半：详情区域（初始为空占位）
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("申请详情"));
        detailPanel.add(new JLabel("请选择一条申请查看详情", SwingConstants.CENTER), BorderLayout.CENTER);

        // JSplitPane 上下分割
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(appTable), detailPanel);
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);

        // 表格选中监听：选中某行后显示详情
        appTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = appTable.getSelectedRow();
            if (row >= 0) {
                selectedInstanceId = (Long) appTableModel.getValueAt(row, 0);
                showDetail(selectedInstanceId);
            }
        });

        // 初始加载数据
        loadMyApplications();
    }

    /** 加载当前用户发起的申请列表 */
    private void loadMyApplications() {
        clearTable(appTable);
        List<ProcessInstance> myApps =
            workflowService.getMyApplications(getCurrentUserId(), null, 1, 100);
        for (ProcessInstance p : myApps) {
            String statusText;
            switch (p.getStatus()) {
                case "PENDING":   statusText = "待审批"; break;
                case "APPROVING": statusText = "审批中"; break;
                case "PASSED":    statusText = "已通过"; break;
                case "REJECTED":  statusText = "已驳回"; break;
                case "CANCELLED": statusText = "已取消"; break;
                default:          statusText = p.getStatus();
            }
            appTableModel.addRow(new Object[]{
                p.getId(),
                p.getDefName(),
                statusText,
                p.getCreateTime() != null ? p.getCreateTime().toString() : ""
            });
        }
    }

    /** 选中申请后显示详情：流程图 + 表单数据 */
    private void showDetail(Long instanceId) {
        detailPanel.removeAll();

        ProcessInstanceDao instanceDao = MyBatisUtil.openSession()
            .getMapper(ProcessInstanceDao.class);
        ProcessInstance instance = instanceDao.findById(instanceId);
        if (instance == null) {
            detailPanel.add(new JLabel("未找到该申请", SwingConstants.CENTER));
            detailPanel.revalidate();
            detailPanel.repaint();
            return;
        }

        // 上半：审批流程图
        JPanel flowPanel = buildFlowChart(instance);
        detailPanel.add(flowPanel, BorderLayout.NORTH);

        // 下半：表单数据展示
        JPanel formPanel = buildFormDataPanel(instance);
        detailPanel.add(formPanel, BorderLayout.CENTER);

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    /** 构建审批流程图（水平排列的节点卡片） */
    private JPanel buildFlowChart(ProcessInstance instance) {
        ProcessDefinitionDao defDao = MyBatisUtil.openSession()
            .getMapper(ProcessDefinitionDao.class);
        ProcessInstanceDao instanceDao = MyBatisUtil.openSession()
            .getMapper(ProcessInstanceDao.class);

        List<ProcessNode> nodes = defDao.findNodesByDefId(instance.getDefId());
        List<ApprovalRecord> records = instanceDao.findRecordsByInstanceId(instance.getId());

        JPanel chart = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
        chart.setBackground(Color.WHITE);

        for (int i = 0; i < nodes.size(); i++) {
            ProcessNode node = nodes.get(i);
            String status = getNodeStatus(node, records, instance.getStatus());
            chart.add(createNodeCard(node, status));
            if (i < nodes.size() - 1) {
                chart.add(createArrow());
            }
        }
        return chart;
    }

    private String getNodeStatus(ProcessNode node, List<ApprovalRecord> records, String instanceStatus) {
        for (ApprovalRecord rec : records) {
            if (rec.getNodeId().equals(node.getId())) {
                if ("APPROVE".equals(rec.getAction())) return "PASSED";
                if ("REJECT".equals(rec.getAction())) return "REJECTED";
                if ("CC".equals(rec.getAction()))      return "CC_DONE";
            }
        }
        if ("APPROVING".equals(instanceStatus)) return "CURRENT";
        return "PENDING";
    }

    private JPanel createNodeCard(ProcessNode node, String status) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(130, 80));

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

    /** 展示表单数据（JSON格式化显示） */
    private JPanel buildFormDataPanel(ProcessInstance instance) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("表单数据"));

        JTextArea formDataArea = new JTextArea();
        formDataArea.setEditable(false);
        formDataArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

        if (instance.getFormData() != null && !instance.getFormData().isEmpty()) {
            // JSON 简单格式化展示
            String json = instance.getFormData();
            json = json.replace("{", "{\n  ")
                       .replace("}", "\n}")
                       .replace(",", ",\n  ");
            formDataArea.setText(json);
        } else {
            formDataArea.setText("无表单数据");
        }

        panel.add(new JScrollPane(formDataArea), BorderLayout.CENTER);

        // 状态标签
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String statusText;
        switch (instance.getStatus()) {
            case "PENDING":   statusText = "待审批"; break;
            case "APPROVING": statusText = "审批中"; break;
            case "PASSED":    statusText = "已通过"; break;
            case "REJECTED":  statusText = "已驳回"; break;
            case "CANCELLED": statusText = "已取消"; break;
            default:          statusText = instance.getStatus();
        }
        statusBar.add(new JLabel("当前状态: " + statusText));
        panel.add(statusBar, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    public String getPanelKey() { return "MY_APPLICATIONS"; }
    @Override
    public String getPanelTitle() { return "我的申请"; }
}
