import systeminfo.*;

public class SystemInfoWorker extends Thread {
    private boolean running = true;

    public void run() {
        while (running) {
            // takes around 1 second and collects usage data for that one second

            template.sampleCpuUsage();
            template.refreshMemoryInfo();
            template.refreshDiskUsage();
            if (template.refreshUsbInfo()) {
                template.showUsbInfo();
            }

        }
    }

    public void stopWorker() {
        running = false;
    }

}
