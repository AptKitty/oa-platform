package com.oa.ui.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.oa.common.MyBatisUtil;
import com.oa.common.Constants;
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
 */
public class MyApplicationPanel extends BasePanel {

    private JTable appTable;
    private DefaultTableModel appTableModel;
    private JPanel detailPanel;
    private Long selectedInstanceId;
    private WorkflowService workflowService = new WorkflowService();

    public MyApplicationPanel() {
        JPanel toolbar = createToolBar(this::loadMyApplications, null, null);
        add(toolbar, BorderLayout.NORTH);

        String[] columns = {"ID", "流程名称", "状态", "提交时间"};
        appTable = createTable(columns);
        appTableModel = (DefaultTableModel) appTable.getModel();

        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("申请详情"));
        detailPanel.add(new JLabel("请选择一条申请查看详情", SwingConstants.CENTER), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(appTable), detailPanel);
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);

        appTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = appTable.getSelectedRow();
            if (row >= 0) {
                selectedInstanceId = (Long) appTableModel.getValueAt(row, 0);
                showDetail(selectedInstanceId);
            }
        });

        loadMyApplications();
    }

    private void loadMyApplications() {
        clearTable(appTable);
        List<ProcessInstance> myApps =
            workflowService.getMyApplications(getCurrentUserId(), null, 1, 100);
        for (ProcessInstance p : myApps) {
            String statusText;
            switch (p.getStatus()) {
                case "DRAFT":     statusText = "草稿"; break;
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

    private void showDetail(Long instanceId) {
        detailPanel.removeAll();

        try (SqlSession s = MyBatisUtil.openSession()) {
            ProcessInstance instance = s.getMapper(ProcessInstanceDao.class).findById(instanceId);
            if (instance == null) {
                detailPanel.add(new JLabel("未找到该申请", SwingConstants.CENTER));
                detailPanel.revalidate(); detailPanel.repaint();
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
    }

    private JPanel buildFlowChart(ProcessInstance instance) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            List<ProcessNode> nodes = s.getMapper(ProcessDefinitionDao.class).findNodesByDefId(instance.getDefId());
            List<ApprovalRecord> records = s.getMapper(ProcessInstanceDao.class).findRecordsByInstanceId(instance.getId());

            JPanel chart = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
            chart.setBackground(Color.WHITE);

            for (int i = 0; i < nodes.size(); i++) {
                ProcessNode node = nodes.get(i);
                String status = getNodeStatus(node, records, instance.getStatus());
                chart.add(createNodeCard(node, status));
                if (i < nodes.size() - 1) chart.add(createArrow());
            }
            return chart;
        }
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
        if ("DRAFT".equals(instanceStatus)) return "DRAFT";
        return "PENDING";
    }

    private JPanel createNodeCard(ProcessNode node, String status) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(130, 80));

        Color bgColor; String icon;
        switch (status) {
            case "PASSED":   bgColor = new Color(200, 255, 200); icon = "已通过"; break;
            case "CURRENT":  bgColor = new Color(255, 255, 200); icon = "审批中"; break;
            case "REJECTED": bgColor = new Color(255, 200, 200); icon = "已驳回"; break;
            case "CC_DONE":  bgColor = new Color(200, 220, 255); icon = "已抄送"; break;
            case "DRAFT":    bgColor = new Color(240, 240, 240); icon = "草稿"; break;
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

        card.add(nameLabel); card.add(typeLabel); card.add(statusLabel);
        return card;
    }

    private JLabel createArrow() {
        JLabel arrow = new JLabel("-->");
        arrow.setFont(new Font("Arial", Font.BOLD, 20));
        arrow.setForeground(Color.GRAY);
        return arrow;
    }

    private JPanel buildFormDataPanel(ProcessInstance instance) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("表单数据"));

        JTextArea formDataArea = new JTextArea();
        formDataArea.setEditable(false);
        formDataArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

        if (instance.getFormData() != null && !instance.getFormData().isEmpty()) {
            String json = instance.getFormData();
            json = json.replace("{", "{\n  ").replace("}", "\n}").replace(",", ",\n  ");
            formDataArea.setText(json);
        } else {
            formDataArea.setText("无表单数据");
        }
        panel.add(new JScrollPane(formDataArea), BorderLayout.CENTER);

        // 状态栏 + 操作按钮
        JPanel bottomBar = new JPanel(new BorderLayout());

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String statusText;
        switch (instance.getStatus()) {
            case "DRAFT":     statusText = "草稿（未提交）"; break;
            case "PENDING":   statusText = "待审批"; break;
            case "APPROVING": statusText = "审批中"; break;
            case "PASSED":    statusText = "已通过"; break;
            case "REJECTED":  statusText = "已驳回"; break;
            case "CANCELLED": statusText = "已取消"; break;
            default:          statusText = instance.getStatus();
        }
        statusBar.add(new JLabel("当前状态: " + statusText));
        bottomBar.add(statusBar, BorderLayout.WEST);

        // 草稿专属操作按钮
        if ("DRAFT".equals(instance.getStatus())) {
            JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton editBtn = new JButton("继续编辑");
            editBtn.addActionListener(e -> resumeDraft(instance.getId()));
            actionBar.add(editBtn);

            JButton delBtn = new JButton("删除草稿");
            delBtn.addActionListener(e -> deleteDraft(instance.getId()));
            actionBar.add(delBtn);

            bottomBar.add(actionBar, BorderLayout.EAST);
        }

        // ???/????????????
        if ("PENDING".equals(instance.getStatus()) || "APPROVING".equals(instance.getStatus())) {
            JPanel actionBar2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton revokeBtn = new JButton("撤销申请");
            revokeBtn.addActionListener(e -> revokeApplication(instance.getId()));
            actionBar2.add(revokeBtn);
            bottomBar.add(actionBar2, BorderLayout.EAST);
        }

        panel.add(bottomBar, BorderLayout.SOUTH);
        return panel;
    }

    /** 继续编辑草稿：记录草稿ID，跳转到发起申请面板 */
    private void resumeDraft(Long instanceId) {
        Constants.setDraftInstanceId(instanceId);
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof com.oa.ui.frame.MainFrame) {
                ((com.oa.ui.frame.MainFrame) parent).showPanel("APPLY");
                return;
            }
        // ???/????????????


            parent = parent.getParent();
        }
    }

    /** 删除草稿 */
    private void deleteDraft(Long instanceId) {
        if (!confirm("确认删除此草稿吗？")) return;
        try (SqlSession s = MyBatisUtil.openSession()) {
            s.getMapper(ProcessInstanceDao.class).deleteById(instanceId);
        }
        loadMyApplications();
        detailPanel.removeAll();
        detailPanel.add(new JLabel("请选择一条申请查看详情", SwingConstants.CENTER));
        detailPanel.revalidate(); detailPanel.repaint();
    }

    
    /** 撤销申请 */
    private void revokeApplication(Long instanceId) {
        if (!confirm("确认撤销此申请吗？")) return;
        try {
            workflowService.revoke(instanceId, getCurrentUserId());
            showInfo("申请已撤销");
            loadMyApplications();
            detailPanel.removeAll();
            detailPanel.add(new JLabel("请选择一条申请查看详情", SwingConstants.CENTER));
            detailPanel.revalidate(); detailPanel.repaint();
        } catch (Exception ex) {
            showError("撤销失败：" + ex.getMessage());
        }
    }

@Override
    public String getPanelKey() { return "MY_APPLICATIONS"; }
    @Override
    public String getPanelTitle() { return "我的申请"; }
}