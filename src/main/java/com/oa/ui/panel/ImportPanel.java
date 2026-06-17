package com.oa.ui.panel;

import com.oa.common.ExportUtil;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImportPanel extends BasePanel {

    public ImportPanel() {
        initUI();
    }

    @Override
    public String getPanelKey() { return "IMPORT"; }

    @Override
    public String getPanelTitle() { return "数据导入"; }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 15, 15));

        JLabel titleLabel = new JLabel("批量数据导入", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        centerPanel.add(titleLabel);

        JLabel descLabel = new JLabel("支持导入 Excel(.xlsx) 文件，第一行为表头，从第二行开始读取数据", SwingConstants.CENTER);
        centerPanel.add(descLabel);

        JButton importUserBtn = new JButton("导入用户数据");
        importUserBtn.addActionListener(e -> importData("用户"));
        centerPanel.add(importUserBtn);

        JButton importAssetBtn = new JButton("导入资产数据");
        importAssetBtn.addActionListener(e -> importData("资产"));
        centerPanel.add(importAssetBtn);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void importData(String type) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        List<String[]> resultList = new ArrayList<>();

        try {
            ExportUtil.importFromExcel(file, row -> row, resultList);
            showInfo("成功导入 " + resultList.size() + " 条" + type + "数据（演示模式，数据未写入数据库）");
        } catch (Exception ex) {
            showError("导入失败: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("数据导入");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        ImportPanel panel = new ImportPanel();
        panel.setCurrentUser(1L, "测试用户");
        frame.add(panel);
        frame.setVisible(true);
    }
}