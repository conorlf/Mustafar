import java.util.*;
import java.util.concurrent.*;

public final class UsbMonitor implements AutoCloseable {
    private final ScheduledExecutorService exec =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "usb-monitor");
                t.setDaemon(true); // don’t block JVM exit
                return t;
            });

    private final long periodMs;// length of time monitor refreshes
    public volatile List<String> oldIds = new ArrayList<>();
    public List<usbDevice> oldList = new ArrayList<>();

    public UsbMonitor(long periodMs) {
        this.periodMs = periodMs;
    }

    public void start() {
        exec.scheduleAtFixedRate(this::scanOnceSafe, 0, periodMs, TimeUnit.MILLISECONDS);
    }

    @Override public void close() {
        exec.shutdownNow();
    }

    private void scanOnceSafe() {
        try { scanOnce(); } catch (Throwable t) {
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

    List<String> added = new ArrayList<>();
    List<String> removed = new ArrayList<>();

    if (!oldIds.isEmpty()) {
        added.addAll(newIds);
        added.removeAll(new HashSet<>(oldIds));

        removed.addAll(oldIds);
        removed.removeAll(new HashSet<>(newIds));
    }

    // Update state each tick
    oldIds.clear();
    oldIds.addAll(newIds);
    oldList.clear();
    oldList.addAll(newList);

    if (!added.isEmpty() || !removed.isEmpty()) {
        for (String id : added)
            System.out.println("[USB] Added: " + id);
        for (String id : removed)
            System.out.println("[USB] Removed: " + id);

        return newList; 
    }

    return null;        
}
   
} 