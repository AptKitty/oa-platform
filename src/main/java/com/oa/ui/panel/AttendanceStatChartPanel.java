package com.oa.ui.panel;

import com.oa.statistics.service.StatService;
import com.oa.statistics.entity.StatResultVO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AttendanceStatChartPanel extends BasePanel {

    private StatService statService = new StatService();

    public AttendanceStatChartPanel() {
        initUI();
    }

    @Override
    public String getPanelKey() { return "STAT_ATTENDANCE"; }

    @Override
    public String getPanelTitle() { return "考勤统计图表"; }

    private void initUI() {
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);
        refresh();
    }

    private void refresh() {
        removeAll();
        add(createToolBar(this::refresh, null, null), BorderLayout.NORTH);

        JPanel chartPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        try {
            // 柱状图：部门出勤对比
            DefaultCategoryDataset barData = new DefaultCategoryDataset();
            List<StatResultVO> list = statService.getAttendanceComparison(2026, 6);
            for (StatResultVO vo : list) {
                if (vo.getValue() instanceof Number) {
                    barData.addValue(((Number) vo.getValue()).doubleValue(), "打卡次数", vo.getName());
                }
            }
            JFreeChart barChart = ChartFactory.createBarChart("部门出勤对比", "部门", "打卡次数", barData);
            chartPanel.add(new ChartPanel(barChart));
        } catch (Exception e) {
            chartPanel.add(new JLabel("出勤数据暂无"));
        }

        try {
            // 饼图：请假类型分布
            DefaultPieDataset pieData = new DefaultPieDataset();
            List<StatResultVO> list2 = statService.getLeaveDistribution(2026, 6);
            for (StatResultVO vo : list2) {
                if (vo.getValue() instanceof Number) {
                    pieData.setValue(vo.getName(), ((Number) vo.getValue()).doubleValue());
                }
            }
            JFreeChart pieChart = ChartFactory.createPieChart("请假类型分布", pieData, true, true, false);
            chartPanel.add(new ChartPanel(pieChart));
        } catch (Exception e) {
            chartPanel.add(new JLabel("请假数据暂无"));
        }

        add(chartPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // ===== 测试入口 =====
    public static void main(String[] args) {
        JFrame frame = new JFrame("考勤统计");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 500);
        frame.setLocationRelativeTo(null);
        AttendanceStatChartPanel panel = new AttendanceStatChartPanel();
        panel.setCurrentUser(1L, "测试用户");
        frame.add(panel);
        frame.setVisible(true);
    }
}