import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SysInfoDashboard extends JPanel {

    private CardPanel usbCard;

    public SysInfoDashboard() {
        // Use BorderLayout for main panel
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel title = new JLabel("System Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topBar.add(title, BorderLayout.WEST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Cards panel - nested layout for different row widths
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Top row: 3 cards
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        JPanel cpuCard = createCard("CPU", template.getCPUInfo());
        JPanel memCard = createCard("Memory", template.getMemoryInfo());
        JPanel diskCard = createCard("Disk", template.getDiskInfo());
        cpuCard.setPreferredSize(new Dimension(200, 200));
        memCard.setPreferredSize(new Dimension(200, 200));
        diskCard.setPreferredSize(new Dimension(200, 200));
        topRow.add(cpuCard);
        topRow.add(memCard);
        topRow.add(diskCard);

        // Bottom row: 2 cards (wider - each 300px)
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        JPanel pciCard = createCard("PCI", template.getPCIInfo());
        usbCard = createCard("USB", "NO DATA");
        pciCard.setPreferredSize(new Dimension(800, 200));
        usbCard.setPreferredSize(new Dimension(800, 200));
        bottomRow.add(pciCard);
        bottomRow.add(usbCard);

        cardPanel.add(topRow);
        cardPanel.add(bottomRow);

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // --- TEST UPDATE FLOW ---
        // 1. Pause 2 seconds (freezes GUI)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        // 2. Show test update
        usbCard.updateCard("USB UPDATED - test refresh");
        System.out.println("[USB Test] usbCard shows UPDATED for testing.");

        // 3. Set listener for live USB changes
        template.usbScan1.setListener((newList, added, removed) -> {
            SwingUtilities.invokeLater(() -> {
                usbCard.updateCard(template.showUsbInfoJNI()); // live JNI scan
                System.out.println("[USB] Card updated with live JNI data.");
            });
        });

        // 4. Start USB scanning
        template.usbScan1.start();
    }

    private CardPanel createCard(String title, String description) {
        return new CardPanel(title, description);
    }
}
