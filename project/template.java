import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingWorker;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

public class MemMetric {

    private MySwingWorker mySwingWorker;
    private SwingWrapper<XYChart> sw;
    private XYChart chart;
    private memInfo mem = new memInfo();

    public void start() {
        // Initialize memory info to get total memory
        mem.read();
        long totalMemoryBytes = mem.getTotal();
        long totalMemoryMB = totalMemoryBytes / (1024 * 1024);

        chart = QuickChart.getChart(
                "Memory Usage - Total: " + totalMemoryMB + " MiB",
                "Time",
                "Memory Usage (MiB)",
                "memory_usage",
                new double[] { 0 },
                new double[] { 0 });
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisTicksVisible(false);
        chart.getStyler().setYAxisMin(0.0);
        chart.getStyler().setYAxisMax((double) totalMemoryMB); // Set max to total memory in MB

        sw = new SwingWrapper<>(chart);
        sw.displayChart();

        mySwingWorker = new MySwingWorker(totalMemoryMB);
        mySwingWorker.execute();
    }

    private class MySwingWorker extends SwingWorker<Boolean, double[]> {
        LinkedList<Double> fifo = new LinkedList<>();
        private final long totalMemoryMB;

        public MySwingWorker(long totalMemoryMB) {
            this.totalMemoryMB = totalMemoryMB;
            fifo.add(0.0);
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            while (!isCancelled()) {
                mem.read();
                long usedMemoryBytes = mem.getUsed();
                long usedMemoryMB = usedMemoryBytes / (1024 * 1024); // Convert bytes to MiB

                // Calculate usage percentage
                int usagePercent = (int) ((usedMemoryMB * 100) / totalMemoryMB);

                fifo.add((double) usedMemoryMB);
                if (fifo.size() > 500)
                    fifo.removeFirst();

                double[] array = fifo.stream().mapToDouble(Double::doubleValue).toArray();
                publish(array);

                // Update chart title with current usage
                updateChartTitle(usedMemoryMB, usagePercent);

                Thread.sleep(1000);
            }
            return true;
        }

        @Override
        protected void process(List<double[]> chunks) {
            double[] latest = chunks.get(chunks.size() - 1);
            chart.updateXYSeries("memory_usage", null, latest, null);
            sw.repaintChart();
        }

        private void updateChartTitle(long currentUsageMB, int usagePercent) {
            chart.setTitle(
                    "Memory Usage - " + currentUsageMB + " MiB / " + totalMemoryMB + " MiB (" + usagePercent + "%)");
        }
    }

    public static void main(String[] args) {
        new MemMetric().start();
    }
}