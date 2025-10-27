// import java.util.ArrayList;
// import java.util.List;

// public class USBMonitor {

// public static void main(String[] args) throws InterruptedException {

// usbInfo usb = new usbInfo();
// List<String> oldIds = new ArrayList<>();
// List<usbDevice> oldList = new ArrayList<>();

// while (true) {
// // Read current USB devices
// List<String> newIds = new ArrayList<>();
// List<usbDevice> newList = new ArrayList<>();

// for (int bus = 1; bus <= 5; bus++) { // example loop
// for (int dev = 1; dev <= 10; dev++) {
// usbDevice device = new usbDevice(bus, dev);
// newList.add(device);
// newIds.add(device.getId());
// }
// }

// if (!oldIds.isEmpty()) {
// List<String> added = new ArrayList<>(newIds);
// added.removeAll(oldIds);

// List<String> removed = new ArrayList<>(oldIds);
// removed.removeAll(newIds);

// for (String id : added) {
// System.out.println("Added: " + id);
// }
// for (String id : removed) {
// System.out.println("Removed: " + id);
// }
// }

// // Prepare for next cycle
// oldIds.clear();
// oldIds.addAll(newIds);
// oldList.clear();
// oldList.addAll(newList);

// Thread.sleep(1000);
// }
// }
// }
