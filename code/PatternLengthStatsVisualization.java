import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PatternLengthStatsVisualization {
    public static void main(String[] args) {
        String inputFile = "ResultStatistic/uncertain_output_Stats.txt"; // 统计结果文件路径

        // 创建数据集
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            boolean startStatistics = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("Final Pattern Length Statistics:")) {
                    startStatistics = true;
                    continue;
                }
                if (startStatistics && line.trim().startsWith("Length=")) {
                    String[] parts = line.trim().split("\\|");
                    String lengthStr = parts[0].trim().split("=")[1];
                    double percentage = Double.parseDouble(parts[2].trim().split("=")[1].replace("%", ""));
                    dataset.addValue(percentage, "Length Percentage", lengthStr);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 创建柱状图
        JFreeChart chart = ChartFactory.createBarChart(
                "Pattern Length Statistics",
                "Length of sequence",
                "Percentage",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // 自定义柱状图
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.RED); // 设置柱状图颜色

        // 创建图表面板并显示
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("Pattern Length Statistics Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
