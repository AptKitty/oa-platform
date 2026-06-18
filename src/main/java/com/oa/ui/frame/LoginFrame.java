package com.oa.ui.frame;

import com.oa.system.service.UserService;
import com.oa.system.entity.User;

import javax.swing.*;
import java.awt.*;

/**
 * 登录窗口
 */
public class LoginFrame extends BaseFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private UserService userService = new UserService();

    public LoginFrame() {
        super("用户登录");
        setSize(450, 320);
        initUI();
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("OA协同办公平台", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("用户名："), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("密  码："), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        loginBtn = new JButton("登 录");
        loginBtn.addActionListener(e -> doLogin());
        formPanel.add(loginBtn, gbc);

        add(formPanel, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("学号: ________  姓名: ________", SwingConstants.CENTER);
        infoLabel.setForeground(Color.GRAY);
        add(infoLabel, BorderLayout.SOUTH);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            showError("请输入用户名和密码");
            return;
        }
        // 后台线程执行登录 + 构建主界面，避免卡顿
        loginBtn.setText("正在登录...");
        loginBtn.setEnabled(false);
        new Thread(() -> {
            try {
                User user = userService.login(username, password);
                SwingUtilities.invokeLater(() -> {
                    setCurrentUser(user.getId(), user.getRealName());
                    MainFrame mainFrame = new MainFrame();
                    mainFrame.setCurrentUser(user.getId(), user.getRealName(), user.getDeptId());
                    mainFrame.initUI();
                    mainFrame.setVisible(true);
                    dispose();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    showError(ex.getMessage());
                    loginBtn.setText("登 录");
                    loginBtn.setEnabled(true);
                });
            }
        }).start();
    }
}