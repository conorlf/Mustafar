/*
 *  Example class containing methods to read and display CPU, PCI and USB information
 *
 *  Copyright (computer) 2024 Mark Burkley (mark.burkley@ul.ie)
 */

import systeminfo.*;
import java.util.List;

import javax.swing.SwingUtilities;

import java.util.ArrayList;

public class template {
    public static Computer computer = new Computer();
    public static cpuInfo cpu = new cpuInfo();
    public static memInfo mem = new memInfo();
    public static diskInfo disk = new diskInfo();
    public static pciInfo pci = new pciInfo();
    public static UsbMonitor usbScan1 = new UsbMonitor();
    public static List<UsbDevice> usbDevices = new ArrayList<>();
    public static List<PciDevice> pciDevices = new ArrayList<>();

    // Hold reference to the displayed dashboard for updates
    public static SysInfoDashboard sysDash;

    public static void registerDashboard(SysInfoDashboard dash) {
        sysDash = dash;
    }

    public static void updateUsbCardTest(String text) {
        if (sysDash != null) {
            SysInfoDashboard.updateUsbCard(sysDash, text);
        }
    }

    public static void updateUsbCardLive() {
        if (sysDash != null) {
            SysInfoDashboard.updateUsbCard(sysDash, showUsbInfoJNI());
        }
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

    public static void autoRefreshUsbPanel(CardPanel panel, int intervalMs) {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    // 1. Refresh the USB device list
                    refreshUsbInfo();

                    // 2. Build the string from usbDevices
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%-8s %-8s %-8s  %-8s  %-30s %-30s%n",
                            "Bus", "Device", "Vendor", "Product", "Vendor Name", "Device Name"));
                    sb.append(String.format("%-8s %-8s %-8s  %-8s  %-30s %-30s%n",
                            "---", "------", "------", "-------", "-----------", "-----------"));

                    for (UsbDevice device : usbDevices) {
                        sb.append(String.format("%-8d %-8d 0x%04X  0x%04X  %-30s %-30s%n",
                                device.bus, device.device, device.vendorID, device.productID,
                                device.vendorName, device.deviceName));
                    }

                    // 3. Update the CardPanel safely on the EDT
                    SwingUtilities.invokeLater(() -> panel.updateCard(sb.toString()));

                    // 4. Sleep for the interval
                    Thread.sleep(intervalMs);
                } catch (InterruptedException ex) {
                    break; // stop the thread if interrupted
                }
            }
        });

        t.setDaemon(true); // allows JVM to exit even if thread is running
        t.start();
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

    public static boolean refreshUsbInfo() {
        List<UsbDevice> result = usbScan1.scanOnce();
        if (result != null) {
            usbDevices.clear();
            usbDevices.addAll(result);
            return true;
        }
        return false;
    }

    public static void showUsbInfo() {
        System.out.println("showUsbInfo(): Found " + usbDevices.size() + " devices.");
        for (UsbDevice device : usbDevices) {
            device.displayUsbInfo();
        }
    }

    public static String showUsbInfoJNI() {
        usbInfo usb = new usbInfo();
        usb.read();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s %-8s %-8s  %-8s  %-30s %-30s%n",
                "Bus", "Device", "Vendor", "Product", "Vendor Name", "Device Name"));
        sb.append(String.format("%-8s %-8s %-8s  %-8s  %-30s %-30s%n",
                "---", "------", "------", "-------", "-----------", "-----------"));

        for (int bus = 1; bus <= usb.busCount(); bus++) {
            for (int dev = 1; dev <= usb.deviceCount(bus); dev++) {
                int vendorId = usb.vendorID(bus, dev);
                int productId = usb.productID(bus, dev);
                String vendorName = Dictionary.getUSBVendorName(vendorId);
                String deviceName = Dictionary.getUSBDeviceName(vendorId, productId);
                sb.append(String.format("%-8d %-8d 0x%04X  0x%04X  %-30s %-30s%n",
                        bus, dev, vendorId, productId, vendorName, deviceName));
            }
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

    public static String getUSBInfo() {
        if (usbDevices.isEmpty())
            return "No USB devices";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s %-8s %-8s  %-8s  %-30s %-30s%n",
                "Bus", "Device", "Vendor", "Product", "Vendor Name", "Device Name"));
        sb.append(String.format("%-8s %-8s %-8s  %-8s  %-30s %-30s%n",
                "---", "------", "------", "-------", "-----------", "-----------"));
        for (UsbDevice device : usbDevices) {
            sb.append(String.format("%-8d %-8d 0x%04X  0x%04X  %-30s %-30s%n",
                    device.bus, device.device, device.vendorID, device.productID,
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
        if (refreshUsbInfo()) {
            showUsbInfo();
        }
        usbScan1.start();

        SystemInfoWorker worker = new SystemInfoWorker();
        worker.start();

        Gui.showChart(computer, worker);

        SysInfoDashboard dashboard = new SysInfoDashboard();
        registerDashboard(dashboard);
        // updateUsbCardLive();
        CardPanel usbPanel = new CardPanel("USB Devices", "Loading...");
        autoRefreshUsbPanel(usbPanel, 5000); // refresh every 5 seconds
    }
}
