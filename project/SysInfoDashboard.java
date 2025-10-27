import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class SysInfoDashboard extends JFrame {

    private final Color bgColor = new Color(25, 25, 25);
    private final Color panelColor = new Color(40, 40, 40);
    private final Color textColor = new Color(230, 230, 230);
    private final JPanel notificationGlass;
    private final JLabel notificationLabel;
    private Timer notificationTimer;

    public SysInfoDashboard() {
        super("System Information Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(bgColor);
        setContentPane(mainPanel);

        // Top title bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(panelColor);
        topBar.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel title = new JLabel("System Information Dashboard");
        title.setForeground(textColor);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topBar.add(title, BorderLayout.WEST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Grid layout for cards/panels
        JPanel cardPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardPanel.setBackground(bgColor);
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // CPU card
        cardPanel.add(createCard("CPU", "Show CPU Specs", () -> {
            showOutputWindow("CPU Info", template::showCPU);
        }));

        // Memory card
        cardPanel.add(createCard("Memory", "Show Total Memory", () -> {
            showOutputWindow("Memory Info", template::showMem);
        }));

        // Disk card
        cardPanel.add(createCard("Disk", "Show Disk Usage", () -> {
            showOutputWindow("Disk Info", template::showDisk);
        }));

        // PCI panel (scrollable text)
        StringBuilder pciContent = new StringBuilder();
        try {
            pciInfo pci = new pciInfo();
            pci.read();
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

                            pciContent.append(String.format("%s : %s Bus: %02d\n", vendorName, deviceName, i));
                        }
                    }
                }
            }
        } catch (Exception e) {
            pciContent.append("Failed to read PCI info.\n");
        }
        cardPanel.add(createTextPanel("PCI Devices", pciContent.toString()));

        // USB panel (scrollable text)
        StringBuilder usbContent = new StringBuilder();
        try {
            usbInfo usb = new usbInfo();
            usb.read();
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

                    usbContent.append(String.format("%s : %s Bus: %02d\n", vendorName, deviceName, i));
                }
            }
        } catch (Exception e) {
            usbContent.append("Failed to read USB info.\n");
        }
        cardPanel.add(createTextPanel("USB Devices", usbContent.toString()));

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // Notification glass pane
        notificationGlass = new JPanel(new GridBagLayout()) {
            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        notificationLabel = new JLabel("", SwingConstants.CENTER);
        notificationLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        notificationLabel.setForeground(Color.WHITE);
        notificationLabel.setOpaque(true);
        notificationLabel.setBackground(new Color(0, 122, 204, 220)); // semi-transparent blue
        notificationLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        notificationGlass.add(notificationLabel, new GridBagConstraints());
        notificationGlass.setVisible(false);
        setGlassPane(notificationGlass);
    }

    private JPanel createCard(String title, String description, Runnable onClick) {
        JPanel card = new JPanel();
        card.setBackground(panelColor);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                new EmptyBorder(20, 20, 20, 20)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(textColor);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><body style='text-align:center;'>" + description + "</body></html>");
        descLabel.setForeground(textColor.brighter());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        card.add(titleLabel);
        card.add(descLabel);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                onClick.run();
            }
        });

        return card;
    }

    private JPanel createTextPanel(String title, String content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                title,
                0, 0, new Font("Segoe UI", Font.BOLD, 14), textColor));

        JTextArea textArea = new JTextArea(content);
        textArea.setFont(new Font("Monospaced", Font.BOLD, 14)); // thicker text
        textArea.setForeground(textColor);
        textArea.setBackground(panelColor);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(textArea);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void showOutputWindow(String title, Runnable templateMethod) {
        JDialog dialog = new JDialog(this, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream oldOut = System.out;
        System.setOut(ps);

        try {
            templateMethod.run();
        } catch (Exception e) {
            e.printStackTrace(ps);
        } finally {
            System.out.flush();
            System.setOut(oldOut);
        }

        JTextArea textArea = new JTextArea(baos.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setBackground(panelColor);
        textArea.setForeground(textColor);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(textArea);
        dialog.add(scroll);
        dialog.setVisible(true);
    }

    public void showNotification(String message, int durationMs) {
        notificationLabel.setText(message);
        notificationGlass.setVisible(true);
        notificationGlass.revalidate();
        notificationGlass.repaint();

        if (notificationTimer != null && notificationTimer.isRunning())
            notificationTimer.stop();

        notificationTimer = new Timer(durationMs, e -> notificationGlass.setVisible(false));
        notificationTimer.setRepeats(false);
        notificationTimer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SysInfoDashboard ui = new SysInfoDashboard();
            ui.setVisible(true);
        });
    }
}
