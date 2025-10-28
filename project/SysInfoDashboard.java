import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import template;

public class SysInfoDashboard extends JPanel {

    private final JPanel notificationGlass;
    private final JLabel notificationLabel;
    private Timer notificationTimer;

    public SysInfoDashboard() {
        super(new OverlayLayout(this));

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

        // Cards panel
        JPanel cardPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add cards
        cardPanel.add(createCard("CPU", template.getCPUInfo()));
        cardPanel.add(createCard("Memory", template.getMemoryInfo()));
        cardPanel.add(createCard("Disk", template.getDiskInfo()));
        cardPanel.add(createCard("PCI", template.getPCIInfo()));
        cardPanel.add(createCard("USB", template.getUSBInfo()));
        // 6th cell left empty

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
        notificationLabel.setOpaque(true);
        notificationLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        notificationGlass.add(notificationLabel, new GridBagConstraints());
        notificationGlass.setVisible(false);

        // Overlay notificationGlass on this panel
        add(notificationGlass);
        add(mainPanel);
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

    // Show notification overlay
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
}
