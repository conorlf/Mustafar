import java.util.LinkedList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

        // Create the frame
        frame = new JFrame("CPU Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create chart panel
        XChartPanel<XYChart> chartPanel = new XChartPanel<>(chart);
        frame.add(chartPanel, BorderLayout.CENTER);

        // Create control panel with button
        JPanel controlPanel = new JPanel();
        stressButton = new JButton("Stress CPU for 15 seconds");
        stressButton.addActionListener(new StressButtonListener());
        controlPanel.add(stressButton);

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

        // Start stress command in a separate thread
        new Thread(() -> {
            try {
                System.out.println("Starting CPU stress test...");

                // Use stress command to generate CPU load
                // -c 2: Use 2 CPU workers
                // -t 15: Run for 15 seconds
                ProcessBuilder pb = new ProcessBuilder("stress", "--cpu", "2", "--timeout", "15");
                Process process = pb.start();

                // Wait for the stress command to complete
                int exitCode = process.waitFor();
                System.out.println("Stress test completed with exit code: " + exitCode);

                // Reset button on completion
                SwingUtilities.invokeLater(() -> {
                    stopStressTest();
                });

            } catch (Exception ex) {
                System.err.println("Error running stress command: " + ex.getMessage());
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    stopStressTest();
                    JOptionPane.showMessageDialog(frame,
                            "Error: Could not run stress command.\nMake sure 'stress' is installed:\nsudo apt-get install stress",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();

        // Set a timer to automatically reset the button after 16 seconds (safety
        // margin)
        stressTimer = new Timer(16000, e -> {
            stopStressTest();
        });
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

        // Kill any running stress processes (safety measure)
        try {
            Runtime.getRuntime().exec("pkill -f stress");
        } catch (Exception ex) {
            // Ignore errors from pkill
        }
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

                System.out.println("CPU Usage: " + cpuUsage + "%");

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

            frame.repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CpuMetric().start();
        });
    }
}