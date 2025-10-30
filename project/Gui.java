import javax.swing.*;
import java.awt.*;
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
import systeminfo.Disk;
import systeminfo.DiskBlocks;


public class Gui {

    public static JPanel createSystemInfoTab(Computer computer) {
        // Replace old text area with card-based dashboard
        return new SysInfoDashboard();
    }

    public static void showChart(Computer computer, SystemInfoWorker worker) {

        TimeSeriesCollection datasetCpu = new TimeSeriesCollection();
        List<TimeSeries> coreSeries = new ArrayList<>();

        for (CpuCore core : computer.cpu.cores) {
            TimeSeries coreTimeSeries = new TimeSeries("Core " + core.index);
            coreSeries.add(coreTimeSeries);
            datasetCpu.addSeries(coreTimeSeries);
        }

        TimeSeriesCollection datasetMem = new TimeSeriesCollection();
        TimeSeries memTimeSeries = new TimeSeries("Memory Usage");
        datasetMem.addSeries(memTimeSeries);

        TimeSeriesCollection datasetDisk = new TimeSeriesCollection();
        List<TimeSeries> diskSeries = new ArrayList<>();

        for (Disk disk : computer.disks) {
            TimeSeries diskTimeSeries = new TimeSeries(disk.name);
            diskSeries.add(diskTimeSeries);
            datasetDisk.addSeries(diskTimeSeries);
        }

        JFreeChart cpuChart = ChartFactory.createTimeSeriesChart("CPU Usage", "Time", "Usage (%)", datasetCpu, true, true, false);
        JFreeChart memChart = ChartFactory.createTimeSeriesChart("Memory Usage", "Time", "Usage (%)", datasetMem, true, false, false);
        JFreeChart diskChart = ChartFactory.createTimeSeriesChart("Disk Usage", "Time", "Usage (%)", datasetDisk, true, true, false);


        JTabbedPane tabs = new JTabbedPane();
        SysInfoDashboard dashboard = new SysInfoDashboard();
        tabs.addTab("System Information", dashboard);
        tabs.addTab("CPU Graph", new ChartPanel(cpuChart));
        tabs.addTab("Memory Graph", new ChartPanel(memChart));
        tabs.addTab("Disk Graph", new ChartPanel(diskChart));
        
        JFrame frame = new JFrame();
        frame.add(tabs);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                worker.stopWorker(); 
            }
        });

        // cpu graph config
        XYPlot cpuPlot = cpuChart.getXYPlot();
        NumberAxis cpuRange = (NumberAxis) cpuPlot.getRangeAxis();
        cpuRange.setRange(0.0, 100.0);
        DateAxis cpuDomain = (DateAxis) cpuPlot.getDomainAxis();
        cpuDomain.setFixedAutoRange(60000);
        XYLineAndShapeRenderer cpuRenderer = (XYLineAndShapeRenderer) cpuPlot.getRenderer();
        cpuRenderer.setDefaultShapesVisible(false);

        // memory graph config
        XYPlot memPlot = memChart.getXYPlot();
        NumberAxis memRange = (NumberAxis) memPlot.getRangeAxis();
        memRange.setRange(0.0, 100.0);
        DateAxis memDomain = (DateAxis) memPlot.getDomainAxis();
        memDomain.setFixedAutoRange(60000);
        XYLineAndShapeRenderer memRenderer = (XYLineAndShapeRenderer) memPlot.getRenderer();
        memRenderer.setDefaultShapesVisible(false);

        // disk graph config
        XYPlot diskPlot = diskChart.getXYPlot();
        NumberAxis diskRange = (NumberAxis) diskPlot.getRangeAxis();
        diskRange.setRange(0.0, 100.0);
        DateAxis diskDomain = (DateAxis) diskPlot.getDomainAxis();
        diskDomain.setFixedAutoRange(60000);
        XYLineAndShapeRenderer diskRenderer = (XYLineAndShapeRenderer) diskPlot.getRenderer();
        diskRenderer.setDefaultShapesVisible(false);

        // Glass pane notification setup
        JPanel notificationGlass = new JPanel(new GridBagLayout());
        JLabel notificationLabel = new JLabel("", SwingConstants.CENTER);
        notificationLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        notificationLabel.setForeground(Color.WHITE);
        notificationLabel.setOpaque(true);
        notificationLabel.setBackground(new Color(0, 122, 204, 220));
        notificationLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        notificationGlass.add(notificationLabel, new GridBagConstraints());
        notificationGlass.setVisible(false);
        frame.setGlassPane(notificationGlass);

        frame.setSize(900, 600);
        frame.setVisible(true);

        // provide dashboard to worker so it can refresh cards
        worker.setDashboard(dashboard);

        // notify on USB change: show glass-pane banner briefly
        worker.setUsbChangeNotifier(message -> {
            notificationLabel.setText(message);
            notificationGlass.setVisible(true);
            // hide after 2 seconds
            new javax.swing.Timer(3000, evt -> {
                notificationGlass.setVisible(false);
                ((javax.swing.Timer) evt.getSource()).stop();
            }).start();
        });


        int[] lastPlotted = new int[computer.cpu.cores.size()];
        int[] lastPlottedDisk = new int[computer.disks.size()];
        // updates cpu chart once per second on swing thread
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
                while (ts.getItemCount() > 0 && ts.getTimePeriod(0).getLastMillisecond() < now.getFirstMillisecond() - 60000) { 
                    ts.delete(0, 0);
                }
            }

            // updates memory chart
            memTimeSeries.addOrUpdate(now, computer.memory.usagePercent);
            while (memTimeSeries.getItemCount() > 0 && memTimeSeries.getTimePeriod(0).getLastMillisecond() < now.getFirstMillisecond() - 60000) { 
                memTimeSeries.delete(0, 0);
            }
            
            // updates disk chart
            for (int i = 0; i < computer.disks.size(); i++) {
                Disk disk = computer.disks.get(i);
                TimeSeries ts = diskSeries.get(i);

                for (int j = lastPlottedDisk[i]; j < disk.diskBlocks.size(); j++) {
                    DiskBlocks t = disk.diskBlocks.get(j);
                    ts.addOrUpdate(now, t.usagePercent);
                }
                lastPlottedDisk[i] = disk.diskBlocks.size();

                // deletes points older than 60 seconds as it's not being displayed on graph
                while (ts.getItemCount() > 0 && ts.getTimePeriod(0).getLastMillisecond() < now.getFirstMillisecond() - 60000) { 
                    ts.delete(0, 0);
                }
            }
        }).start();
    }
}
