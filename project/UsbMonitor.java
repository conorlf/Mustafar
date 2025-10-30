import java.util.*;
import systeminfo.*;
//import java.util.concurrent.*;

public final class UsbMonitor{
    /*private final ScheduledExecutorService exec =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "usb-monitor");
                t.setDaemon(true); // don’t block JVM exit
                return t;
            });

    private final long periodMs;// length of time monitor refreshes
    */
    public volatile List<String> oldIds = new ArrayList<>();
    public List<UsbDevice> oldList = new ArrayList<>();
    public List<UsbDevice> lastAdded = new ArrayList<>();
    public List<UsbDevice> lastRemoved = new ArrayList<>();

    public UsbMonitor() {
        
    }
    /*
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
    }*/


public List<UsbDevice> scanOnce() {
    usbInfo usb = new usbInfo();
    usb.read();

    List<String> newIds = new ArrayList<>();
    List<UsbDevice> newList = new ArrayList<>();

    for (int bus = 1; bus <= usb.busCount(); bus++) {
        for (int dev = 1; dev <= usb.deviceCount(bus); dev++) {
            UsbDevice device = new UsbDevice(bus, dev, usb.vendorID(bus, dev), usb.productID(bus, dev));
            newList.add(device);
            newIds.add(device.getUniqueID());
        }
    }

    List<String> added = new ArrayList<>();
    List<String> removed = new ArrayList<>();

    // Compute changes on first run oldIds is empty, so all are treated as added
    added.addAll(newIds);
    added.removeAll(new HashSet<>(oldIds));

    removed.addAll(oldIds);
    removed.removeAll(new HashSet<>(newIds));

    // Derive concrete device objects for notifications
    lastAdded.clear();
    lastRemoved.clear();
    if (!added.isEmpty()) {
        for (UsbDevice d : newList) {
            if (added.contains(d.getUniqueID())) lastAdded.add(d);
        }
    }
    if (!removed.isEmpty()) {
        for (UsbDevice d : oldList) {
            if (removed.contains(d.getUniqueID())) lastRemoved.add(d);
        }
    }

    // Update state each tick
    oldIds.clear();
    oldIds.addAll(newIds);
    oldList.clear();
    oldList.addAll(newList);

    if (!added.isEmpty() || !removed.isEmpty()) {
        template.getUsbNotification(lastAdded, lastRemoved);

    return newList; 
}

return null;
}
   
} 