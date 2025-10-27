
import java.util.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;

/** Listener for USB notifications */
interface UsbNotificationListener {
    void onUsbChange(String message);
}

public final class UsbMonitor implements AutoCloseable {

    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "usb-monitor");
        t.setDaemon(true); // don’t block JVM exit
        return t;
    });

    private final long periodMs; // monitor refresh interval

    // Track current USB devices
    public volatile List<String> oldIds = new ArrayList<>();
    public List<usbDevice> oldList = new ArrayList<>();

    // Listener for notifications
    private UsbNotificationListener listener;

    /** Constructor with period in milliseconds */
    public UsbMonitor(long periodMs) {
        this.periodMs = periodMs;
    }

    /** Set a listener for USB changes */
    public void setNotificationListener(UsbNotificationListener listener) {
        this.listener = listener;
    }

    /** Start periodic scanning */
    public void start() {
        exec.scheduleAtFixedRate(this::scanOnceSafe, 0, periodMs, TimeUnit.MILLISECONDS);
    }

    /** Stop the monitor */
    @Override
    public void close() {
        exec.shutdownNow();
    }

    /** Internal wrapper to catch exceptions */
    private void scanOnceSafe() {
        try {
            scanOnce();
        } catch (Throwable t) {
            System.out.println("[USB Monitor] Error: " + t.getMessage());
        }
    }

    /** Scan USB devices once, detect changes, and notify listener */
    public List<usbDevice> scanOnce() {
        usbInfo usb = new usbInfo();
        usb.read();

        List<String> newIds = new ArrayList<>();
        List<usbDevice> newList = new ArrayList<>();

        for (int bus = 1; bus <= usb.busCount(); bus++) {
            for (int dev = 1; dev <= usb.deviceCount(bus); dev++) {
                usbDevice device = new usbDevice(bus, dev, usb.vendorID(bus, dev), usb.productID(bus, dev));
                newList.add(device);
                newIds.add(device.getUniqueID());
            }
        }

        List<String> added = new ArrayList<>();
        List<String> removed = new ArrayList<>();

        if (!oldIds.isEmpty()) {
            added.addAll(newIds);
            added.removeAll(new HashSet<>(oldIds));

            removed.addAll(oldIds);
            removed.removeAll(new HashSet<>(newIds));
        }

        // Update internal state
        oldIds.clear();
        oldIds.addAll(newIds);
        oldList.clear();
        oldList.addAll(newList);

        // Notify listener
        if (!added.isEmpty() || !removed.isEmpty()) {
            for (String id : added) {
                String msg = "[USB] Added: " + id;
                System.out.println(msg);
                if (listener != null) {
                    // Safe UI update
                    String finalMsg = msg;
                    SwingUtilities.invokeLater(() -> listener.onUsbChange(finalMsg));
                }
            }
            for (String id : removed) {
                String msg = "[USB] Removed: " + id;
                System.out.println(msg);
                if (listener != null) {
                    String finalMsg = msg;
                    SwingUtilities.invokeLater(() -> listener.onUsbChange(finalMsg));
                }
            }
            return newList;
        }

        return null;
    }
}
