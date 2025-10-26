/*
 *  Example class containing methods to read and display CPU, PCI and USB information
 *
 *  Copyright (c) 2024 Mark Burkley (mark.burkley@ul.ie)
 */

import systeminfo.*;
import java.util.List;
import java.util.ArrayList;

public class template 
{
    public static void showPCI()
    {}

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
                    " and product "+String.format("0x%04X", usb.productID(i,j)));
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



    public static void loadCpuInfo(Computer c) {
        cpuInfo cpu = new cpuInfo();
        cpu.read(0);
        // read all the static info - i.e. nujmber cores, model etc.populate it to our data structure
        Cpu myCpu = new Cpu(cpu.getModel(), cpu.socketCount(), cpu.coresPerSocket(), cpu.l1dCacheSize(), cpu.l1iCacheSize(), cpu.l2CacheSize(), cpu.l3CacheSize());
        
        // assuming all sockets are identical
        for (int j = 0; j < myCpu.coresPerSocket; j++){
            CpuCore core = new CpuCore(j);
            myCpu.cores.add(core);
        }
        c.cpu = myCpu;
    }

    public static void loadPciInfo(Computer c) {
        pciInfo pci = new pciInfo();
        pci.read();
        // Iterate through each bus
        for (int i = 0; i < pci.busCount(); i++) {
            PciBus myPciBus = new PciBus(i);
        
            // Iterate for up to 32 devices.  Not every device slot may be populated
            // so ensure at least one function before storing device information
            for (int j = 0; j < 32; j++) {
                if (pci.functionCount (i, j) > 0) {
                    PciDevice myPciDevice = new PciDevice(j);

                    // Iterate through up to 8 functions per device.
                    for (int k = 0; k < 8; k++) {
                        if (pci.functionPresent (i, j, k) > 0) {
                            PciFunction myPciFunction = new PciFunction (k, String.format("0x%04X", pci.vendorID(i,j,k)), String.format("0x%04X", pci.productID(i,j,k)));
                            myPciDevice.pciFunctions.add(myPciFunction);
                        }
                    }
                    myPciBus.pciDevices.add(myPciDevice);
                }
            }
            c.pciBuses.add(myPciBus);
        }
    }

    public static void loadUsbInfo(Computer c) {
        usbInfo usb = new usbInfo();
        usb.read();
        // Iterate through all of the USB buses
        for (int i = 1; i <= usb.busCount(); i++) {
            UsbBus myUsb = new UsbBus(i);
            // Iterate through all of the USB devices on the bus
            for (int j = 1; j <= usb.deviceCount(i); j++) {
                UsbDevice myUsbDevice = new UsbDevice(j, String.format("0x%04X", usb.vendorID(i,j)), String.format("0x%04X", usb.productID(i,j)));
                myUsb.usbDevices.add(myUsbDevice);
            }
            c.usbBuses.add(myUsb);
        }
    }

    public static void loadDiskInfo(Computer c) {
        diskInfo disk = new diskInfo();
        disk.read();
        for (int i = 0; i < disk.diskCount(); i++) {
            Disk myDisk = new Disk(disk.getName(i), disk.getTotal(i), disk.getUsed(i));
            c.disks.add(myDisk);
        }
    }

    public static void loadMemoryInfo(Computer c) {
        memInfo mem = new memInfo();
        mem.read();
        Memory myMemory = new Memory(mem.getTotal(), mem.getUsed());
        c.memory = myMemory;
    }

    public static void main(String[] args)
    {
        Computer c = new Computer();
        System.loadLibrary("sysinfo");
        sysInfo info = new sysInfo();

        loadCpuInfo(c);
        loadPciInfo(c);
        loadUsbInfo(c);
        loadDiskInfo(c);
        loadMemoryInfo(c);

        //c.dumpToConsole();
        
        cpuInfo cpu = new cpuInfo();
        cpu.read(0);
        for (int i = 1; i <= 15; i++) {
            sampleCpuUsage(c, cpu, i);
        }

        Gui.showChart(c);
    }
 

    public static void sampleCpuUsage (Computer c, cpuInfo cpu, int secondsSinceStart) {
        cpu.read(1);
        for (int i = 0; i < c.cpu.cores.size(); i++) {
            int idleTime = cpu.getIdleTime(i);
            int systemTime = cpu.getSystemTime(i);
            int userTime = cpu.getUserTime(i);
            int totalTime = idleTime + systemTime + userTime;

            double idlePercent = ((double) idleTime / totalTime) * 100;
            double systemPercent = ((double) systemTime / totalTime) * 100;
            double userPercent = ((double) userTime / totalTime) * 100;

            CpuTimings myCpuTimings = new CpuTimings(secondsSinceStart, idlePercent, userPercent, systemPercent);
            c.cpu.cores.get(i).cpuTimings.add(myCpuTimings);
        }
    }
    public static void old_main()
    {
        System.loadLibrary("sysinfo");
        sysInfo info = new sysInfo();
        cpuInfo cpu = new cpuInfo();
        cpu.read(0);

        showCPU();
        showPCI();
        showUSB();
        showDisk();
        showMem();
    }
}

