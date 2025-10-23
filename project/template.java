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

        // Sleep for 1 second and display the idle time percentage for
        // core 1. This assumes 10Hz so in one second we have 100
        cpu.read(1);
        System.out.println("core 1 idle=" + cpu.getIdleTime(1) + "%");
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

    public static void cpuUtil() {
        cpuInfo cpu = new cpuInfo();
        cpu.read(0);

        final int NUM_CORES = cpu.coresPerSocket() * cpu.socketCount();

        // Take the first sample
        int[] prevUser = new int[NUM_CORES];
        int[] prevSystem = new int[NUM_CORES];
        int[] prevIdle = new int[NUM_CORES];

        for (int core = 0; core < NUM_CORES; core++) {
            prevUser[core] = cpu.getUserTime(core);
            prevSystem[core] = cpu.getSystemTime(core);
            prevIdle[core] = cpu.getIdleTime(core);
        }

        // Wait one second for the next reading
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        cpu.read(0); // refresh stats

        long totalUsed = 0; // still use long for accumulation
        long totalTime = 0;

        System.out.println("Per-core utilization (percent):");

        for (int core = 0; core < NUM_CORES; core++) {
            int user = cpu.getUserTime(core);
            int system = cpu.getSystemTime(core);
            int idle = cpu.getIdleTime(core);

            int deltaUser = user - prevUser[core];
            int deltaSystem = system - prevSystem[core];
            int deltaIdle = idle - prevIdle[core];

            // skip this core if deltas look wrong (counter wrap or reset)
            if (deltaUser < 0 || deltaSystem < 0 || deltaIdle < 0) {
                System.err.printf("Skipping core %d due to negative delta (wraparound?)%n", core);
                continue;
            }

            int used = deltaUser + deltaSystem;
            int total = used + deltaIdle;

            if (total == 0)
                continue;

            double coreUtil = (double) used / total * 100.0;
            System.out.printf("  core %d: %.2f%%%n", core, coreUtil);

            totalUsed += used;
            totalTime += total;
        }

        if (totalTime == 0) {
            System.out.println("No valid CPU delta data available.");
            return;
        }

        double totalUtil = (double) totalUsed / totalTime * 100.0;
        System.out.printf("Total CPU Utilization: %.2f%%%n", totalUtil);
    }

    public static void main(String[] args) {
        System.loadLibrary("sysinfo");
        sysInfo info = new sysInfo();
        cpuInfo cpu = new cpuInfo();
        cpu.read(0);

        // showCPU();
        // showPCI();
        // showUSB();
        // showDisk();
        showMem();
        // SwingWorkerRealTime swingWorkerRealTime = new SwingWorkerRealTime();
        // swingWorkerRealTime.go();

        cpuUtil();

    }
}
