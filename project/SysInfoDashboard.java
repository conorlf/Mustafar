import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class SysInfoDashboard extends JFrame {

    private final Color bgColor = new Color(25, 25, 25);
    private final Color panelColor = new Color(40, 40, 40);
    private final Color accentColor = new Color(0, 122, 204);
    private final Color textColor = new Color(230, 230, 230);

    public SysInfoDashboard() {
        super("System Information Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
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

        // Grid layout for cards
        JPanel cardPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardPanel.setBackground(bgColor);
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add subsystem cards
        cardPanel.add(createCard("CPU", "View CPU information", () -> showOutputWindow("CPU Info", template::showCPU)));
        cardPanel.add(createCard("PCI", "View PCI devices", () -> showOutputWindow("PCI Info", template::showPCI)));
        cardPanel.add(createCard("USB", "View USB devices", () -> showOutputWindow("USB Info", template::showUSB)));
        cardPanel.add(
                createCard("Memory", "View memory usage", () -> showOutputWindow("Memory Info", template::showMem)));
        cardPanel.add(createCard("Disk", "View disks", () -> showOutputWindow("Disk Info", template::showDisk)));

        mainPanel.add(cardPanel, BorderLayout.CENTER);
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

    /**
     * Run a template method and capture its System.out output into a scrollable
     * modal window
     */
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
        textArea.setBackground(panelColor);
        textArea.setForeground(textColor);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(textArea);
        dialog.add(scroll);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        // Load dictionaries (simulate original template behavior)
        // You may need to call Dictionary.loadPCIDictionary / loadUSBDictionary here
        SwingUtilities.invokeLater(() -> {
            SysInfoDashboard ui = new SysInfoDashboard();
            ui.setVisible(true);
        });
    }
}
