import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

public class MemMetric {
    private MySwingWorker mySwingWorker;
    private JFrame frame;
    private XYChart chart;
    private memInfo mem = new memInfo();

    public void start() {
        // Initialize memory info to get total memory
        mem.read();
        long totalMemoryKB = mem.getTotal();

        chart = QuickChart.getChart(
                "Memory Usage",
                "Time",
                "Memory Usage (%)",
                "memory_usage",
                new double[] { 0 },
                new double[] { 0 });
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisTicksVisible(false);
        chart.getStyler().setYAxisMin(0.0);
        chart.getStyler().setYAxisMax(100.0); // Set Y-axis to 0-100%

        // Create the frame
        frame = new JFrame("Memory Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create chart panel
        XChartPanel<XYChart> chartPanel = new XChartPanel<>(chart);
        frame.add(chartPanel, BorderLayout.CENTER);

        // Show frame
        frame.pack();
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Start updating
        mySwingWorker = new MySwingWorker(totalMemoryKB);
        mySwingWorker.execute();
    }

    private class MySwingWorker extends SwingWorker<Boolean, double[]> {
        LinkedList<Double> fifo = new LinkedList<>();
        private final long totalMemoryKB;

        public MySwingWorker(long totalMemoryKB) {
            this.totalMemoryKB = totalMemoryKB;
            fifo.add(0.0);
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            while (!isCancelled()) {
                mem.read();
                long usedMemoryKB = mem.getUsed();

                // Calculate usage percentage (0-100%)
                double usagePercent = (usedMemoryKB * 100.0) / totalMemoryKB;

                System.out.println("Memory: " + usedMemoryKB + " KB / " + totalMemoryKB + " KB = "
                        + String.format("%.1f", usagePercent) + "%");

                fifo.add(usagePercent);
                if (fifo.size() > 500)
                    fifo.removeFirst();

                double[] array = fifo.stream().mapToDouble(Double::doubleValue).toArray();
                publish(array);

                updateChartTitle(usedMemoryKB, usagePercent);

                Thread.sleep(1000);
            }
            return true;
        }

        @Override
        protected void process(List<double[]> chunks) {
            double[] latest = chunks.get(chunks.size() - 1);
            chart.updateXYSeries("memory_usage", null, latest, null);
            frame.repaint();
        }

        private void updateChartTitle(long currentUsageKB, double usagePercent) {
            long currentUsageMB = currentUsageKB / 1024;
            long totalMemoryMB = totalMemoryKB / 1024;
            chart.setTitle("Memory Usage - " + String.format("%.1f", usagePercent) + "% (" +
                    currentUsageMB + " MiB / " + totalMemoryMB + " MiB)");
        }
    }
}