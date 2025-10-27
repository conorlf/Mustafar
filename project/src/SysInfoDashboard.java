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
        // super class contructor String title
        super("System Information");
        // sets the default close operation
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // set the size of the window
        setSize(900, 600);
        // centr the window
        setLocationRelativeTo(null);
        // sets layout background colour and main panel content holder
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(bgColor);
        setContentPane(mainPanel);
        // creates another panel as the top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(panelColor);
        // set border adds padding
        topBar.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel title = new JLabel("System Information");
        title.setForeground(textColor);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        // using the border layout WEST to position the title on the left side
        topBar.add(title, BorderLayout.WEST);
        // using the border layout NORTH to position the top bar at the top of the main
        // panel
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Grid layout for cards
        JPanel cardPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardPanel.setBackground(bgColor);
        // border padding of main panel
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // clickable cards lambda creates thread and runnable and statrs cpu metric
        cardPanel.add(createCard("CPU", "Show CPU Graph", () -> {
            new Thread(() -> new CpuMetric().start()).start();
        }));
        // PCI card
        cardPanel.add(createCard("PCI", "View PCI devices", () -> showOutputWindow("PCI Info", template::showPCI)));

        // USB card
        cardPanel.add(createCard("USB", "View USB devices", () -> showOutputWindow("USB Info", template::showUSB)));

        // Memory card
        cardPanel.add(createCard("Memory", "Show Memory Graph", () -> {
            new Thread(() -> new MemMetric().start()).start();
        }));

        // Disk card: shows total/used on card and detailed info on click
        memInfo mem = new memInfo();
        mem.read();
        diskInfo disk = new diskInfo();
        disk.read();

        String diskSummary = "<html>Total Memory: " + mem.getTotal() +
                " Used: " + "<b>" + mem.getUsed() + "</b>" + "<br>" +
                "Disks: ";
        for (int i = 0; i < disk.diskCount(); i++) {
            diskSummary += disk.getUsed(i) + "/" + disk.getTotal(i) + " ";
        }
        diskSummary += "</html>";

        cardPanel.add(createCard("Disk", diskSummary, () -> showOutputWindow("Disk Info", () -> template.showDisk())));

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // Notification glass
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
        notificationLabel.setBackground(new Color(0, 122, 204, 220));
        notificationLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        notificationGlass.add(notificationLabel, new GridBagConstraints());
        notificationGlass.setVisible(false);
        setGlassPane(notificationGlass);
    }

    private JPanel createCard(String title, String description, Runnable onClick) {
        JPanel card = new JPanel();
        card.setBackground(new Color(40, 40, 40));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                new EmptyBorder(20, 20, 20, 20)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(textColor);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
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

    private void showOutputWindow(String title, Runnable templateMethod) {
        JDialog dialog = new JDialog(this, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        // Capture System.out output
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

        String output = baos.toString();

        JTextArea textArea = new JTextArea(output);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setBackground(new Color(40, 40, 40));
        textArea.setForeground(new Color(230, 230, 230));
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
