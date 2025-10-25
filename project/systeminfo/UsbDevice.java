package systeminfo;

public class UsbDevice {
    public int deviceIndex;
    public String vendorId;
    public String productId;

    public UsbDevice(int deviceIndex, String vendorId, String productId) {
        this.deviceIndex = deviceIndex;
        this.vendorId = vendorId;
        this.productId = productId;
    }
}   