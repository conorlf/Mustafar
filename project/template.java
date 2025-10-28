/*
 * Simplified version of the template class
 * showing CPU, RAM, Disk, PCI, and USB info in clean formats
 */

import javax.swing.*;

public class template {

    public static void showCPU() {
        cpuInfo cpu = new cpuInfo();
        cpu.read(0);

        System.out.println("=== CPU Information ===");
        System.out.println("Model: " + cpu.getModel());
        System.out.println("Sockets: " + cpu.socketCount());
        System.out.println("Cores per socket: " + cpu.coresPerSocket());
        System.out.println("Cache Sizes:");
        System.out.println("  L1d: " + cpu.l1dCacheSize());
        System.out.println("  L1i: " + cpu.l1iCacheSize());
        System.out.println("  L2 : " + cpu.l2CacheSize());
        System.out.println("  L3 : " + cpu.l3CacheSize());
    }

    public static void showMem() {
        memInfo mem = new memInfo();
        mem.read();

        System.out.println("=== Memory Information ===");
        System.out.println("Total Memory: " + mem.getTotal());
        System.out.println("Used Memory : " + mem.getUsed());
    }

    public static void showDisk() {
        diskInfo disk = new diskInfo();
        disk.read();

        System.out.println("=== Disk Information ===");
        for (int i = 0; i < disk.diskCount(); i++) {
            System.out.println("Disk: " + disk.getName(i));
            System.out.println("  Total blocks: " + disk.getTotal(i));
            System.out.println("  Used blocks : " + disk.getUsed(i));
            System.out.println();
        }
    }

    public static void showPCI() {
        pciInfo pci = new pciInfo();
        pci.read();

        System.out.println("=== PCI Devices ===");

        for (int i = 0; i < pci.busCount(); i++) {
            for (int j = 0; j < 32; j++) {
                for (int k = 0; k < 8; k++) {
                    if (pci.functionPresent(i, j, k) > 0) {
                        int vendorId = pci.vendorID(i, j, k);
                        int productId = pci.productID(i, j, k);

                        String vendorName = Dictionary.getPCIVendorName(vendorId);
                        String deviceName = Dictionary.getPCIDeviceName(vendorId, productId);

                        if (vendorName == null || vendorName.isEmpty())
                            vendorName = "Unknown Vendor (0x" + String.format("%04X", vendorId) + ")";
                        if (deviceName == null || deviceName.isEmpty())
                            deviceName = "Unknown Device (0x" + String.format("%04X", productId) + ")";

                        System.out.println(vendorName + " : " + deviceName);
                    }
                }
            }
        }
    }

    // Return USB devices as HTML with <br> between lines
    public static String showUSBString() {
        usbInfo usb = new usbInfo();
        try {
            usb.read();
        } catch (Exception e) {
            return "<html><body><h3>USB Devices</h3><p>Error reading USB: " + e.getMessage() + "</p></body></html>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h3>USB Devices</h3>");

        for (int i = 1; i <= usb.busCount(); i++) {
            for (int j = 1; j <= usb.deviceCount(i); j++) {
                int vendorId = usb.vendorID(i, j);
                int productId = usb.productID(i, j);
                if (vendorId == 0 && productId == 0)
                    continue;

                String vendorName = Dictionary.getUSBVendorName(vendorId);
                String deviceName = Dictionary.getUSBDeviceName(vendorId, productId);

                if (vendorName == null || vendorName.isEmpty())
                    vendorName = "Unknown Vendor (0x" + String.format("%04X", vendorId) + ")";
                if (deviceName == null || deviceName.isEmpty())
                    deviceName = "Unknown Device (0x" + String.format("%04X", productId) + ")";

                sb.append(vendorName)
                        .append(" : ")
                        .append(deviceName)
                        .append("<br>");
            }
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    // Return CPU information as HTML
    public static String showCPUString() {
        cpuInfo cpu = new cpuInfo();
        cpu.read(0);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h3>CPU Information</h3>");

        sb.append("Model: ").append(cpu.getModel()).append("<br>");
        sb.append("Sockets: ").append(cpu.socketCount()).append("<br>");
        sb.append("Cores per socket: ").append(cpu.coresPerSocket()).append("<br>");
        sb.append("Cache Sizes:<br>");
        sb.append("  L1d: ").append(cpu.l1dCacheSize()).append("<br>");
        sb.append("  L1i: ").append(cpu.l1iCacheSize()).append("<br>");
        sb.append("  L2 : ").append(cpu.l2CacheSize()).append("<br>");
        sb.append("  L3 : ").append(cpu.l3CacheSize()).append("<br>");

        sb.append("</body></html>");
        return sb.toString();
    }

    // Return PCI devices as HTML with <br> between lines
    public static String showPCIString() {
        pciInfo pci = new pciInfo();
        pci.read();

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h3>PCI Devices</h3>");

        for (int i = 0; i < pci.busCount(); i++) {
            for (int j = 0; j < 32; j++) {
                for (int k = 0; k < 8; k++) {
                    if (pci.functionPresent(i, j, k) > 0) {
                        int vendorId = pci.vendorID(i, j, k);
                        int productId = pci.productID(i, j, k);

                        String vendorName = Dictionary.getPCIVendorName(vendorId);
                        String deviceName = Dictionary.getPCIDeviceName(vendorId, productId);

                        if (vendorName == null || vendorName.isEmpty())
                            vendorName = "Unknown Vendor (0x" + String.format("%04X", vendorId) + ")";
                        if (deviceName == null || deviceName.isEmpty())
                            deviceName = "Unknown Device (0x" + String.format("%04X", productId) + ")";

                        sb.append(vendorName)
                                .append(" : ")
                                .append(deviceName)
                                .append("<br>");
                    }
                }
            }
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    // Print USB devices to console
    public static void showUSB() {
        usbInfo usb = new usbInfo();
        usb.read();

        System.out.println("=== USB Devices ===");

        for (int i = 1; i <= usb.busCount(); i++) {
            for (int j = 1; j <= usb.deviceCount(i); j++) {
                int vendorId = usb.vendorID(i, j);
                int productId = usb.productID(i, j);

                if (vendorId == 0 && productId == 0)
                    continue;

                String vendorName = Dictionary.getUSBVendorName(vendorId);
                String deviceName = Dictionary.getUSBDeviceName(vendorId, productId);

                if (vendorName == null || vendorName.isEmpty())
                    vendorName = "Unknown Vendor (0x" + String.format("%04X", vendorId) + ")";
                if (deviceName == null || deviceName.isEmpty())
                    deviceName = "Unknown Device (0x" + String.format("%04X", productId) + ")";

                System.out.println(vendorName + " : " + deviceName);
            }
        }
    }

    public static void main(String[] args) {
        System.loadLibrary("sysinfo");
        long startTime = System.currentTimeMillis();
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

        long endTime = System.currentTimeMillis();
        System.out.println("Dictionaries loaded in " + (endTime - startTime) + " ms");

        SwingUtilities.invokeLater(() -> {
            SysInfoDashboard dashboard = new SysInfoDashboard();

            UsbMonitor monitor = new UsbMonitor(3000);
            monitor.setNotificationListener(message -> {
                System.out.println("USB Notification: " + message);
                dashboard.showNotification(message, 3000);
            });
            monitor.start();

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
