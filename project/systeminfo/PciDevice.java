package systeminfo;

import java.util.List;
import java.util.ArrayList;

public class PciDevice {
    public int deviceIndex;
    public List<PciFunction> pciFunctions;

    public PciDevice(int deviceIndex) {
        this.deviceIndex = deviceIndex;
        this.pciFunctions = new ArrayList<>();
    }
}