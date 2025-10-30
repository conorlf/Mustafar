import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SysInfoDashboard extends JPanel {

    private JTextArea cpuContent;
    private JTextArea memContent;
    private JTextArea diskContent;
    private JTextArea usbContent;

    public SysInfoDashboard() {
        // Use BorderLayout for main panel
        setLayout(new BorderLayout());

        // Main panel for cards
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
        JPanel cpuCard = createCpuCard();
        JPanel memCard = createMemCard();
        JPanel diskCard = createDiskCard();
        cpuCard.setPreferredSize(new Dimension(400, 300));
        memCard.setPreferredSize(new Dimension(400, 300));
        diskCard.setPreferredSize(new Dimension(400, 300));
        topRow.add(cpuCard);
        topRow.add(memCard);
        topRow.add(diskCard);
        
        // Bottom row: 2 cards (wider - each 300px)
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        JPanel pciCard = createCard("PCI", template.getPCIInfo());
        JPanel usbCard = createUsbCard();
        pciCard.setPreferredSize(new Dimension(800, 400));
        usbCard.setPreferredSize(new Dimension(800, 400));
        bottomRow.add(pciCard);
        bottomRow.add(usbCard);
        
        cardPanel.add(topRow);
        cardPanel.add(bottomRow);

        mainPanel.add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, String description) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(titleLabel, BorderLayout.NORTH);

        JTextArea content = new JTextArea(description);
        content.setEditable(false);
        content.setFont(new Font("Monospaced", Font.PLAIN, 11));
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setOpaque(false);
        
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createCpuCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = new JLabel("CPU", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(titleLabel, BorderLayout.NORTH);

        cpuContent = new JTextArea(template.getCPUInfo());
        cpuContent.setEditable(false);
        cpuContent.setFont(new Font("Monospaced", Font.PLAIN, 11));
        cpuContent.setLineWrap(true);
        cpuContent.setWrapStyleWord(true);
        cpuContent.setOpaque(false);

        JScrollPane scroll = new JScrollPane(cpuContent);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createMemCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = new JLabel("Memory", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(titleLabel, BorderLayout.NORTH);

        memContent = new JTextArea(template.getMemoryInfo());
        memContent.setEditable(false);
        memContent.setFont(new Font("Monospaced", Font.PLAIN, 11));
        memContent.setLineWrap(true);
        memContent.setWrapStyleWord(true);
        memContent.setOpaque(false);

        JScrollPane scroll = new JScrollPane(memContent);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDiskCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = new JLabel("Disk", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(titleLabel, BorderLayout.NORTH);

        diskContent = new JTextArea(template.getDiskInfo());
        diskContent.setEditable(false);
        diskContent.setFont(new Font("Monospaced", Font.PLAIN, 11));
        diskContent.setLineWrap(true);
        diskContent.setWrapStyleWord(true);
        diskContent.setOpaque(false);

        JScrollPane scroll = new JScrollPane(diskContent);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createUsbCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = new JLabel("USB", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(titleLabel, BorderLayout.NORTH);

        usbContent = new JTextArea(template.getUSBInfo());
        usbContent.setEditable(false);
        usbContent.setFont(new Font("Monospaced", Font.PLAIN, 11));
        usbContent.setLineWrap(true);
        usbContent.setWrapStyleWord(true);
        usbContent.setOpaque(false);

        JScrollPane scroll = new JScrollPane(usbContent);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    public void refreshCpu() {
        if (cpuContent != null) {
            cpuContent.setText(template.getCPUInfo());
        }
    }

    public void refreshMemory() {
        if (memContent != null) {
            memContent.setText(template.getMemoryInfo());
        }
    }

    public void refreshUsb() {
        if (usbContent != null) {
            usbContent.setText(template.getUSBInfo());
        }
    }
}
