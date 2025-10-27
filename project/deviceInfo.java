class usbDevice {
   // attributes
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
   }

   // methods
   public String getUniqueID() {
      return Integer.toString(vendorID) + Integer.toString(productID); // string concatenation
   }
   /*
    * public void setDeviceName() {
    * //empty
    * }
    * 
    * public void setVendorName() {
    * //empty
    * }
    */
}

class pciDevice {
   // attributes
   public int bus;
   public int device;
   public int vendorID;
   public int productID;
   public String vendorName;
   public String deviceName;
   public String function; // not sure if this is an int or a string

   // constructor
   public pciDevice(int bus, int device, int vendorID, int productID) {
      this.bus = bus;
      this.device = device;
      this.vendorID = vendorID;
      this.productID = productID;
   }

   // methods
   public String getUniquePciID() {
      return Integer.toString(vendorID) + Integer.toString(productID); // string concatenation
   }
   /*
    * public void setDeviceName() {
    * //empty
    * }
    * 
    * public void setVendorName() {
    * //empty - call to getVendorName method
    * return "Unknown Vendor";
    * }
    */
}