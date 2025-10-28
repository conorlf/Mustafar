
class PciDevice{
   //attributes
   public int bus;
   public int device;
   public int function;
   public int vendorID;
   public int productID;
   public String vendorName;
   public String deviceName;
  
   //constructor
   public PciDevice(int bus, int device, int function, int vendorID, int productID) {
      this.bus = bus;
      this.device = device;
      this.function = function;
      this.vendorID = vendorID;
      this.productID = productID;
      this.vendorName=Dictionary.getPCIVendorName(vendorID);
      this.deviceName=Dictionary.getPCIDeviceName(vendorID, productID);
   }
   
   //methods
   public String getUniquePciID() {
      return Integer.toString(vendorID)+":"+ Integer.toString(productID); //string concatenation
   }
    public void displayPciInfo(){
         System.out.printf("%-8d %-8d %-8d 0x%04X      0x%04X      %-30s %-30s%n",bus,device,function,vendorID,productID,vendorName,deviceName);
    }
   /*
   public void setDeviceName() {
      //empty
   }
   
   public void setVendorName() {
      //empty - call to getVendorName method
      return "Unknown Vendor";
   }*/
}