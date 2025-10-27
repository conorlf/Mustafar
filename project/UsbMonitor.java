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
    private volatile List<String> oldIds = new ArrayList<>();
    private List<usbDevice> oldList = new ArrayList<>();

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
        }
    }

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

        // Detect added devices
        List<String> added = new ArrayList<>(newIds);
        added.removeAll(oldIds);

        // Detect removed devices
        List<String> removed = new ArrayList<>(oldIds);
        removed.removeAll(newIds);

        // Update oldIds with a copy
        oldIds.clear();
        oldIds.addAll(newIds);

        // Notify listener
        for (String id : added) {
            String msg = "[USB] Added: " + id;
            System.out.println(msg);
            notifyListener(msg);
        }
        for (String id : removed) {
            String msg = "[USB] Removed: " + id;
            System.out.println(msg);
            notifyListener(msg);
        }

        oldList = newList;

        return newList;
    }

    private void notifyListener(String msg) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onNotification(msg));
        }
    }
}
