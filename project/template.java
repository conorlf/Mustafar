/*
 *  Example class containing methods to read and display CPU, PCI and USB information
 *
 *  Copyright (c) 2024 Mark Burkley (mark.burkley@ul.ie)
 */
import java.util.Scanner;
public class template 
{
    public static void showPCI()
    {
        pciInfo pci = new pciInfo();
        pci.read();

        System.out.println("\nThis machine has "+
            pci.busCount()+" PCI buses ");

        // Iterate through each bus
        for (int i = 0; i < pci.busCount(); i++) {
            System.out.println("Bus "+i+" has "+
                pci.deviceCount(i)+" devices");

            // Iterate for up to 32 devices.  Not every device slot may be populated
            // so ensure at least one function before printing device information
            for (int j = 0; j < 32; j++) {
                if (pci.functionCount (i, j) > 0) {
                    System.out.println("Bus "+i+" device "+j+" has "+
                        pci.functionCount(i, j)+" functions");

                    // Iterate through up to 8 functions per device.
                    for (int k = 0; k < 8; k++) {
                        if (pci.functionPresent (i, j, k) > 0) {
                            System.out.println("Bus "+i+" device "+j+" function "+k+
                                " has vendor "+String.format("0x%04X", pci.vendorID(i,j,k))+
                                " and product "+String.format("0x%04X", pci.productID(i,j,k))+" and vendor name"+Dictionary.getPCIVendorName(pci.vendorID(i,j,k))+" and device name "+Dictionary.getPCIDeviceName(pci.vendorID(i,j,k),pci.productID(i,j,k)));
                        }
                    }
                }
            }
        }
    }

    public static void showUSB()
    {
        usbInfo usb = new usbInfo();
        usb.read();
        System.out.println("\nThis machine has "+
            usb.busCount()+" USB buses ");

        // Iterate through all of the USB buses
        for (int i = 1; i <= usb.busCount(); i++) {
            System.out.println("Bus "+i+" has "+
                usb.deviceCount(i)+" devices");

            // Iterate through all of the USB devices on the bus
            for (int j = 1; j <= usb.deviceCount(i); j++) {
                System.out.println("Bus "+i+" device "+j+
                    " has vendor "+String.format("0x%04X", usb.vendorID(i,j))+
                    " and product "+String.format("0x%04X", usb.productID(i,j))+" and vendor name "+Dictionary.getUSBVendorName(usb.vendorID(i,j))+" and device name "+Dictionary.getUSBDeviceName(usb.vendorID(i,j),usb.productID(i,j)));
            }
        }
    }

    public static void showCPU()
    {
        cpuInfo cpu = new cpuInfo();
        cpu.read(0);

        // Show CPU model, CPU sockets and cores per socket
        System.out.println("CPU " + cpu.getModel() + " has "+
            cpu.socketCount() + " sockets each with "+
            cpu.coresPerSocket() + " cores");

        // Show sizes of L1,L2 and L3 cache
        System.out.println("l1d="+cpu.l1dCacheSize()+
            ", l1i="+cpu.l1iCacheSize()+
            ", l2="+cpu.l2CacheSize()+
            ", l3="+cpu.l3CacheSize());

        // Sleep for 1 second and display the idle time percentage for
        // core 1.  This assumes 10Hz so in one second we have 100
        cpu.read(1);
        System.out.println("core 1 idle="+cpu.getIdleTime(1)+"%");
    }

    public static void showDisk()
    {
        diskInfo disk = new diskInfo();
        disk.read();

        // Iterate through all of the disks
        for (int i = 0; i < disk.diskCount(); i++) {
            System.out.println ("disk "+disk.getName(i)+" has "+
                disk.getTotal(i)+" blocks, of which "+
                disk.getUsed(i)+" are used");
        }
    }

    public static void showMem()
    {
        memInfo mem = new memInfo();
        mem.read();

        System.out.println ("There is "+mem.getTotal()+" memory of which "+
            mem.getUsed()+" is used");
    }

    public static void main(String[] args)
    {
        System.loadLibrary("sysinfo");
        Scanner input=new Scanner(System.in);
         String usbFilePath = "//home//project//JavaInstall//Mustafar//project//usb.ids";  // Linux
        

        //Load the dictionary
        System.out.println("Loading USB dictionary...");
        long mili1=System.currentTimeMillis();
        Dictionary.loadUSBDictionary(usbFilePath);
        long mili2=System.currentTimeMillis();
        mili2=mili2-mili1;
        System.out.println("Dictionary loaded. "+mili2);
         String pciFilePath = "//home//project//JavaInstall//Mustafar//project//pci.ids";  // Linux
        // Load the dictionary
        System.out.println("Loading PCI dictionary...");
        mili1=System.currentTimeMillis();
        Dictionary.loadPCIDictionary(pciFilePath);
        mili2=System.currentTimeMillis();
        mili2=mili2-mili1;
        System.out.println("Dictionary loaded. "+mili2);
        sysInfo info = new sysInfo();
        cpuInfo cpu = new cpuInfo();
        cpu.read(0);
        try(UsbMonitor monitor = new UsbMonitor(1000)){
        monitor.start();
        boolean isRunning=true;
        while(isRunning){
        System.out.println("==========Welcome to the System Information Tool==========");
        System.out.println("Press 1) View CPU Information");
        System.out.println("Press 2) View PCI Information");
        System.out.println("Press 3) View connected USB Devices");
        System.out.println("Press 4) View current Memory Usage");
        System.out.println("Press 5) View Disks");
        System.out.println("Press x) To Exit");
        char choice = Character.toLowerCase(input.next().charAt(0));
        switch(choice){
        case '1':{
            showCPU();
            break;
        }
        case '2':{
            showPCI();
            break;
        }
        case '3':{
            showUSB();
            break;
        }
        case '4':{
            showDisk();
            break;
        }
        case '5':{
            showMem();
            break;
        }
        case 'x':{
            System.out.println("System shutting down...");
            System.exit(0);
        }
        default:{
            System.out.println("Invalid input. Please try again");
        }
        }
    }
    }
}
}

