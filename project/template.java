/*
 *  Example class containing methods to read and display CPU, PCI and USB information
 *
 *  Copyright (computer) 2024 Mark Burkley (mark.burkley@ul.ie)
 */

import systeminfo.*;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
//import java.util.Dictionary;

public class template 
{
    public static Computer computer = new Computer();
    public static cpuInfo cpu = new cpuInfo();
    public static memInfo mem = new memInfo();
    public static diskInfo disk = new diskInfo();
    public static pciInfo pci = new pciInfo();
    public static UsbMonitor usbScan1 = new UsbMonitor();
    public static List<UsbDevice> usbDevices = new ArrayList<>();
    public static List<PciDevice> pciDevices = new ArrayList<>();
    //public static int pciBusCount, pciDeviceCount,pciFunctionCount;
    public static void loadCpuInfo() {
        cpu.read(0);
        // read all the static info - i.e. nujmber cores, model etc.populate it to our data structure
        Cpu myCpu = new Cpu(cpu.getModel(), cpu.socketCount(), cpu.coresPerSocket(), cpu.l1dCacheSize(), cpu.l1iCacheSize(), cpu.l2CacheSize(), cpu.l3CacheSize());
        
        // assuming all sockets are identical
        for (int j = 0; j < myCpu.coresPerSocket; j++){
            CpuCore core = new CpuCore(j);
            myCpu.cores.add(core);
        }
        computer.cpu = myCpu;
    }

    public static void  loadPciInfo() {
        
        pci.read();
        
        

        for (int bus = 0; bus < pci.busCount(); bus++) {
        for (int dev = 0; dev < pci.deviceCount(bus); dev++) {
        for (int function = 0; function < 8; function++) {
                if (pci.functionPresent (bus, dev, function) > 0) {
                        PciDevice device = new PciDevice(bus, dev, function, pci.vendorID(bus, dev,function), pci.productID(bus, dev,function));
                        pciDevices.add(device);
                        }
        }
            
        }
    }    
        
    
    }
    public static void showPCI()
    {
        

        System.out.println("\nThis machine has "+
            pci.busCount()+" PCI buses ");

        // Iterate through each bus
        for (int i = 0; i < pci.busCount(); i++) {
            System.out.println("Bus "+i+" has "+
                pci.deviceCount(i)+" devices");

            
        }
         if (pciDevices == null || pciDevices.isEmpty()) {
        System.out.println("No PCI devices found.");
        return;
             }
            System.out.println("PCI Devices Detected: " + pciDevices.size());

            System.out.printf( "%-8s %-8s %-8s %-12s %-12s %-30s %-30s%n","Bus", "Device", "Func", "Vendor ID", "Product ID", "Vendor Name", "Device Name");

            for (PciDevice device : pciDevices) {
                device.displayPciInfo();
            }

    }

    public static void refreshUsbInfo() {
                // Iterate through all of the USB buses
        

        List<UsbDevice> result = usbScan1.scanOnce();

        if (result != null) {
            usbDevices.clear();         // remove old elements
            usbDevices.addAll(result); 
        }
         
        
    }
    public static void showUsbInfo(){
            System.out.println("showUsbInfo(): Found " + usbDevices.size() + " devices.");

            for(UsbDevice device:usbDevices){
            device.displayUsbInfo();
        }


    }

    // disks are one based
    public static void loadDiskInfo() {
        disk.read();
        for (int i = 0; i < disk.diskCount(); i++) {
            Disk myDisk = new Disk(i, disk.getName(i));
            computer.disks.add(myDisk);
        }
        refreshDiskUsage();
    }

    public static void refreshDiskUsage() {
        disk.read();
        for (int i = 0; i < disk.diskCount(); i++) {
            DiskBlocks db = new DiskBlocks(disk.getUsed(i), disk.getTotal(i), disk.getAvailable(i));
            computer.disks.get(i).diskBlocks.add(db);
        }
    }

    public static void loadMemoryInfo() {
        mem.read();
        Memory myMemory = new Memory(mem.getTotal(), mem.getUsed());
        computer.memory = myMemory;
    }

    public static void sampleCpuUsage() {
        cpu.read(1);
        for (int i = 0; i < computer.cpu.cores.size(); i++) {
            int idleTime = cpu.getIdleTime(i);
            int systemTime = cpu.getSystemTime(i);
            int userTime = cpu.getUserTime(i);
            int totalTime = idleTime + systemTime + userTime;

            double idlePercent = ((double) idleTime / totalTime) * 100;
            double systemPercent = ((double) systemTime / totalTime) * 100;
            double userPercent = ((double) userTime / totalTime) * 100;

            CpuTimings myCpuTimings = new CpuTimings(idlePercent, userPercent, systemPercent);
            computer.cpu.cores.get(i).cpuTimings.add(myCpuTimings);
        }
    }

    public static void refreshMemoryInfo() {
        mem.read();
        computer.memory.update(mem.getTotal(), mem.getUsed());
    }



    public static void main(String[] args)
    {
        System.loadLibrary("sysinfo");
      //  System.loadLibrary("sysinfo");
      String usbPath = "//home//vboxuser//Mustafar//project//usb.ids";
      System.out.println("Loading USB dictionary...");
      long var3 = System.currentTimeMillis();
      Dictionary.loadUSBDictionary(usbPath);
      long var5 = System.currentTimeMillis();
      var5 -= var3;
      System.out.println("Dictionary loaded. " + var5);
      String pciPath = "//home//vboxuser//Mustafar//project//pci.ids";
      System.out.println("Loading PCI dictionary...");
      var3 = System.currentTimeMillis();
      Dictionary.loadPCIDictionary(pciPath);
      var5 = System.currentTimeMillis();
      var5 -= var3;
      System.out.println("Dictionary loaded. " + var5);
      //new sysInfo();
      //cpuInfo var9 = new cpuInfo();
      //var9.read(0);
        
        loadCpuInfo();
        loadMemoryInfo();
        loadDiskInfo();
        /* load static hardware info once
        loadCpuInfo();
        loadPciInfo();
        loadUsbInfo();
        loadDiskInfo();
        loadMemoryInfo();
        */
        // warm up CPU info
        cpu.read(0);        
        loadPciInfo();
        showPCI();
        usbScan1.scanOnce();
        refreshUsbInfo();
        showUsbInfo();
                // start background sampler collecting real-time data
        SystemInfoWorker worker = new SystemInfoWorker();
        worker.start();

        Gui.showChart(computer, worker);
     
     
    }
}

