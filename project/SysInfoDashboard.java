import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SysInfoDashboard extends JPanel {

    CardPanel usbCard;

    public SysInfoDashboard() {
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

        // Cards panel
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Top row: CPU, Memory, Disk
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

        // Bottom row: PCI, USB
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

        // Register this dashboard in template
        template.registerDashboard(this);

        // Test update: show "Hello World" for 2 seconds
        refreshUsbCardTest("Hello World", 2000);

        // Live USB updates
        template.usbScan1
                .setListener((newList, added, removed) -> SwingUtilities.invokeLater(template::updateUsbCardLive));

        // Start USB monitor
        template.usbScan1.start();
    }

    private CardPanel createCard(String title, String description) {
        return new CardPanel(title, description);
    }

    /** Replaces the usbCard with a fresh CardPanel to force redraw */
    public void refreshUsbCard(String text) {
        SwingUtilities.invokeLater(() -> {
            JPanel parent = (JPanel) usbCard.getParent();
            int index = -1;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                if (parent.getComponent(i) == usbCard) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                parent.remove(usbCard);
                usbCard = new CardPanel("USB", text);
                usbCard.setPreferredSize(new Dimension(800, 200));
                parent.add(usbCard, index);
                parent.revalidate();
                parent.repaint();
            }
        });
    }

    /** Helper: show a test text for a delay, then revert to live */
    private void refreshUsbCardTest(String text, int delayMs) {
        refreshUsbCard(text);
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ignored) {
            }
            SwingUtilities.invokeLater(template::updateUsbCardLive);
        }).start();
    }

    /** Static helper to update a dashboard's usbCard safely */
    public static void updateUsbCard(SysInfoDashboard dashboard, String text) {
        dashboard.refreshUsbCard(text);
    }
}
