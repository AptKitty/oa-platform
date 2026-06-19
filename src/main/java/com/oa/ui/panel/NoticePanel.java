package com.oa.ui.panel;

import com.oa.notice.entity.Notice;
import com.oa.notice.service.NoticeService;
import com.oa.system.service.RoleService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.util.List;
import com.oa.common.PageResult;

/**
 * 公告面板 — 公告列表 / 发布(富文本) / 附件上传 / 定时发布 / 置顶 / 已读追踪
 *
 * 注册 key: "NOTICE"
 * 成员3 负责 (成员2 增强: 富文本 + 附件 + 定时发布)
 */
public class NoticePanel extends BasePanel {

    private NoticeService noticeService;
    private JTable noticeTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton newBtn;
    private JButton deleteBtn;
    private boolean canManage; // ???????(???/??)

    public NoticePanel() {
        noticeService = new NoticeService();
        initUI();
        loadNotices(null);
        // 启动时激活已到期的定时公告
        noticeService.activateScheduledNotices();    }

    @Override
    public void setCurrentUser(Long userId, String username) {
        super.setCurrentUser(userId, username);
        checkManagePermission();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        newBtn = new JButton("新建公告");
        newBtn.addActionListener(e -> showPublishDialog());
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> loadNotices(searchField.getText().trim()));
        leftBtns.add(newBtn);
        leftBtns.add(refreshBtn);
        deleteBtn = new JButton("删除公告");
        deleteBtn.addActionListener(e -> deleteSelectedNotice());
        leftBtns.add(deleteBtn);
        // ???????????????
        newBtn.setVisible(false);
        deleteBtn.setVisible(false);

        JPanel rightSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchField = new JTextField(15);
        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> loadNotices(searchField.getText().trim()));
        rightSearch.add(new JLabel("关键字："));
        rightSearch.add(searchField);
        rightSearch.add(searchBtn);

        toolbar.add(leftBtns, BorderLayout.WEST);
        toolbar.add(rightSearch, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        // 公告列表表格
        String[] columns = {"ID", "标题", "发布人", "发布时间", "已读", "未读", "置顶"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        noticeTable = new JTable(tableModel);
        noticeTable.setRowHeight(28);
        noticeTable.getTableHeader().setReorderingAllowed(false);
        noticeTable.getColumnModel().getColumn(0).setMaxWidth(40);
        noticeTable.getColumnModel().getColumn(5).setMaxWidth(50);
        noticeTable.getColumnModel().getColumn(6).setMaxWidth(50);

        // ???????????????
        noticeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = noticeTable.getSelectedRow();
                    if (row >= 0) {
                        Long noticeId = (Long) tableModel.getValueAt(row, 0);
                        showDetailDialog(noticeId);
                    }
                }
            }
        });
        add(new JScrollPane(noticeTable), BorderLayout.CENTER);

        // ?????????/??????????
        checkManagePermission();
    }

    /** ??????????????????/??? */
    private void checkManagePermission() {
        try {
            RoleService roleService = new RoleService();
            java.util.Set<Long> roleIds = roleService.findRoleIdsByUserId(getCurrentUserId());
            canManage = roleIds.contains(1L) || roleIds.contains(2L);
            if (newBtn != null) newBtn.setVisible(canManage);
            if (deleteBtn != null) deleteBtn.setVisible(canManage);
        } catch (Exception e) {
            canManage = false;
            if (newBtn != null) newBtn.setVisible(false);
            if (deleteBtn != null) deleteBtn.setVisible(false);
        }
    }

    private void loadNotices(String keyword) {
        try {
            PageResult<Notice> result = noticeService.findByPage(
                    (keyword == null || keyword.isEmpty()) ? null : keyword, 1, 100);
            tableModel.setRowCount(0);
            for (Notice n : result.getRows()) {
                int readCount = noticeService.getReadCount(n.getId());
                int totalUsers = noticeService.getActiveUserCount();
                int unreadCount = Math.max(0, totalUsers - readCount);
                tableModel.addRow(new Object[]{
                        n.getId(), n.getTitle(),
                        n.getPublisherName() != null ? n.getPublisherName() : "用户#" + n.getPublisherId(),
                        n.getCreateTime() != null
                                ? n.getCreateTime().toLocalDate().toString() : "",
                        readCount, unreadCount,
                        n.getIsTop() != null && n.getIsTop() == 1 ? "[顶]" : ""
                });
            }
        } catch (Exception e) {
            showError("加载公告失败：" + e.getMessage());
        }
    }

    /** 删除选中的公告（仅发布人本人或管理员可删） */
    private void deleteSelectedNotice() {
        int row = noticeTable.getSelectedRow();
        if (row < 0) { showError("请先选择一条公告"); return; }
        Long noticeId = (Long) tableModel.getValueAt(row, 0);
        try {
            Notice notice = noticeService.findById(noticeId);
            if (notice == null) { showError("公告不存在"); return; }

            // 权限检查：发布人本人 或 超级管理员(role=1) / 部门经理(role=2)
            boolean canDelete = notice.getPublisherId().equals(getCurrentUserId());
            if (!canDelete) {
                RoleService roleService = new RoleService();
                java.util.Set<Long> roleIds = roleService.findRoleIdsByUserId(getCurrentUserId());
                canDelete = roleIds.contains(1L) || roleIds.contains(2L);
            }
            if (!canDelete) {
                showError("只有发布人本人或管理员才能删除公告");
                return;
            }

            if (!confirm("确定要删除公告【" + notice.getTitle() + "】吗？")) return;
            noticeService.delete(noticeId);
            showInfo("删除成功");
            loadNotices(searchField.getText().trim());
        } catch (Exception e) {
            showError("删除失败：" + e.getMessage());
        }
    }

    private void showDetailDialog(Long noticeId) {
        try {
            Notice notice = noticeService.findById(noticeId);
            if (notice == null) { showError("公告不存在"); return; }
            noticeService.markRead(noticeId, getCurrentUserId());

            List<Long> readUserIds = noticeService.getReadUserIds(noticeId);
            String readInfo = "已读 " + readUserIds.size() + " 人";

            JPanel detailPanel = new JPanel(new BorderLayout(10, 10));
            detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // 用 JEditorPane 渲染 HTML 内容
            JEditorPane contentPane = new JEditorPane();
            contentPane.setContentType("text/html");
            contentPane.setEditable(false);
            String html = notice.getContentHtml() != null ? notice.getContentHtml()
                    : "<html><body>" + notice.getContent() + "</body></html>";
            contentPane.setText(html);

            JPanel infoPanel = new JPanel(new GridLayout(5, 1, 5, 5));
            infoPanel.add(new JLabel("发布人：" + (notice.getPublisherName() != null ? notice.getPublisherName() : "用户#" + notice.getPublisherId())));
            infoPanel.add(new JLabel("发布时间：" +
                    (notice.getCreateTime() != null
                            ? notice.getCreateTime().toString().replace("T", " ") : "")));
            infoPanel.add(new JLabel(readInfo));
            // 附件下载
            if (notice.getAttachment() != null && !notice.getAttachment().isEmpty()) {
                JButton downloadBtn = new JButton("下载附件");
                downloadBtn.addActionListener(e -> {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setSelectedFile(new java.io.File("附件"));
                    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        try {
                            java.io.File src = new java.io.File(notice.getAttachment());
                            java.nio.file.Files.copy(src.toPath(), chooser.getSelectedFile().toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            showInfo("下载成功");
                        } catch (Exception ex) {
                            showError("下载失败：" + ex.getMessage());
                        }
                    }
                });
                infoPanel.add(downloadBtn);
            }

            detailPanel.add(new JLabel(notice.getTitle(), SwingConstants.CENTER), BorderLayout.NORTH);
            detailPanel.add(new JScrollPane(contentPane), BorderLayout.CENTER);
            // 已读/未读名单按钮
            JButton readDetailBtn = new JButton("查看已读/未读名单");
            readDetailBtn.addActionListener(ev -> showReadDetail(noticeId));
            infoPanel.add(readDetailBtn);

            detailPanel.add(infoPanel, BorderLayout.SOUTH);
            detailPanel.setPreferredSize(new java.awt.Dimension(600, 500));
            JOptionPane.showMessageDialog(this, detailPanel,
                    "公告详情", JOptionPane.INFORMATION_MESSAGE);
            loadNotices(searchField.getText().trim());
        } catch (Exception e) {
            showError("加载公告详情失败：" + e.getMessage());
        }
    }

    /** 发布公告对话框（富文本 + 附件上传 + 定时发布 + 置顶） */
    private void showPublishDialog() {
        if (!canManage) { showError("??????????????"); return; }
        JTextField titleField = new JTextField(30);

        // ===== 富文本编辑器 =====
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        JScrollPane editorScroll = new JScrollPane(textPane);
        editorScroll.setPreferredSize(new Dimension(600, 300));

        // 富文本工具栏
        JPanel richToolbar = createRichTextToolbar(textPane);

        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(richToolbar, BorderLayout.NORTH);
        editorPanel.add(editorScroll, BorderLayout.CENTER);

        // ===== 附件上传 =====
        JPanel attachPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel attachLabel = new JLabel("未选择附件");
        attachLabel.setForeground(Color.GRAY);
        JButton attachBtn = new JButton("选择附件");
        final String[] attachPath = {""};
        attachBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                attachPath[0] = fc.getSelectedFile().getAbsolutePath();
                attachLabel.setText(fc.getSelectedFile().getName());
                attachLabel.setForeground(Color.BLACK);
            }
        });
        attachPanel.add(attachBtn);
        attachPanel.add(attachLabel);

        // ===== 置顶 + 定时发布 =====
        JCheckBox topCheck = new JCheckBox("置顶");
        JTextField schedDateField = new JTextField(10);
        JTextField schedTimeField = new JTextField(6);
        schedTimeField.setText("09:00");
        JPanel schedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        schedPanel.add(new JLabel("定时发布:"));
        schedPanel.add(new JLabel("日期(yyyy-MM-dd):"));
        schedPanel.add(schedDateField);
        schedPanel.add(new JLabel("时间(HH:mm):"));
        schedPanel.add(schedTimeField);

        // 组装对话框
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("标题："), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleField, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("内容："), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        formPanel.add(editorPanel, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("附件："), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(attachPanel, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(topCheck, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel(""), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(schedPanel, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(this, panel,
                "新建公告", JOptionPane.OK_CANCEL_OPTION)) return;

        String title = titleField.getText().trim();
        if (title.isEmpty()) { showError("标题不能为空"); return; }

        try {
            Notice notice = new Notice();
            notice.setTitle(title);
            // 获取 HTML 内容
            String htmlContent = textPane.getText();
            notice.setContent(extractPlainText(htmlContent));
            notice.setContentHtml(htmlContent);
            notice.setPublisherId(getCurrentUserId());
            notice.setIsTop(topCheck.isSelected() ? 1 : 0);
            notice.setAttachment(attachPath[0].isEmpty() ? null : attachPath[0]);

            String schedDate = schedDateField.getText().trim();
            String schedTime = schedTimeField.getText().trim();
            if (!schedDate.isEmpty() && !schedTime.isEmpty()) {
                notice.setScheduledTime(
                        java.time.LocalDateTime.parse(schedDate + "T" + schedTime + ":00"));
                notice.setStatus(0); // 定时发布 → 先存为草稿状态
            } else {
                notice.setStatus(1); // 立即发布
            }
            noticeService.publish(notice);
            showInfo("公告发布成功！");
            loadNotices(null);
        } catch (Exception e) {
            showError("发布失败：" + e.getMessage());
        }
    }

    /** 创建富文本编辑工具栏 */
    private JPanel createRichTextToolbar(JTextPane textPane) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));

        // 粗体
        JButton boldBtn = new JButton("B");
        boldBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        boldBtn.addActionListener(e -> toggleStyle(textPane, StyleConstants.Bold, boldBtn));
        toolbar.add(boldBtn);

        // 斜体
        JButton italicBtn = new JButton("I");
        italicBtn.setFont(new Font("Microsoft YaHei", Font.ITALIC, 14));
        italicBtn.addActionListener(e -> toggleStyle(textPane, StyleConstants.Italic, italicBtn));
        toolbar.add(italicBtn);

        // 下划线
        JButton underlineBtn = new JButton("U");
        underlineBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        underlineBtn.addActionListener(e -> toggleStyle(textPane, StyleConstants.Underline, underlineBtn));
        toolbar.add(underlineBtn);

        toolbar.add(new JToolBar.Separator());

        // 字号
        String[] sizes = {"12", "14", "16", "18", "20", "24", "28"};
        JComboBox<String> sizeCombo = new JComboBox<>(sizes);
        sizeCombo.setSelectedItem("14");
        sizeCombo.addActionListener(e -> {
            String sz = (String) sizeCombo.getSelectedItem();
            if (sz != null) setFontSize(textPane, Integer.parseInt(sz));
        });
        toolbar.add(new JLabel("字号:"));
        toolbar.add(sizeCombo);

        toolbar.add(new JToolBar.Separator());

        // 标题
        JButton h1Btn = new JButton("H1");
        h1Btn.addActionListener(e -> setFontSize(textPane, 24));
        toolbar.add(h1Btn);
        JButton h2Btn = new JButton("H2");
        h2Btn.addActionListener(e -> setFontSize(textPane, 18));
        toolbar.add(h2Btn);
        JButton normalBtn = new JButton("正文");
        normalBtn.addActionListener(e -> setFontSize(textPane, 14));
        toolbar.add(normalBtn);

        return toolbar;
    }

    /** 切换文字样式（粗体/斜体/下划线） */
    private void toggleStyle(JTextPane textPane, Object styleKey, JButton btn) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start == end) return;

        // 获取当前样式
        AttributeSet attrs = doc.getCharacterElement(start).getAttributes();
        boolean isActive = StyleConstants.isBold(attrs) && styleKey == StyleConstants.Bold
                || StyleConstants.isItalic(attrs) && styleKey == StyleConstants.Italic
                || StyleConstants.isUnderline(attrs) && styleKey == StyleConstants.Underline;

        SimpleAttributeSet sas = new SimpleAttributeSet();
        if (styleKey == StyleConstants.Bold) StyleConstants.setBold(sas, !isActive);
        else if (styleKey == StyleConstants.Italic) StyleConstants.setItalic(sas, !isActive);
        else if (styleKey == StyleConstants.Underline) StyleConstants.setUnderline(sas, !isActive);

        doc.setCharacterAttributes(start, end - start, sas, false);
        btn.setForeground(isActive ? Color.BLACK : Color.BLUE);
        textPane.requestFocus();
    }

    /** 设置选中文字字号 */
    private void setFontSize(JTextPane textPane, int size) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start == end) {
            // 无选中时设置后续输入的字号
            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setFontSize(sas, size);
            textPane.setCharacterAttributes(sas, false);
        } else {
            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setFontSize(sas, size);
            doc.setCharacterAttributes(start, end - start, sas, false);
        }
        textPane.requestFocus();
    }

    /** 从 HTML 中提取纯文本（用于搜索和列表显示） */
    private String extractPlainText(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
    }

    
    /** 显示已读/未读详细名单 */
    private void showReadDetail(Long noticeId) {
        try {
            java.util.List<java.util.Map<String, Object>> readList = noticeService.getReadUserNames(noticeId);
            java.util.List<java.util.Map<String, Object>> unreadList = noticeService.getUnreadUserNames(noticeId);

            JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));

            // 已读列表
            DefaultListModel<String> readModel = new DefaultListModel<>();
            for (java.util.Map<String, Object> u : readList) {
                String name = (String) u.getOrDefault("realName", "未知");
                String dept = (String) u.getOrDefault("deptName", "-");
                readModel.addElement(name + " (" + dept + ")");
            }
            JList<String> readListUI = new JList<>(readModel);
            JPanel readPanel = new JPanel(new BorderLayout());
            readPanel.setBorder(BorderFactory.createTitledBorder("已读 (" + readList.size() + "人)"));
            readPanel.add(new JScrollPane(readListUI), BorderLayout.CENTER);

            // 未读列表
            DefaultListModel<String> unreadModel = new DefaultListModel<>();
            for (java.util.Map<String, Object> u : unreadList) {
                String name = (String) u.getOrDefault("realName", "未知");
                String dept = (String) u.getOrDefault("deptName", "-");
                unreadModel.addElement(name + " (" + dept + ")");
            }
            JList<String> unreadListUI = new JList<>(unreadModel);
            JPanel unreadPanel = new JPanel(new BorderLayout());
            unreadPanel.setBorder(BorderFactory.createTitledBorder("未读 (" + unreadList.size() + "人)"));
            unreadPanel.add(new JScrollPane(unreadListUI), BorderLayout.CENTER);

            panel.add(readPanel);
            panel.add(unreadPanel);
            panel.setPreferredSize(new java.awt.Dimension(500, 350));

            JOptionPane.showMessageDialog(this, panel, "已读/未读名单", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            showError("加载名单失败：" + e.getMessage());
        }
    }

    @Override
    public String getPanelKey()   { return "NOTICE"; }
    @Override
    public String getPanelTitle() { return "公告消息"; }
}
