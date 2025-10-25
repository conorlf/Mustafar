package systeminfo;

import java.util.List;
import java.util.ArrayList;

public class PciBus {
    public int busIndex;
    public List<PciDevice> pciDevices;

    public PciBus(int busIndex) {
        this.busIndex = busIndex;
        this.pciDevices = new ArrayList<>();
    }
}