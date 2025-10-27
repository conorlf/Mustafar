import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingWorker;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

public class MemMetric {

    MySwingWorker mySwingWorker;
    SwingWrapper<XYChart> sw;
    XYChart chart;
    memInfo mem = new memInfo();

    public void start() {
        // Create Chart
        chart = QuickChart.getChart("Memory Usage", "Time", "Memory Usage (%)", "memoryUsage",
                new double[] { 0 }, new double[] { 0 });
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisTicksVisible(false);
        chart.getStyler().setYAxisMin(0.0);
        chart.getStyler().setYAxisMax(100.0);

        // Show it
        sw = new SwingWrapper<XYChart>(chart);
        sw.displayChart();

        mySwingWorker = new MySwingWorker();
        mySwingWorker.execute();
    }

    private class MySwingWorker extends SwingWorker<Boolean, double[]> {

        LinkedList<Double> fifo = new LinkedList<Double>();
        long totalMemoryKB;

        public MySwingWorker() {
            // Initialize memory and get total memory once
            mem.read();
            totalMemoryKB = mem.getTotal();
            fifo.add(0.0);
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            while (!isCancelled()) {
                // Read current memory usage
                mem.read();
                long usedMemoryKB = mem.getUsed();

                // Calculate memory usage percentage
                double memoryUsagePercent = (usedMemoryKB * 100.0) / totalMemoryKB;

                fifo.add(memoryUsagePercent);
                if (fifo.size() > 500) {
                    fifo.removeFirst();
                }

                double[] array = new double[fifo.size()];
                for (int i = 0; i < fifo.size(); i++) {
                    array[i] = fifo.get(i);
                }
                publish(array);

                try {
                    Thread.sleep(1000); // 1Hz refresh
                } catch (InterruptedException e) {
                    System.out.println("MySwingWorker shut down.");
                }
            }
            return true;
        }

        @Override
        protected void process(List<double[]> chunks) {
            double[] mostRecentDataSet = chunks.get(chunks.size() - 1);

            chart.updateXYSeries("memoryUsage", null, mostRecentDataSet, null);
            sw.repaintChart();

            // Update title with current memory usage
            double currentUsage = mostRecentDataSet[mostRecentDataSet.length - 1];
            long currentUsageMB = (long) ((currentUsage * totalMemoryKB) / (100.0 * 1024));
            long totalMemoryMB = totalMemoryKB / 1024;
            chart.setTitle("Memory Usage - " + String.format("%.1f", currentUsage) + "% (" +
                    currentUsageMB + " MiB / " + totalMemoryMB + " MiB)");
        }
    }
}