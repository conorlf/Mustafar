
public class UsbDevice{
   //attributes
   public int bus;
   public int device;
   public int vendorID;
   public int productID;
   public String vendorName;
   public String deviceName;
   
   // Constructor
   public UsbDevice(int bus, int device, int vendorID, int productID) {
      this.bus = bus;
      this.device = device;
      this.vendorID = vendorID;
      this.productID = productID;
      this.vendorName=Dictionary.getUSBVendorName(vendorID);
      this.deviceName=Dictionary.getUSBDeviceName(vendorID, productID);
   }
   
   //methods
   public String getUniqueID() {
      return Integer.toString(vendorID) +":"+Integer.toString(productID); //string concatenation
   }  
    public void displayUsbInfo() {
    System.out.println(
        "Bus " + bus +
        " device " + device +
        " has vendor " + String.format("0x%04X", vendorID) +
        " and product " + String.format("0x%04X", productID) +
        " and vendor name " + vendorName +
        " and device name " + deviceName
    );
} 
}
  /* 
   public void setDeviceName() {
      //empty
   }
   
   public void setVendorName() {
      //empty
   }*/
