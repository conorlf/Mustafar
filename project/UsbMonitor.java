import java.util.*;
import java.util.concurrent.*;

import javax.swing.SwingUtilities;

import systeminfo.*;

public final class UsbMonitor implements AutoCloseable {
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "usb-monitor");
        t.setDaemon(true); // allow JVM to exit
        return t;
    });

    private final long periodMs = 2000L; // scan every 2 seconds (adjust as needed)

    // state used to compute diffs
    private final List<String> oldIds = new ArrayList<>();
    private final List<UsbDevice> oldList = new ArrayList<>();

    // optional listener (single) that can be notified when devices change
    public interface Listener {
        void onUsbChange(List<UsbDevice> newList, List<String> added, List<String> removed);
    }

    private volatile Listener listener;

    public UsbMonitor() {
    }

    /** Start periodic scanning. Idempotent if already started. */
    public void start() {
        exec.scheduleAtFixedRate(this::scanOnceSafe, 0, periodMs, TimeUnit.MILLISECONDS);
    }

    /** Stop scanning and free resources. */
    @Override
    public void close() {
        exec.shutdownNow();
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    private void scanOnceSafe() {
        try {
            List<UsbDevice> newList = scanOnce(); // returns newList (never null)

        } catch (Throwable t) {
            System.out.println("[USB Monitor] Error: " + t);
            t.printStackTrace();
        }
    }

    /**
     * Do one scan and return the list of devices (never null).
     * Also prints added/removed and notifies optional listener.
     */
    public List<UsbDevice> scanOnce() {
        usbInfo usb = new usbInfo();
        usb.read();

        System.out.println("[RAW C++ DATA] Bus count: " + usb.busCount());
        for (int bus = 1; bus <= usb.busCount(); bus++) {
            int devCount = usb.deviceCount(bus);
            System.out.println("[RAW C++ DATA] Bus " + bus + " devices: " + devCount);
            for (int dev = 1; dev <= devCount; dev++) {
                int vendor = usb.vendorID(bus, dev);
                int product = usb.productID(bus, dev);
                System.out.printf("[RAW C++ DATA] Bus %d Dev %d: %04X:%04X%n",
                        bus, dev, vendor, product);
            }
        }

        List<String> newIds = new ArrayList<>();
        List<UsbDevice> newList = new ArrayList<>();

        for (int bus = 1; bus <= usb.busCount(); bus++) {
            for (int dev = 1; dev <= usb.deviceCount(bus); dev++) {
                UsbDevice device = new UsbDevice(bus, dev, usb.vendorID(bus, dev), usb.productID(bus, dev));
                newList.add(device);
                newIds.add(device.getUniqueID());
            }
        }

        // compute deltas
        List<String> added = new ArrayList<>(newIds);
        added.removeAll(oldIds);

        List<String> removed = new ArrayList<>(oldIds);
        removed.removeAll(newIds);

        // update state
        synchronized (this) {
            oldIds.clear();
            oldIds.addAll(newIds);
            oldList.clear();
            oldList.addAll(newList);
        }

        if (!added.isEmpty() || !removed.isEmpty()) {
            for (String id : added)
                System.out.println("[USB] Added: " + id);
            for (String id : removed)
                System.out.println("[USB] Removed: " + id);

        }

        // notify listener (non-blocking: call on a new thread to avoid blocking
        // scheduler)
        Listener l = listener;
        if (l != null) {
            final List<UsbDevice> snapshot = Collections.unmodifiableList(new ArrayList<>(newList));
            final List<String> addedSnapshot = Collections.unmodifiableList(new ArrayList<>(added));
            final List<String> removedSnapshot = Collections.unmodifiableList(new ArrayList<>(removed));
            // notify off the scheduler thread to avoid long-running listener blocking scans
            CompletableFuture.runAsync(() -> {
                try {
                    l.onUsbChange(snapshot, addedSnapshot, removedSnapshot);
                } catch (Throwable t) {
                    System.out.println("[USB Monitor] listener error: " + t);
                }
            });
        }

        return newList;
    }
}
