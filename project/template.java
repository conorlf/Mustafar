
/*
 *  Example class containing methods to read and display CPU, PCI and USB information
 *
 *  Copyright (c) 2024 Mark Burkley (mark.burkley@ul.ie)
 */
//import java.util.Scanner;
import javax.swing.*;
// import javax.swing.border.EmptyBorder;
// import java.awt.*;
// import java.io.ByteArrayOutputStream;
// import java.io.PrintStream;

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
                                    " and product " + String.format("0x%04X", pci.productID(i, j, k))
                                    + " and vendor name" + Dictionary.getPCIVendorName(pci.vendorID(i, j, k))
                                    + " and device name "
                                    + Dictionary.getPCIDeviceName(pci.vendorID(i, j, k), pci.productID(i, j, k)));
                        }
                    }
                }
            }
        }
    }

    public static void showUSB() {
        System.out.println("\n=== Current USB Devices ===");

        usbInfo usb = new usbInfo();
        usb.read();

        int totalValidDevices = 0;

        System.out.println("This machine has " + usb.busCount() + " USB buses");

        // Iterate through all of the USB buses
        for (int i = 1; i <= usb.busCount(); i++) {
            int deviceCount = usb.deviceCount(i);
            int validDevicesThisBus = 0;

            // First count valid devices
            for (int j = 1; j <= deviceCount; j++) {
                int vendorId = usb.vendorID(i, j);
                int productId = usb.productID(i, j);
                if (vendorId != 0 || productId != 0) {
                    validDevicesThisBus++;
                }
            }

            System.out.println(
                    "Bus " + i + " has " + deviceCount + " device slots (" + validDevicesThisBus + " valid devices)");

            // Iterate through all of the USB devices on the bus
            for (int j = 1; j <= deviceCount; j++) {
                try {
                    int vendorId = usb.vendorID(i, j);
                    int productId = usb.productID(i, j);

                    // Skip invalid/ghost devices
                    if (vendorId == 0 && productId == 0) {
                        continue;
                    }

                    String vendorName = Dictionary.getUSBVendorName(vendorId);
                    if (vendorName == null || vendorName.equals("Unknown Vendor")) {
                        vendorName = "Unknown Vendor (0x" + String.format("%04X", vendorId) + ")";
                    }

                    String deviceName = Dictionary.getUSBDeviceName(vendorId, productId);
                    if (deviceName == null || deviceName.equals("Unknown Device")) {
                        deviceName = "Unknown Device (0x" + String.format("%04X", productId) + ")";
                    }

                    System.out.println("  Bus " + i + " Device " + j +
                            ": " + vendorName + " - " + deviceName +
                            " [Vendor: 0x" + String.format("%04X", vendorId) +
                            ", Product: 0x" + String.format("%04X", productId) + "]");

                    totalValidDevices++;

                } catch (Exception e) {
                    System.out.println("  Bus " + i + " Device " + j + " - Error reading device info");
                }
            }
        }

        System.out.println("Total valid USB devices: " + totalValidDevices);
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

    public static void main(String[] args) {
        // Load libraries and dictionaries
        System.loadLibrary("sysinfo");

        // Load dictionaries with error handling
        try {
            Dictionary.loadUSBDictionary("usb.ids");
            System.out.println("USB dictionary loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load USB dictionary: " + e.getMessage());
        }

        try {
            Dictionary.loadPCIDictionary("pci.ids");
            System.out.println("PCI dictionary loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load PCI dictionary: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            SysInfoDashboard dashboard = new SysInfoDashboard();

            // Use a longer interval to be more reliable
            UsbMonitor monitor = new UsbMonitor(3000);
            monitor.setNotificationListener(message -> {
                System.out.println("USB Notification: " + message);
                dashboard.showNotification(message, 3000);
            });

            monitor.start();

            // Stop the monitor when the window closes
            dashboard.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.out.println("Closing USB monitor...");
                    monitor.close();
                }
            });

            dashboard.setVisible(true);
            System.out.println("Dashboard started with USB monitoring");
        });
    }
}