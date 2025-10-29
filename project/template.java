/*
 *  Example class containing methods to read and display CPU, PCI and USB information
 *
 *  Copyright (computer) 2024 Mark Burkley (mark.burkley@ul.ie)
 */

import systeminfo.*;
import java.util.List;
import java.util.ArrayList;

public class template {
    public static Computer computer = new Computer();
    public static cpuInfo cpu = new cpuInfo();
    public static memInfo mem = new memInfo();
    public static diskInfo disk = new diskInfo();
    public static pciInfo pci = new pciInfo();
    public static usbInfo usb = new usbInfo();

    public static List<UsbDevice> usbDevices = new ArrayList<>();
    public static List<PciDevice> pciDevices = new ArrayList<>();

    // Hold reference to the displayed dashboard for updates
    public static SysInfoDashboard sysDash;

    public static void registerDashboard(SysInfoDashboard dash) {
        sysDash = dash;
    }

    public static void loadCpuInfo() {
        cpu.read(0);
        Cpu myCpu = new Cpu(cpu.getModel(), cpu.socketCount(), cpu.coresPerSocket(), cpu.l1dCacheSize(),
                cpu.l1iCacheSize(), cpu.l2CacheSize(), cpu.l3CacheSize());
        for (int i = 0; i < myCpu.socketCount; i++) {
            for (int j = 0; j < myCpu.coresPerSocket; j++) {
                CpuCore core = new CpuCore(j);
                myCpu.cores.add(core);
            }
        }
        computer.cpu = myCpu;
    }

    public static void loadPciInfo() {
        pci.read();
        for (int bus = 0; bus < pci.busCount(); bus++) {
            for (int dev = 0; dev < pci.deviceCount(bus); dev++) {
                for (int function = 0; function < 8; function++) {
                    if (pci.functionPresent(bus, dev, function) > 0) {
                        PciDevice device = new PciDevice(bus, dev, function, pci.vendorID(bus, dev, function),
                                pci.productID(bus, dev, function));
                        pciDevices.add(device);
                    }
                }
            }
        }
    }

    public static void showPCI() {
        System.out.println("\nThis machine has " + pci.busCount() + " PCI buses ");
        for (int i = 0; i < pci.busCount(); i++) {
            System.out.println("Bus " + i + " has " + pci.deviceCount(i) + " devices");
        }
        if (pciDevices == null || pciDevices.isEmpty()) {
            System.out.println("No PCI devices found.");
            return;
        }
        System.out.println("PCI Devices Detected: " + pciDevices.size());
        System.out.printf("%-8s %-8s %-8s %-12s %-12s %-30s %-30s%n", "Bus", "Device", "Func", "Vendor ID",
                "Product ID", "Vendor Name", "Device Name");
        for (PciDevice device : pciDevices) {
            device.displayPciInfo();
        }
    }

    // SIMPLIFIED: Just read USB info directly
    public static String getUSBInfo() {
        usb.read();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s %-8s %-8s  %-8s  %-30s %-30s%n",
                "Bus", "Device", "Vendor", "Product", "Vendor Name", "Device Name"));
        sb.append(String.format("%-8s %-8s %-8s  %-8s  %-30s %-30s%n",
                "---", "------", "------", "-------", "-----------", "-----------"));

        int deviceCount = 0;
        for (int bus = 1; bus <= usb.busCount(); bus++) {
            for (int dev = 1; dev <= usb.deviceCount(bus); dev++) {
                int vendorId = usb.vendorID(bus, dev);
                int productId = usb.productID(bus, dev);
                String vendorName = Dictionary.getUSBVendorName(vendorId);
                String deviceName = Dictionary.getUSBDeviceName(vendorId, productId);
                sb.append(String.format("%-8d %-8d 0x%04X  0x%04X  %-30s %-30s%n",
                        bus, dev, vendorId, productId, vendorName, deviceName));
                deviceCount++;
            }
        }

        if (deviceCount == 0) {
            sb.append("No USB devices detected");
        }

        return sb.toString();
    }

    // CPU, Memory, Disk, PCI helper methods
    public static String getCPUInfo() {
        if (computer.cpu == null)
            return "CPU not loaded";
        return String.format("Model: %s\nCores: %d", computer.cpu.model, computer.cpu.cores.size());
    }

    public static String getMemoryInfo() {
        if (computer.memory == null)
            return "Memory not loaded";
        return String.format("Total: %d KB\nUsed: %d KB\nUsage: %.1f%%",
                computer.memory.totalMemory, computer.memory.usedMemory, computer.memory.usagePercent);
    }

    public static String getDiskInfo() {
        diskInfo disk = new diskInfo();
        disk.read();
        StringBuilder sb = new StringBuilder();
        sb.append("Disks:\n");
        for (int i = 0; i < disk.diskCount(); i++) {
            sb.append(String.format("Disk %d: %s\n", i, disk.getName(i)));
            sb.append(String.format("%d KB used / %d KB total (%.1f%%)\n",
                    disk.getUsed(i), disk.getTotal(i),
                    ((float) ((disk.getTotal(i) > 0) ? (disk.getUsed(i) * 100.0) / disk.getTotal(i) : 0.0))));
        }
        sb.append("Total disks \n");
        sb.append(disk.diskCount());
        return sb.toString().trim();
    }

    public static String getPCIInfo() {
        if (pciDevices.isEmpty())
            return "No PCI devices";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s %-8s %-8s %-8s  %-8s  %-30s %-30s%n",
                "Bus", "Device", "Func", "Vendor", "Product", "Vendor Name", "Device Name"));
        sb.append(String.format("%-8s %-8s %-8s %-8s  %-8s  %-30s %-30s%n",
                "---", "------", "----", "------", "-------", "-----------", "-----------"));
        for (PciDevice device : pciDevices) {
            sb.append(String.format("%-8d %-8d %-8d 0x%04X  0x%04X  %-30s %-30s%n",
                    device.bus, device.device, device.function, device.vendorID, device.productID,
                    device.vendorName, device.deviceName));
        }
        return sb.toString();
    }

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

    // Simplified versions that work with manual refresh
    public static boolean refreshUsbInfo() {
        // Can still read USB if needed, but won't trigger GUI updates
        return true;
    }

    public static void showUsbInfo() {
        // Just print to console for debugging
        System.out.println("Current USB devices:");
        System.out.println(getUSBInfo());
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

    public static void main(String[] args) {
        System.loadLibrary("sysinfo");

        String usbPath = "usb.ids";
        System.out.println("Loading USB dictionary...");
        long start = System.currentTimeMillis();
        Dictionary.loadUSBDictionary(usbPath);
        System.out.println("Dictionary loaded in " + (System.currentTimeMillis() - start) + " ms.");

        String pciPath = "pci.ids";
        System.out.println("Loading PCI dictionary...");
        start = System.currentTimeMillis();
        Dictionary.loadPCIDictionary(pciPath);
        System.out.println("Dictionary loaded in " + (System.currentTimeMillis() - start) + " ms.");

        loadCpuInfo();
        loadMemoryInfo();
        loadDiskInfo();
        cpu.read(0);
        loadPciInfo();
        showPCI();

        SystemInfoWorker worker = new SystemInfoWorker();
        worker.start();

        Gui.showChart(computer, worker);

        // The dashboard will handle its own button-based refresh
    }
}