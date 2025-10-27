public class usbPluggedIn{
usbInfo usb = new usbInfo();
List<String> oldIds = new ArrayList<>();
List<usbDevice> oldList = new ArrayList<>();

while (true) {
    usb.read();

    // fresh containers each tick
    List<String> newIds = new ArrayList<>();
    List<usbDevice> newList = new ArrayList<>();

    for (int bus = 1; bus <= usb.busCount(); bus++) {
        for (int dev = 1; dev <= usb.deviceCount(bus); dev++) {  
            usbDevice device = new usbDevice(bus, dev, usb.vendorID(bus, dev), usb.productID(bus, dev));
            newList.add(device);
            newIds.add(device.getUniqueID());
        }
    }

    if (!oldIds.isEmpty()) {
        List<String> added = new ArrayList<>(newIds);
        added.removeAll(new HashSet<>(oldIds));   // O(n)

        List<String> removed = new ArrayList<>(oldIds);
        removed.removeAll(new HashSet<>(newIds)); // O(n)

        for (String id : added)   System.out.println("Device added: " + id);
        for (String id : removed) System.out.println("Device removed: " + id);
    }

    // prepare for next cycle (copy contents, avoid aliasing)
    oldIds.clear(); 
    oldIds.addAll(newIds); 
    oldList.clear(); 
    oldList.addAll(newList);

    Thread.sleep(1000);
}

}
