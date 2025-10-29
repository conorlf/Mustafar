import systeminfo.*;

public class SystemInfoWorker extends Thread {
    private boolean running = true;

    public void run() {
        while (running) {
            // Only handle CPU, Memory, and Disk - USB is now manual refresh only
            template.sampleCpuUsage();
            template.refreshMemoryInfo();
            template.refreshDiskUsage();

            // REMOVED: USB auto-refresh logic
            // if (template.refreshUsbInfo()) {
            // template.showUsbInfo();
            // }

            try {
                Thread.sleep(1000); // Sleep for 1 second between samples
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void stopWorker() {
        running = false;
        interrupt(); // Also interrupt the sleep
    }
}