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
        cpu.read(0);
        // Create Chart
        chart = QuickChart.getChart(
                "CPU Usage (Real-Time Demo)",
                "Time",
                "Value",
                "randomWalk",
                new double[] { 0 },
                new double[] { 0 });
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisTicksVisible(false);

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
                cpu.read(1);
                fifo.add((double) cpu.getIdleTime(1));
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
            chart.updateXYSeries("randomWalk", null, latest, null);
            sw.repaintChart();
        }
    }
}
