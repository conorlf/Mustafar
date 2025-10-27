import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.TimerTask;
import java.util.Timer;

import org.knowm.xchart.XChartPanel;

public class SysInfoDashboard extends JFrame {

    private final Color bgColor = new Color(25, 25, 25);
    private final Color panelColor = new Color(40, 40, 40);
    private final Color textColor = new Color(230, 230, 230);

    private JTextArea pciArea;
    private JTextArea usbArea;

    public SysInfoDashboard() {
        super("System Information Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(bgColor);
        setContentPane(mainPanel);

        // Box 1: CPU Specs
        JPanel cpuSpecBox = createBox("CPU Specs", template::showCPU, false);
        mainPanel.add(cpuSpecBox);

        // Box 2: CPU Graph
        JPanel cpuGraphBox = createBox("CPU Usage Graph", () -> new CpuMetric().start(), false);
        mainPanel.add(cpuGraphBox);

        // Box 3: Memory Total
        JPanel memSpecBox = createBox("Memory Total", template::showMem, false);
        mainPanel.add(memSpecBox);

        // Box 4: Memory Graph
        JPanel memGraphBox = createBox("Memory Usage Graph", () -> new MemMetric().start(), false);
        mainPanel.add(memGraphBox);

        // Box 5: Disk Usage Summary
        JPanel diskSummaryBox = createBox("Disk Usage (Used/Total)", this::showDiskSummary, false);
        mainPanel.add(diskSummaryBox);

        // Box 6: Disk Full Details
        JPanel diskDetailsBox = createBox("Disk Details", () -> showOutputWindow("Disk Info", template::showDisk),
                false);
        mainPanel.add(diskDetailsBox);

        // Box 7: PCI Devices (Live)
        pciArea = new JTextArea();
        JScrollPane pciScroll = new JScrollPane(pciArea);
        pciArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        pciArea.setEditable(false);
        JPanel pciBox = createBox("PCI Devices", pciScroll);
        mainPanel.add(pciBox);
        startPCIUpdate();

        // Box 8: USB Devices (Live)
        usbArea = new JTextArea();
        JScrollPane usbScroll = new JScrollPane(usbArea);
        usbArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        usbArea.setEditable(false);
        JPanel usbBox = createBox("USB Devices", usbScroll);
        mainPanel.add(usbBox);
        startUSBUpdate();
    }

    private JPanel createBox(String title, Runnable onClick, boolean captureOutput) {
        JPanel box = new JPanel();
        box.setBackground(panelColor);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                new EmptyBorder(10, 10, 10, 10)));
        box.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(textColor);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        box.add(titleLabel, BorderLayout.NORTH);

        JButton button = new JButton("View");
        box.add(button, BorderLayout.CENTER);

        button.addActionListener(e -> {
            if (captureOutput) {
                showOutputWindow(title, onClick);
            } else {
                onClick.run();
            }
        });

        return box;
    }

    private JPanel createBox(String title, JComponent component) {
        JPanel box = new JPanel();
        box.setBackground(panelColor);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                new EmptyBorder(10, 10, 10, 10)));
        box.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(textColor);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        box.add(titleLabel, BorderLayout.NORTH);

        box.add(component, BorderLayout.CENTER);
        return box;
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
        } finally {
            System.out.flush();
            System.setOut(oldOut);
        }

        JTextArea textArea = new JTextArea(baos.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setBackground(panelColor);
        textArea.setForeground(textColor);
        textArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(textArea);
        dialog.add(scroll);
        dialog.setVisible(true);
    }

    private void showDiskSummary() {
        diskInfo disk = new diskInfo();
        disk.read();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < disk.diskCount(); i++) {
            sb.append(String.format("%s : Used %d / Total %d\n",
                    disk.getName(i),
                    disk.getUsed(i),
                    disk.getTotal(i)));
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Disk Usage", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startPCIUpdate() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                pciInfo pci = new pciInfo();
                pci.read();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pci.busCount(); i++) {
                    for (int j = 0; j < 32; j++) {
                        for (int k = 0; k < 8; k++) {
                            if (pci.functionPresent(i, j, k) > 0) {
                                int vendorId = pci.vendorID(i, j, k);
                                int productId = pci.productID(i, j, k);
                                String vendor = Dictionary.getPCIVendorName(vendorId);
                                String device = Dictionary.getPCIDeviceName(vendorId, productId);
                                if (vendor == null || vendor.isEmpty())
                                    vendor = String.format("Unknown Vendor (0x%04X)", vendorId);
                                if (device == null || device.isEmpty())
                                    device = String.format("Unknown Device (0x%04X)", productId);
                                sb.append(String.format("%s : %s Bus : %d\n", vendor, device, i));
                            }
                        }
                    }
                }
                SwingUtilities.invokeLater(() -> pciArea.setText(sb.toString()));
            }
        }, 0, 3000);
    }

    private void startUSBUpdate() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                usbInfo usb = new usbInfo();
                usb.read();
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= usb.busCount(); i++) {
                    for (int j = 1; j <= usb.deviceCount(i); j++) {
                        int vendorId = usb.vendorID(i, j);
                        int productId = usb.productID(i, j);
                        if (vendorId == 0 && productId == 0)
                            continue;
                        String vendor = Dictionary.getUSBVendorName(vendorId);
                        String device = Dictionary.getUSBDeviceName(vendorId, productId);
                        if (vendor == null || vendor.isEmpty())
                            vendor = String.format("Unknown Vendor (0x%04X)", vendorId);
                        if (device == null || device.isEmpty())
                            device = String.format("Unknown Device (0x%04X)", productId);
                        sb.append(String.format("%s : %s Bus : %d\n", vendor, device, i));
                    }
                }
                SwingUtilities.invokeLater(() -> usbArea.setText(sb.toString()));
            }
        }, 0, 3000);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SysInfoDashboard ui = new SysInfoDashboard();
            ui.setVisible(true);
        });
    }
}
