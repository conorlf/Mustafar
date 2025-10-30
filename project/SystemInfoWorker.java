import systeminfo.*;
import javax.swing.SwingUtilities;


public class SystemInfoWorker extends Thread {
    private boolean running = true;
    private SysInfoDashboard dashboard;

    public void setDashboard(SysInfoDashboard dashboard) {
        this.dashboard = dashboard;
    }


    public void run() {
        while (running) {   
            // takes around 1 second and collects usage data for that one second
            
            template.sampleCpuUsage();
            template.refreshMemoryInfo();
            template.refreshDiskUsage();

            // refresh CPU and Memory cards every tick
            if (dashboard != null) {
                SwingUtilities.invokeLater(() -> {
                    dashboard.refreshCpu();
                    dashboard.refreshMemory();
                });
            }

            // refresh USB card only when devices change
            if (template.refreshUsbInfo()) {
                if (dashboard != null) {
                    SwingUtilities.invokeLater(() -> dashboard.refreshUsb());
                } else {
                    template.showUsbInfo();
                }
            }
            
            


        }
    }

    public void stopWorker() {
        running = false;
    }

}
