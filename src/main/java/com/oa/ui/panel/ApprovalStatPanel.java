package com.oa.ui.panel;//QQQQQQQQQQQQQQQQQQQ

import com.oa.statistics.service.StatService;//引入统计业务类。统计大屏的数据
import com.oa.statistics.entity.StatResultVO;//引入统计结果实体类。StatResultVO 只有三个字段：name（名称）、value（数值）、label（标签）。不管查什么统计，都统一用这个类装数据。
import org.jfree.chart.ChartFactory;//：引入 JFreeChart 的图表工厂。它能一句话生成柱状图、饼图、折线图
import org.jfree.chart.ChartPanel;//引入图表面板。JFreeChart 画的图不能直接加到 Swing 界面上，需要包一层 ChartPanel
import org.jfree.chart.JFreeChart;//引入图表对象。ChartFactory.createBarChart() 返回的就是这个类型
import org.jfree.data.category.DefaultCategoryDataset;//引入柱状图专用数据集。柱状图需要两组数据：一组是分类（横轴），一组是数值（纵轴）。DefaultCategoryDataset 就是装这个的。
import javax.swing.*;//界面库,* 是通配符
import java.awt.*;//布局库
import java.util.List;//列表类

public class ApprovalStatPanel extends BasePanel {//声明类，继承 BasePanel

    private StatService statService = new StatService();//创建统计业务对象

    public ApprovalStatPanel() {
        initUI();
    }//构造函数,构造函数负责初始化，”initUI“方法负责画界面

    @Override//重写父类方法
    public String getPanelKey() { return "STAT_APPROVAL"; }//返回这个面板的唯一标识。组员1 用 "STAT_APPROVAL" 这个字符串在 MainFrame 里注册。

    @Override
    public String getPanelTitle() { return "审批效率统计"; }//返回菜单上显示的中文名

    private void initUI() {
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);//调父类的 createToolBar()，传入三个方法引用。第一个是 this::refresh（点刷新调 refresh），后面两个是 null（不生成新增和导出按钮）。返回一个装了按钮的面板，然后 add(..., BorderLayout.NORTH) 放到顶部。
        refresh();//界面画好后，立刻加载一次数据
    }

    private void refresh() {
        removeAll();
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);

        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            List<StatResultVO> list = statService.getApprovalRanking("2026-06");
            for (StatResultVO vo : list) {
                if (vo.getValue() instanceof Number) {
                    dataset.addValue(((Number) vo.getValue()).doubleValue(), "审批次数", vo.getName());
                }
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "审批工作量统计", "审批人", "审批次数",
                    dataset
            );
            ChartPanel chartPanel = new ChartPanel(chart);
            add(chartPanel, BorderLayout.CENTER);
        } catch (Exception e) {
            add(new JLabel("暂无数据: " + e.getMessage(), SwingConstants.CENTER), BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    // ===== 测试入口 =====
    public static void main(String[] args) {
        JFrame frame = new JFrame("审批效率");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        ApprovalStatPanel panel = new ApprovalStatPanel();
        panel.setCurrentUser(1L, "测试用户");
        frame.add(panel);
        frame.setVisible(true);
    }
}
