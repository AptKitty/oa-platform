package com.oa;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Font;
import com.oa.ui.frame.LoginFrame;

import javax.swing.*;

/**
 * 应用程序入口
 * OA协同办公平台 - 启动类
 */
public class Application {

    public static void main(String[] args) {
        System.setProperty("flatlaf.font", "Microsoft YaHei");

        // 设置 FlatLaf 现代化主题
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("FlatLaf主题加载失败，使用默认主题");
        }

        Font globalFont = new Font("Microsoft YaHei", Font.PLAIN, 13);
        UIManager.put("defaultFont", globalFont);
        UIManager.put("Label.font", globalFont);
        UIManager.put("Button.font", globalFont);
        UIManager.put("TextField.font", globalFont);
        UIManager.put("TextArea.font", globalFont);
        UIManager.put("Table.font", globalFont);
        UIManager.put("TableHeader.font", globalFont);
        UIManager.put("TitledBorder.font", globalFont);
        UIManager.put("ComboBox.font", globalFont);
        UIManager.put("Menu.font", globalFont);
        UIManager.put("MenuItem.font", globalFont);
        UIManager.put("OptionPane.font", globalFont);

        // 在 EDT 线程启动 GUI
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });

        // TODO: IM模块增量开发时，在此启动WebSocket服务
        // new Thread(() -> {
        //     ImWebSocketServer server = new ImWebSocketServer(8887);
        //     server.start();
        //     System.out.println("IM WebSocket服务已启动，端口: 8887");
        // }).start();
    }
}
