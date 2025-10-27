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
    private volatile Map<String, UsbDeviceInfo> currentDevices = new ConcurrentHashMap<>();
    private UsbNotificationListener listener;

    public UsbMonitor(long periodMs) {
        this.periodMs = periodMs;
    }

    public void setNotificationListener(UsbNotificationListener listener) {
        this.listener = listener;
    }

    public void start() {
        // Do an initial scan immediately
        scanOnceSafe();
        // Then start periodic scanning
        exec.scheduleAtFixedRate(this::scanOnceSafe, periodMs, periodMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        exec.shutdownNow();
    }

    private void scanOnceSafe() {
        try {
            List<UsbDeviceInfo> changes = scanOnce();
            if (!changes.isEmpty() && listener != null) {
                SwingUtilities.invokeLater(() -> {
                    for (UsbDeviceInfo change : changes) {
                        String message = change.added ? "[USB] Added: " + change.description
                                : "[USB] Removed: " + change.description;
                        listener.onNotification(message);
                    }
                });
            }
        } catch (Throwable t) {
            System.err.println("[USB Monitor] Error during scan: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public List<UsbDeviceInfo> scanOnce() {
        Map<String, UsbDeviceInfo> newDevices = new HashMap<>();
        List<UsbDeviceInfo> changes = new ArrayList<>();

        try {
            usbInfo usb = new usbInfo();
            usb.read();

            System.out.println("[USB Monitor] Scanning - Found " + usb.busCount() + " buses");

            // Build current device list
            for (int bus = 1; bus <= usb.busCount(); bus++) {
                int deviceCount = usb.deviceCount(bus);
                System.out.println("[USB Monitor] Bus " + bus + " has " + deviceCount + " devices");

                for (int dev = 1; dev <= deviceCount; dev++) {
                    try {
                        int vendorId = usb.vendorID(bus, dev);
                        int productId = usb.productID(bus, dev);

                        // Skip invalid devices (all zeros)
                        if (vendorId == 0 && productId == 0) {
                            continue;
                        }

                        String vendorName = Dictionary.getUSBVendorName(vendorId);
                        String deviceName = Dictionary.getUSBDeviceName(vendorId, productId);

                        String deviceId = String.format("Bus%d-Dev%d-%04X-%04X", bus, dev, vendorId, productId);
                        String description = String.format("%s (%04X:%04X) - %s %s",
                                deviceId, vendorId, productId, vendorName, deviceName);

                        UsbDeviceInfo device = new UsbDeviceInfo(deviceId, description, bus, dev, vendorId, productId);
                        newDevices.put(deviceId, device);

                        System.out.println("[USB Monitor] Found: " + description);

                    } catch (Exception e) {
                        System.err.println("[USB Monitor] Error reading device at bus " + bus + ", device " + dev);
                    }
                }
            }

            // Compare with previous scan
            Map<String, UsbDeviceInfo> oldDevices = new HashMap<>(currentDevices);

            // Find new devices
            for (String deviceId : newDevices.keySet()) {
                if (!oldDevices.containsKey(deviceId)) {
                    UsbDeviceInfo device = newDevices.get(deviceId);
                    device.added = true;
                    changes.add(device);
                    System.out.println("[USB Monitor] DEVICE ADDED: " + device.description);
                }
            }

            // Find removed devices
            for (String deviceId : oldDevices.keySet()) {
                if (!newDevices.containsKey(deviceId)) {
                    UsbDeviceInfo device = oldDevices.get(deviceId);
                    device.added = false;
                    changes.add(device);
                    System.out.println("[USB Monitor] DEVICE REMOVED: " + device.description);
                }
            }

            // Update current devices
            currentDevices = newDevices;

        } catch (Exception e) {
            System.err.println("[USB Monitor] Fatal error during USB scan: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[USB Monitor] Scan complete. Current devices: " + currentDevices.size() + ", Changes: "
                + changes.size());
        return changes;
    }

    public List<UsbDeviceInfo> getCurrentDevices() {
        return new ArrayList<>(currentDevices.values());
    }

    // Helper class to store USB device information
    public static class UsbDeviceInfo {
        public final String deviceId;
        public final String description;
        public final int bus;
        public final int device;
        public final int vendorId;
        public final int productId;
        public boolean added;

        public UsbDeviceInfo(String deviceId, String description, int bus, int device, int vendorId, int productId) {
            this.deviceId = deviceId;
            this.description = description;
            this.bus = bus;
            this.device = device;
            this.vendorId = vendorId;
            this.productId = productId;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}