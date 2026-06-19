package com.oa.ui.panel;

import com.oa.statistics.service.StatService;
import com.oa.statistics.entity.StatResultVO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ApprovalStatPanel extends BasePanel {

    private StatService statService = new StatService();

    public ApprovalStatPanel() {
        initUI();
    }

    @Override
    public String getPanelKey() { return "STAT_APPROVAL"; }
    @Override
    public String getPanelTitle() { return "审批效率统计"; }

    private void initUI() {
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);
        refresh();
    }

    private void refresh() {
        removeAll();
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);

        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            List<StatResultVO> list = statService.getApprovalRanking(
                java.time.LocalDate.now().getYear() + "-" + String.format("%02d", java.time.LocalDate.now().getMonthValue()));
            for (StatResultVO vo : list) {
                if (vo.getValue() instanceof Number) {
                    dataset.addValue(((Number) vo.getValue()).doubleValue(), "审批次数", vo.getName());
                }
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "审批工作量统计", "审批人", "审批次数",
                    dataset
            );
            configureChartFont(chart);
            ChartPanel chartPanel = new ChartPanel(chart);
            add(chartPanel, BorderLayout.CENTER);
        } catch (Exception e) {
            add(new JLabel("暂无数据: " + e.getMessage(), SwingConstants.CENTER), BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    /** 设置JFreeChart中文字体，解决图表中文显示为方框的问题 */
    public static void configureChartFont(JFreeChart chart) {
        Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 16);
        Font labelFont = new Font("Microsoft YaHei", Font.BOLD, 14);
        Font tickFont  = new Font("Microsoft YaHei", Font.PLAIN, 12);
        Font legendFont = new Font("Microsoft YaHei", Font.PLAIN, 12);

        // 标题
        TextTitle title = chart.getTitle();
        if (title != null) title.setFont(titleFont);

        // 图例
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(legendFont);
        }

        // 柱状图/折线图的坐标轴
        if (chart.getPlot() instanceof CategoryPlot) {
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.getDomainAxis().setLabelFont(labelFont);
            plot.getDomainAxis().setTickLabelFont(tickFont);
            plot.getRangeAxis().setLabelFont(labelFont);
            plot.getRangeAxis().setTickLabelFont(tickFont);
        }

        // 饼图
        if (chart.getPlot() instanceof PiePlot) {
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setLabelFont(tickFont);
        }
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
