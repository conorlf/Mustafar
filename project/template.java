/*
 *  Example class containing methods to read and display CPU, PCI and USB information
 *
 *  Copyright (computer) 2024 Mark Burkley (mark.burkley@ul.ie)
 */

import systeminfo.*;
import java.util.List;
import java.util.ArrayList;

public class template 
{
    public static Computer computer = new Computer();
    public static cpuInfo cpu = new cpuInfo();
    public static memInfo mem = new memInfo();
    public static diskInfo disk = new diskInfo();

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

    public static void loadPciInfo() {
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
            computer.pciBuses.add(myPciBus);
        }
    }

    public static void loadUsbInfo() {
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
            computer.usbBuses.add(myUsb);
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
        System.out.println(disk.diskCount());
        System.out.println(computer.disks.size());
        for (int i = 0; i < disk.diskCount(); i++) {
            System.out.println(i);
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

        // load static hardware info once
        loadCpuInfo();
        loadPciInfo();
        loadUsbInfo();
        loadDiskInfo();
        loadMemoryInfo();

        // warm up CPU info
        cpu.read(0);        

        // start background sampler collecting real-time data
        SystemInfoWorker worker = new SystemInfoWorker();
        worker.start();

        Gui.showChart(computer, worker);
    }
}

