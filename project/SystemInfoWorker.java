import systeminfo.*;
import javax.swing.SwingUtilities;


public class SystemInfoWorker extends Thread {
    private boolean running = true;
    private SysInfoDashboard dashboard;
    public static interface UsbChangeNotifier { void notify(String message); }
    private UsbChangeNotifier usbNotifier;

    public void setDashboard(SysInfoDashboard dashboard) {
        this.dashboard = dashboard;
    }

    public void setUsbChangeNotifier(UsbChangeNotifier notifier) {
        this.usbNotifier = notifier;
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
                    final String msg = template.notificationString;
                    SwingUtilities.invokeLater(() -> usbNotifier.notify(msg));
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
