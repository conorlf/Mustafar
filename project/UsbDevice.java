
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
    System.out.printf("%-8d %-8d 0x%04X      0x%04X      %-30s %-30s%n",bus,device,vendorID,productID,vendorName,deviceName);
} 
}
  /* 
   public void setDeviceName() {
      //empty
   }
   
   public void setVendorName() {
      //empty
   }*/
