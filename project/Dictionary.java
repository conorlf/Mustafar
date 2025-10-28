import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Dictionary {
    
    private static Map<Integer, String> pciVendorMap = new HashMap<>();
    private static Map<String, String> pciDeviceMap = new HashMap<>();

    private static Map<Integer, String> usbVendorMap = new HashMap<>();
    private static Map<String, String> usbDeviceMap = new HashMap<>();
   
    //Helper loader (shared)
    private static void loadIdsFile(String path, Map<Integer, String> vendorMap, Map<String, String> deviceMap) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int currentVendor = -1;

            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#"))
                continue;

                // Device lines start with a tab
                if (line.startsWith("\t") && currentVendor != -1) {
                    String[] parts = line.trim().split("\\s+", 2);
                    if (parts.length == 2) {
                        String key = String.format("%04x:%04x",currentVendor, Integer.parseInt(parts[0], 16));
                        deviceMap.put(key, parts[1]);
                    }
                } else {
                    // Vendor line
                    String[] parts = line.trim().split("\\s+", 2);//split the line where there is multiple whitespaces
                    if (parts.length == 2) {
                     if (parts[0].matches(".*[A-Z].*")) continue;
                     else{
                        currentVendor = Integer.parseInt(parts[0], 16);//take the id part of the line and convert it to an int and its currently base 16 so change to decimal
                        vendorMap.put(currentVendor, parts[1]);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load " + path + ": " + e.getMessage());
        }
    }

    //PCI specific wrappers
    public static void loadPCIDictionary(String path) {
        
        loadIdsFile(path, pciVendorMap, pciDeviceMap);
    }

    public static String getPCIVendorName(int vendorId) {
        return pciVendorMap.getOrDefault(vendorId, "Unknown Vendor");
    }

    public static String getPCIDeviceName(int vendorId, int deviceId) {
        String key = String.format("%04x:%04x", vendorId, deviceId).toLowerCase();
        return pciDeviceMap.getOrDefault(key, "Unknown Device");
    }

    // USB specific wrappers
    public static void loadUSBDictionary(String path) {
        loadIdsFile(path, usbVendorMap, usbDeviceMap);
    }

    public static String getUSBVendorName(int vendorId) {
        return usbVendorMap.getOrDefault(vendorId, "Unknown Vendor");
    }

    public static String getUSBDeviceName(int vendorId, int deviceId) {
        String key = String.format("%04x:%04x", vendorId, deviceId).toLowerCase();
        return usbDeviceMap.getOrDefault(key, "Unknown Device");
    }
}