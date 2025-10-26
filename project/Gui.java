import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import systeminfo.*;

public class Gui {

    public static void showChart(Computer computer) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        // For each CPU core, create a series and populate it
        for (CpuCore core : computer.cpu.cores) {
            XYSeries series = new XYSeries("Core " + core.index);

            for (CpuTimings timing : core.cpuTimings) {
                series.add(timing.secondsSinceStart, timing.usagePercent);
            }

            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "CPU Usage Static)",
                "Time (seconds)",
                "Usage (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,   // Show legend
                true,   // Use tooltips
                false   // Configure chart to generate URLs?
        );

        // Create and display the chart in a window
        JFrame frame = new JFrame("CPU Usage Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
    }
}
