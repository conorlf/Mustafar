import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

/**
 * Simple real-time CPU metric demo using XChart and SwingWorker
 */
public class CpuMetric {

    private MySwingWorker mySwingWorker;
    private SwingWrapper<XYChart> sw;
    private XYChart chart;
    cpuInfo cpu = new cpuInfo();

    public void start() {
        // Initialize CPU monitoring
        cpu.read();

        // Create Chart
        chart = QuickChart.getChart(
                "CPU Usage (Real-Time)",
                "Time",
                "CPU Usage %",
                "cpu_usage",
                new double[] { 0 },
                new double[] { 0 });
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisTicksVisible(false);
        chart.getStyler().setYAxisMin(0.0);
        chart.getStyler().setYAxisMax(100.0);

        // Show it
        sw = new SwingWrapper<>(chart);
        sw.displayChart();

        // Start updating
        mySwingWorker = new MySwingWorker();
        mySwingWorker.execute();
    }

    private class MySwingWorker extends SwingWorker<Boolean, double[]> {
        LinkedList<Double> fifo = new LinkedList<>();

        public MySwingWorker() {
            fifo.add(0.0);
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            while (!isCancelled()) {
                // Read CPU stats with 1 second interval
                cpu.read(1);

                // Get idle percentage and convert to CPU usage percentage
                int idlePercent = cpu.getIdleTime(1);
                int cpuUsage = 100 - idlePercent;

                System.out.println("Idle: " + idlePercent + "%, CPU Usage: " + cpuUsage + "%");

                fifo.add((double) cpuUsage);
                if (fifo.size() > 500)
                    fifo.removeFirst();

                double[] array = fifo.stream().mapToDouble(Double::doubleValue).toArray();
                publish(array);
                Thread.sleep(1000);
            }
            return true;
        }

        @Override
        protected void process(List<double[]> chunks) {
            double[] latest = chunks.get(chunks.size() - 1);
            chart.updateXYSeries("cpu_usage", null, latest, null);

            // Update title with current value
            double currentValue = latest[latest.length - 1];
            chart.setTitle("CPU Usage - Current: " + String.format("%.1f", currentValue) + "%");

            sw.repaintChart();
        }
    }

    public static void main(String[] args) {
        new CpuMetric().start();
    }
}