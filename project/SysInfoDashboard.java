import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SysInfoDashboard extends JPanel {

    private CardPanel usbCard;
    private JButton refreshUsbButton;

    public SysInfoDashboard() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        // Top bar with refresh button
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
        usbCard = createCard("USB", template.getUSBInfo());

        // Add refresh button specifically for USB
        JButton usbRefreshButton = new JButton("Refresh USB");
        usbRefreshButton.addActionListener(e -> refreshUsbCard());

        JPanel usbPanelWithButton = new JPanel(new BorderLayout());
        usbPanelWithButton.add(usbCard, BorderLayout.CENTER);
        usbPanelWithButton.add(usbRefreshButton, BorderLayout.SOUTH);

        pciCard.setPreferredSize(new Dimension(800, 200));
        usbPanelWithButton.setPreferredSize(new Dimension(800, 250));
        bottomRow.add(pciCard);
        bottomRow.add(usbPanelWithButton);

        cardPanel.add(topRow);
        cardPanel.add(bottomRow);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // Register this dashboard in template
        template.registerDashboard(this);
    }

    private CardPanel createCard(String title, String description) {
        return new CardPanel(title, description);
    }

    /** Simple USB refresh - just get fresh data and update the card */
    public void refreshUsbCard() {
        String newUsbInfo = template.getUSBInfo();
        usbCard.updateCard(newUsbInfo);
        System.out.println(template.getUSBInfo());
    }

    /** Refresh all system information cards */

}