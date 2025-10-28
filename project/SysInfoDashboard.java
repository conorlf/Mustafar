import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SysInfoDashboard extends JPanel {

    private final JPanel notificationGlass;
    private final JLabel notificationLabel;
    private Timer notificationTimer;

    public SysInfoDashboard() {
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

        // Cards panel
        JPanel cardPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add cards (replace template.showXXXString() with your real strings)
        cardPanel.add(createCard("CPU", template.showCPUString()));
        cardPanel.add(createCard("PCI", template.showPCIString()));
        cardPanel.add(createCard("USB", template.showUSBString()));
        cardPanel.add(createCard("Memory", "Total / Used"));
        cardPanel.add(createCard("Disk", "Total / Used"));

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
        setLayout(new OverlayLayout(this));
        add(notificationGlass);
        add(mainPanel);
    }

    private JPanel createCard(String title, String description) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        card.add(titleLabel);
        card.add(descLabel);
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
