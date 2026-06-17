package com.oa.ui.panel;

import com.oa.common.ExportUtil;//引入导出工具类。createToolBar() 里导出按钮会调到
import javax.swing.*;
import javax.swing.table.DefaultTableModel;//表格数据模型
import java.awt.*;

public abstract class BasePanel extends JPanel {//继承 Swing 的面板类,加行，add（）组件

    protected Long currentUserId;//存储用户ID，Long 是包装类型（不是基本类型 long），因为数据库存的是 BIGINT，可以为 null
    protected String currentUsername;//存储当前用户名

    public BasePanel() {//构造函数。每次 new CalendarPanel() 时，Java 会先执行 BasePanel 的构造函数，再执行 其它Panel 的构造函数。
        setLayout(new BorderLayout(10, 10));//设布局管理器为 BorderLayout。(10, 10) 是组件之间的水平和垂直间距（10 像素）
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));//5个区域，设边距，四个参数分别是上、左、下、右的像素
    }

    public void setCurrentUser(Long userId, String username) {//设置当前登录用户。这个方法由外部调用——你的 main 方法里 panel.setCurrentUser(1L, "测试用户") 调的就是它。登录后由 MainFrame 调用。
        this.currentUserId = userId;//把传入的 userId 存到成员变量 this.currentUserId。this. 是为了区分"成员变量"和"参数"
        this.currentUsername = username;//把传入的 username 存到成员变量  this.currentUsername 是为了区分"成员变量"和"参数"
    }

    public abstract String getPanelKey();//抽象方法——只有声明，没有方法体
    public abstract String getPanelTitle();//抽象方法——只有声明，没有方法体

    protected void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "错误", JOptionPane.ERROR_MESSAGE); }//Swing的弹窗方法
    protected void showInfo(String msg) { JOptionPane.showMessageDialog(this, msg, "提示", JOptionPane.INFORMATION_MESSAGE); }//弹信息提示。和前一行唯一的区别是图标——INFORMATION_MESSAGE 是蓝色圆圈 i 图标
    protected boolean confirm(String msg) {
        int r = JOptionPane.showOptionDialog(this, msg, "确认",//调用 showOptionDialog——比 showConfirmDialog 更灵活，可以自定义按钮文字。返回值 r 是用户点了第几个按钮（从 0 开始）。
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new String[]{"是", "否"}, "否");//两个按钮，文字分别是"是"和"否"
        return r == JOptionPane.YES_OPTION;
    }//this — 父窗口
   // msg — 提示文字，比如"确定要删除吗？"
    // "确认" — 窗口标题
    //JOptionPane.YES_NO_OPTION — 对话框类型（确定/取消风格）
    //JOptionPane.QUESTION_MESSAGE — 图标（问号）

    protected JTable createTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return new JTable(model);
    }

    protected JPanel createToolBar(Runnable onRefresh, Runnable onAdd, Runnable onExport) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> onRefresh.run());
        toolbar.add(refreshBtn);
        if (onAdd != null) {
            JButton addBtn = new JButton("新增");
            addBtn.addActionListener(e -> onAdd.run());
            toolbar.add(addBtn);
        }
        if (onExport != null) {
            JButton exportBtn = new JButton("导出Excel");
            exportBtn.addActionListener(e -> onExport.run());
            toolbar.add(exportBtn);
        }
        return toolbar;
    }

    protected void clearTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
    }

    protected static final int TEXT_FIELD_WIDTH = 15;
    protected static final int BUTTON_HEIGHT = 28;
}