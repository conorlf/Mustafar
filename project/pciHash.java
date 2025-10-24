import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.HashMap;

public class pciHash {
    public static void Hash() {
        File IDs = new File("project/pci.ids");
        HashMap<String, String> PCIlookup = new HashMap<>();

        // try-with-resources: Scanner will be closed automatically
        try (Scanner sc = new Scanner(IDs)) {
            while (sc.hasNextLine()) {
                String data = sc.nextLine();
                if (data.startsWith("#") || data.trim().isEmpty() || data.startsWith("\t")
                        || !data.substring(0, 4).equals("8086")) {
                    continue; // Skip comments and empty lines
                }
                System.out.println(data);
                if (data.substring(0, 4).equals("8086"))
                    while (true) {
                        data = sc.nextLine();
                        if (!data.startsWith("\t") && !data.startsWith("#")) {
                            break;
                        }
                        System.out.println(data);
                    }
                System.out.println(data);
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Hash();
    }
}
