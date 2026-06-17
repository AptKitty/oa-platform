package com.oa;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Font;
import com.oa.ui.frame.LoginFrame;

import javax.swing.*;

/**
 * ??????
 * OA?????? - ???
 */
public class Application {

    public static void main(String[] args) {
        // ?? FlatLaf ?????
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("FlatLaf?????????????");
        }

        // ?????????? FlatLaf ????????????
        Font globalFont = new Font("Microsoft YaHei", Font.PLAIN, 13);
        UIManager.put("defaultFont", globalFont);
        UIManager.put("Label.font", globalFont);
        UIManager.put("Button.font", globalFont);
        UIManager.put("TextField.font", globalFont);
        UIManager.put("TextArea.font", globalFont);
        UIManager.put("Table.font", globalFont);
        UIManager.put("TableHeader.font", globalFont);
        UIManager.put("TitledBorder.font", globalFont);

        // ? EDT ???? GUI
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });

        // TODO: IM????????????WebSocket??
        // new Thread(() -> {
        //     ImWebSocketServer server = new ImWebSocketServer(8887);
        //     server.start();
        //     System.out.println("IM WebSocket????????: 8887");
        // }).start();
    }
}
