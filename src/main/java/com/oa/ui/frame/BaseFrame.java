package com.oa.ui.frame;

import javax.swing.*;
import com.oa.common.Constants;

/**
 * GUI基础框架 - 所有窗口的基类
 * 模块负责人: 【组员G】
 *
 * 使用 FlatLaf 主题 (pom已引入)，美观度优于原生Swing
 */
public abstract class BaseFrame extends JFrame {

    protected Long currentUserId;
    protected String currentUsername;

    public BaseFrame(String title) {
        super(title + " - OA协同办公平台");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        // 设置图标（可选）
        // setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
    }

    /**
     * 设置当前登录用户
     */
    public void setCurrentUser(Long userId, String username) {
        this.currentUserId = userId;
        this.currentUsername = username;
        Constants.setCurrentUser(userId, username);
    }

    /**
     * 初始化界面 - 子类实现
     */
    protected abstract void initUI();

    /**
     * 显示错误对话框
     */
    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 显示信息对话框
     */
    protected void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 确认对话框
     */
    protected boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "确认",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
