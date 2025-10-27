import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import systeminfo.Computer;
import systeminfo.CpuCore;
import systeminfo.CpuTimings;

public class Gui {

    public static void showChart(Computer computer) {

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        java.util.List<TimeSeries> coreSeries = new ArrayList<>();

        for (CpuCore core : computer.cpu.cores) {
            TimeSeries myTimeSeries = new TimeSeries("Core " + core.index);
            coreSeries.add(myTimeSeries);
            dataset.addSeries(myTimeSeries);
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart("Live CPU Usage", "Time", "Usage (%)", dataset, true, true, false);

        XYPlot plot = chart.getXYPlot();

        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 100.0);

        DateAxis domain = (DateAxis) plot.getDomainAxis();
        domain.setFixedAutoRange(60000);

        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) plot.getRenderer();
        r.setDefaultShapesVisible(false);

        JFrame frame = new JFrame("CPU Usage Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.setSize(900, 600);
        frame.setVisible(true);

        int[] lastPlotted = new int[computer.cpu.cores.size()];

        // updates once per second on swing thread
        new javax.swing.Timer(1000, e -> {
            Second now = new Second(); 
            for (int i = 0; i < computer.cpu.cores.size(); i++) {
                CpuCore core = computer.cpu.cores.get(i);
                TimeSeries ts = coreSeries.get(i);

                for (int j = lastPlotted[i]; j < core.cpuTimings.size(); j++) {
                    CpuTimings t = core.cpuTimings.get(j);
                    ts.addOrUpdate(now, t.usagePercent);
                }
                lastPlotted[i] = core.cpuTimings.size();

                // deletes points older than 60 seconds as it's not being displayed on graph
                while (ts.getItemCount() > 0 &&
                       ts.getTimePeriod(0).getLastMillisecond() < now.getFirstMillisecond() - 60000) {
                    ts.delete(0, 0);
                }
            }
        }).start();
    }
}
