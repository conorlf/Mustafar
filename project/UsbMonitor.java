import java.util.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;

public final class UsbMonitor implements AutoCloseable {

    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "usb-monitor");
        t.setDaemon(true);
        return t;
    });

    private final long periodMs;
    private volatile Map<String, usbDevice> currentDevices = new ConcurrentHashMap<>();
    private UsbNotificationListener listener;

    public UsbMonitor(long periodMs) {
        this.periodMs = periodMs;
    }

    public void setNotificationListener(UsbNotificationListener listener) {
        this.listener = listener;
    }

    public void start() {
        exec.scheduleAtFixedRate(this::scanOnceSafe, 0, periodMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        exec.shutdownNow();
    }

    private void scanOnceSafe() {
        try {
            scanOnce();
        } catch (Throwable t) {
            System.out.println("[USB Monitor] Error: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public List<usbDevice> scanOnce() {
        System.out.println("[USB Monitor] Scanning for USB device changes...");
        usbInfo usb = new usbInfo();
        usb.read();

        Map<String, usbDevice> newDevices = new HashMap<>();
        List<usbDevice> deviceList = new ArrayList<>();

        // Build current device list
        for (int bus = 1; bus <= usb.busCount(); bus++) {
            for (int dev = 1; dev <= usb.deviceCount(bus); dev++) {
                try {
                    usbDevice device = new usbDevice(bus, dev, usb.vendorID(bus, dev), usb.productID(bus, dev));
                    String deviceId = device.getUniqueID();
                    newDevices.put(deviceId, device);
                    deviceList.add(device);

                    // Debug output
                    System.out.println("[USB Monitor] Found device: " + deviceId +
                            " (Vendor: " + usb.vendorID(bus, dev) +
                            ", Product: " + usb.productID(bus, dev) + ")");
                } catch (Exception e) {
                    System.out.println("[USB Monitor] Error reading device at bus " + bus + ", device " + dev);
                    e.printStackTrace();
                }
            }
        }

        // Detect changes
        Map<String, usbDevice> oldDevices = new HashMap<>(currentDevices);

        // Find added devices
        for (String deviceId : newDevices.keySet()) {
            if (!oldDevices.containsKey(deviceId)) {
                String msg = "[USB] Added: " + deviceId;
                System.out.println(msg);
                notifyListener(msg);
            }
        }

        // Find removed devices
        for (String deviceId : oldDevices.keySet()) {
            if (!newDevices.containsKey(deviceId)) {
                String msg = "[USB] Removed: " + deviceId;
                System.out.println(msg);
                notifyListener(msg);
            }
        }

        // Update current devices
        currentDevices = newDevices;

        System.out.println("[USB Monitor] Scan complete. Total devices: " + currentDevices.size());
        return deviceList;
    }

    public List<usbDevice> getCurrentDevices() {
        return new ArrayList<>(currentDevices.values());
    }

    private void notifyListener(String msg) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onNotification(msg));
        }
    }
}