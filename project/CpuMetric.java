import java.util.LinkedList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

public class CpuMetric {

    private MySwingWorker mySwingWorker;
    private JFrame frame;
    private XYChart chart;
    private cpuInfo cpu = new cpuInfo();
    private JButton stressButton;
    private Timer stressTimer;
    private boolean isStressing = false;
    private final int REFRESH_RATE_MS = 250; // 4 times per second (1000ms / 4 = 250ms)

    public void start() {
        // Initialize CPU monitoring
        cpu.read();

        // Create Chart
        chart = QuickChart.getChart(
                "CPU Usage (Real-Time - 4Hz)",
                "Time",
                "CPU Usage %",
                "cpu_usage",
                new double[] { 0 },
                new double[] { 0 });
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisTicksVisible(false);
        chart.getStyler().setYAxisMin(0.0);
        chart.getStyler().setYAxisMax(100.0);

        // Create the frame
        frame = new JFrame("CPU Monitor - 4Hz Refresh");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create chart panel
        XChartPanel<XYChart> chartPanel = new XChartPanel<>(chart);
        frame.add(chartPanel, BorderLayout.CENTER);

        // Create control panel with button
        JPanel controlPanel = new JPanel();
        stressButton = new JButton("Stress CPU for 15 seconds");
        stressButton.addActionListener(new StressButtonListener());

        JLabel refreshLabel = new JLabel("Refresh: 4Hz");
        refreshLabel.setForeground(Color.BLUE);

        controlPanel.add(stressButton);
        controlPanel.add(refreshLabel);

        frame.add(controlPanel, BorderLayout.SOUTH);

        // Show frame
        frame.pack();
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Start updating
        mySwingWorker = new MySwingWorker();
        mySwingWorker.execute();
    }

    private class StressButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isStressing) {
                startStressTest();
            } else {
                stopStressTest();
            }
        }
    }

    private void startStressTest() {
        isStressing = true;
        stressButton.setText("Stop Stress Test");
        stressButton.setBackground(Color.RED);
        stressButton.setForeground(Color.WHITE);

        new Thread(() -> {
            try {
                int cores = Runtime.getRuntime().availableProcessors();
                System.out.println("Starting " + cores + " CPU-intensive threads...");

                // Create multiple threads that do intensive math
                List<Thread> threads = new ArrayList<>();
                for (int i = 0; i < cores; i++) {
                    Thread t = new Thread(() -> {
                        long startTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - startTime < 15000
                                && !Thread.currentThread().isInterrupted()) {
                            // Intensive floating point calculations
                            double result = 0;
                            for (int j = 0; j < 1000000; j++) {
                                result += Math.sqrt(j) * Math.sin(j) * Math.cos(j);
                            }
                        }
                    });
                    t.start();
                    threads.add(t);
                }

                // Wait for 15 seconds
                Thread.sleep(15000);

                // Stop all threads
                for (Thread t : threads) {
                    t.interrupt();
                }

                System.out.println("Java-based stress test completed");
                SwingUtilities.invokeLater(() -> stopStressTest());

            } catch (Exception ex) {
                System.err.println("Error in Java stress test: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> stopStressTest());
            }
        }).start();

        stressTimer = new Timer(16000, e -> stopStressTest());
        stressTimer.setRepeats(false);
        stressTimer.start();
    }

    private void stopStressTest() {
        isStressing = false;
        stressButton.setText("Stress CPU for 15 seconds");
        stressButton.setBackground(null);
        stressButton.setForeground(null);

        if (stressTimer != null) {
            stressTimer.stop();
        }
    }

    private class MySwingWorker extends SwingWorker<Boolean, double[]> {
        LinkedList<Double> fifo = new LinkedList<>();
        private long lastReadTime = 0;
        private final long CPU_READ_INTERVAL = 1000; // Read CPU stats once per second
        private double lastCpuUsage = 0.0;

        public MySwingWorker() {
            fifo.add(0.0);
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            while (!isCancelled()) {
                long currentTime = System.currentTimeMillis();

                // Only read CPU stats once per second to avoid overwhelming the system
                if (currentTime - lastReadTime >= CPU_READ_INTERVAL) {
                    cpu.read(1); // Read with 1 second interval for accurate percentages

                    // Get idle percentage and convert to CPU usage percentage
                    int idlePercent = cpu.getIdleTime(1);
                    lastCpuUsage = 100 - idlePercent;

                    System.out.println("CPU Usage: " + String.format("%.1f", lastCpuUsage) + "%");
                    lastReadTime = currentTime;
                }

                // Add current CPU usage to the FIFO buffer
                fifo.add(lastCpuUsage);
                if (fifo.size() > 200) // Keep more points for smoother graph
                    fifo.removeFirst();

                double[] array = fifo.stream().mapToDouble(Double::doubleValue).toArray();
                publish(array);

                // Sleep for 250ms to achieve 4Hz refresh rate
                Thread.sleep(REFRESH_RATE_MS);
            }
            return true;
        }

        @Override
        protected void process(List<double[]> chunks) {
            if (!chunks.isEmpty()) {
                double[] latest = chunks.get(chunks.size() - 1);
                chart.updateXYSeries("cpu_usage", null, latest, null);

                // Update title with current value
                double currentValue = latest[latest.length - 1];
                chart.setTitle("CPU Usage - Current: " + String.format("%.1f", currentValue) + "% (4Hz)");

                frame.repaint();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CpuMetric().start();
        });
    }
}