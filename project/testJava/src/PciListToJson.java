package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PciListToJson {
    public static void main(String[] args) throws Exception {
        File input = new File("C:\\Code\\Projects\\CS4421\\project\\testJava\\src\\pci.ids"); // your input file
        Scanner sc = new Scanner(input, "UTF-8");

        JsonArray vendors = new JsonArray();
        JsonObject currentVendor = null;
        JsonArray currentDevices = null;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            // Skip blank or comment lines
            if (line.trim().isEmpty() || line.trim().startsWith("#"))
                continue;

            // Vendor line: starts with no leading spaces
            if (!Character.isWhitespace(line.charAt(0))) {
                String[] parts = line.trim().split("\\s{2,}", 2); // split by 2+ spaces
                if (parts.length == 2) {
                    currentVendor = new JsonObject();
                    currentDevices = new JsonArray();
                    currentVendor.addProperty("vendor_id", parts[0]);
                    currentVendor.addProperty("vendor_name", parts[1]);
                    currentVendor.add("devices", currentDevices);
                    vendors.add(currentVendor);
                }
            }
            // Device line: starts with whitespace
            else if (currentVendor != null) {
                String[] parts = line.trim().split("\\s{2,}", 2);
                if (parts.length == 2) {
                    JsonObject dev = new JsonObject();
                    dev.addProperty("device_id", parts[0]);
                    dev.addProperty("device_name", parts[1]);
                    currentDevices.add(dev);
                }
            }
        }
        sc.close();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(vendors));
    }
}