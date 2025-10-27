import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FrontEnd extends JFrame {

    public FrontEnd() {
        // Frame setup
        setTitle("Dark Mode UI Template");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        // Define dark mode colors
        Color bgColor = new Color(25, 25, 25);
        Color panelColor = new Color(40, 40, 40);
        Color accentColor = new Color(0, 122, 204);
        Color textColor = new Color(230, 230, 230);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(bgColor);
        setContentPane(mainPanel);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(panelColor);
        topBar.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Dark Mode Dashboard");
        title.setForeground(textColor);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topBar.add(title, BorderLayout.WEST);

        JButton settingsBtn = new JButton("⚙ Settings");
        styleButton(settingsBtn, panelColor, accentColor, textColor);
        topBar.add(settingsBtn, BorderLayout.EAST);

        mainPanel.add(topBar, BorderLayout.NORTH);

        // Center content area
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(bgColor);
        contentPanel.setLayout(new GridLayout(1, 2, 15, 15));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card1 = createCard("Overview", "System performance summary", panelColor, textColor);
        JPanel card2 = createCard("Activity", "Recent user activity", panelColor, textColor);

        contentPanel.add(card1);
        contentPanel.add(card2);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Bottom status bar
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setForeground(textColor);
        statusLabel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(panelColor);
        statusBar.add(statusLabel, BorderLayout.WEST);

        mainPanel.add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel createCard(String title, String description, Color bg, Color text) {
        JPanel card = new JPanel();
        card.setBackground(bg);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(text);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><body style='width: 200px'>" + description + "</body></html>");
        descLabel.setForeground(text.darker());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        card.add(titleLabel);
        card.add(descLabel);

        return card;
    }

    private void styleButton(JButton button, Color bg, Color accent, Color text) {
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBorder(BorderFactory.createLineBorder(accent));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(accent);
                button.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
                button.setForeground(text);
            }
        });
    }

    public void showMessageWindow(String message) {
        // Create a new window
        JFrame messageFrame = new JFrame("Message");
        messageFrame.setSize(400, 200);
        messageFrame.setLocationRelativeTo(this); // center relative to main window
        messageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Dark mode colors (same as main UI)
        Color bgColor = new Color(25, 25, 25);
        Color textColor = new Color(230, 230, 230);

        // Center panel
        JPanel panel = new JPanel(new GridBagLayout()); // centers content
        panel.setBackground(bgColor);

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setForeground(textColor);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));

        panel.add(label);
        messageFrame.add(panel);

        messageFrame.setVisible(true);
    }
}

// public static void main(String[] args) {
// // Optional: use system look & feel for smoother fonts
// try {
// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
// } catch (Exception ignored) {}

// SwingUtilities.invokeLater(() -> {
// FrontEnd ui = new FrontEnd();
// ui.setVisible(true);
// ui.showMessageWindow();
// });
// }
// }
