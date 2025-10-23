
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class PCIidReader {

    public static void readPCIIDs() {
        File PCIsFile = new File("project/PCIs.txt");
        try (Scanner scanner = new Scanner(PCIsFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
