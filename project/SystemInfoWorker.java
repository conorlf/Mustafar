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
                } else {
                    template.showUsbInfo();
                }
                if (usbNotifier != null) {
                    // Build a message listing all additions/removals
                    StringBuilder msg = new StringBuilder();
                    for (UsbDevice d : template.usbScan1.lastAdded) {
                        msg.append("USB Added:\n").append(d.vendorName).append("\t").append(d.deviceName).append("\n");
                    }
                    for (UsbDevice d : template.usbScan1.lastRemoved) {
                        msg.append("USB Removed:\n").append(d.vendorName).append("\t").append(d.deviceName).append("\n");
                    }
                    String finalMsg = msg.length() == 0 ? "USB devices changed" : msg.toString().trim();
                    SwingUtilities.invokeLater(() -> usbNotifier.notify(finalMsg));
                }
            }
            
            


        }
    }

    public void stopWorker() {
        running = false;
    }

}
