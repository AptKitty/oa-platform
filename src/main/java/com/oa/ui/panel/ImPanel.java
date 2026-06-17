package com.oa.ui.panel;

import javax.swing.*;
import java.awt.*;

/**
 * IM 即时通讯面板 — 占位
 */
public class ImPanel extends BasePanel {

    public ImPanel() {
        super();
        initUI();
    }

    @Override
    public String getPanelKey() { return "IM"; }

    @Override
    public String getPanelTitle() { return "即时通讯"; }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel center = new JPanel(new GridBagLayout());
        JLabel label = new JLabel("IM即时通讯模块开发中，敬请期待...", SwingConstants.CENTER);
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
        label.setForeground(Color.GRAY);
        center.add(label);

        JLabel hint = new JLabel("WebSocket 服务端框架已就绪，GUI 聊天界面待增量开发", SwingConstants.CENTER);
        hint.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        hint.setForeground(new Color(180, 180, 180));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = new Insets(15, 0, 0, 0);
        center.add(hint, gbc);

        add(center, BorderLayout.CENTER);
    }
}
