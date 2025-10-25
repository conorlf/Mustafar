package systeminfo;

import java.util.List;
import java.util.ArrayList;

public class UsbBus {
    public int busIndex;
    public List<UsbDevice> usbDevices;

    public UsbBus (int busIndex) {
        this.busIndex = busIndex;
        this.usbDevices = new ArrayList<>();
    }
}