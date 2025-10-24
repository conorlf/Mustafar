public class usbDevice{
   //attributes
   public int bus;
   public int device;
   public int vendorID;
   public int productID;
   public String vendorName;
   public String deviceName;
   
   // Constructor
   public usbDevice(int bus, int device, int vendorID, int productID) {
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
  /* 
   public void setDeviceName() {
      //empty
   }
   
   public void setVendorName() {
      //empty
   }*/
}