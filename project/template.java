/*
 *  Example class containing methods to read and display CPU, PCI and USB information
 *
 *  Copyright (c) 2024 Mark Burkley (mark.burkley@ul.ie)
 */

public class template {
    public static void showPCI() {
        pciInfo pci = new pciInfo();
        pci.read();

        System.out.println("\nThis machine has " +
                pci.busCount() + " PCI buses ");

        // Iterate through each bus
        for (int i = 0; i < pci.busCount(); i++) {
            System.out.println("Bus " + i + " has " +
                    pci.deviceCount(i) + " devices");

            // Iterate for up to 32 devices. Not every device slot may be populated
            // so ensure at least one function before printing device information
            for (int j = 0; j < 32; j++) {
                if (pci.functionCount(i, j) > 0) {
                    System.out.println("Bus " + i + " device " + j + " has " +
                            pci.functionCount(i, j) + " functions");

                    // Iterate through up to 8 functions per device.
                    for (int k = 0; k < 8; k++) {
                        if (pci.functionPresent(i, j, k) > 0) {
                            System.out.println("Bus " + i + " device " + j + " function " + k +
                                    " has vendor " + String.format("0x%04X", pci.vendorID(i, j, k)) +
                                    " and product " + String.format("0x%04X", pci.productID(i, j, k)));
                        }
                    }
                }
            }
        }
    }

    public static void showUSB() {
        usbInfo usb = new usbInfo();
        usb.read();
        System.out.println("\nThis machine has " +
                usb.busCount() + " USB buses ");

        // Iterate through all of the USB buses
        for (int i = 1; i <= usb.busCount(); i++) {
            System.out.println("Bus " + i + " has " +
                    usb.deviceCount(i) + " devices");

            // Iterate through all of the USB devices on the bus
            for (int j = 1; j <= usb.deviceCount(i); j++) {
                System.out.println("Bus " + i + " device " + j +
                        " has vendor " + String.format("0x%04X", usb.vendorID(i, j)) +
                        " and product " + String.format("0x%04X", usb.productID(i, j)));
            }
        }
    }

    public static void showCPU() {
        cpuInfo cpu = new cpuInfo();

        // First read establishes baseline
        cpu.read(0);

        // Show CPU model, CPU sockets and cores per socket
        System.out.println("CPU " + cpu.getModel() + " has " +
                cpu.socketCount() + " sockets each with " +
                cpu.coresPerSocket() + " cores");

        // Show sizes of L1,L2 and L3 cache
        System.out.println("l1d=" + cpu.l1dCacheSize() +
                ", l1i=" + cpu.l1iCacheSize() +
                ", l2=" + cpu.l2CacheSize() +
                ", l3=" + cpu.l3CacheSize());

        // Second read after 1 second gives you meaningful deltas
        cpu.read(1);

        // Now calculate percentage properly
        int user = cpu.getUserTime(1);
        int system = cpu.getSystemTime(1);
        int idle = cpu.getIdleTime(1);
        int total = user + system + idle;

        if (total > 0) {
            double idlePercent = (idle * 100.0) / total;
            System.out.println("core 1 idle=" + idlePercent + "% (" +
                    idle + " jiffies out of " + total + ")");
        } else {
            System.out.println("core 1 idle=0% (no activity measured)");
        }
    }

    public static void showDisk() {
        diskInfo disk = new diskInfo();
        disk.read();

        // Iterate through all of the disks
        for (int i = 0; i < disk.diskCount(); i++) {
            System.out.println("disk " + disk.getName(i) + " has " +
                    disk.getTotal(i) + " blocks, of which " +
                    disk.getUsed(i) + " are used");
        }
    }

    public static void showMem() {
        memInfo mem = new memInfo();
        mem.read();

        System.out.println("There is " + mem.getTotal() + " memory of which " +
                mem.getUsed() + " is used");
    }

    public static double getCoreUtilization(int core) {
        cpuInfo cpu = new cpuInfo();

        // First read to establish baseline
        cpu.read(0);
        // Second read to get deltas
        cpu.read(1);

        int cpuCount = cpu.socketCount() * cpu.coresPerSocket();

        // Get the time deltas (these are the values you mentioned)
        int userTime = cpu.getUserTime(core);
        int systemTime = cpu.getSystemTime(1);
        int idleTime = cpu.getIdleTime(1);

        // Calculate total CPU time
        int totalTime = userTime + systemTime + idleTime;

        // Avoid division by zero
        if (totalTime <= 0) {
            return 0.0;
        }

        // Calculate utilization: (non-idle time) / total time
        int nonIdleTime = userTime + systemTime;
        double utilization = ((double) nonIdleTime / totalTime) * 100.0;

        return 100 - idleTime;
    }

    public static void main(String[] args) {
        System.loadLibrary("sysinfo");
        sysInfo info = new sysInfo();
        cpuInfo cpu = new cpuInfo();
        cpu.read(0);
        while (true)
            System.out.println("Core 1 Utilization: " + getCoreUtilization(1) + "%");
        // showCPU();
        // showPCI();
        // showUSB();
        // showDisk();
        // showMem();
        // SwingWorkerRealTime swingWorkerRealTime = new SwingWorkerRealTime();
        // swingWorkerRealTime.go();

    }
}
