import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CardPanel extends JPanel {
    private final JTextArea contentArea;
    private final JLabel titleLabel;

    public CardPanel(String title, String description) {
        super(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(10, 10, 10, 10)));

        titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        contentArea = new JTextArea(description);
        contentArea.setEditable(false);
        contentArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setOpaque(false);

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    /** This lets you update the visible text later. */
    public void updateCard(String newDescription) {
        Runnable r = () -> {
            contentArea.setText(newDescription);
            revalidate();
            repaint();
        };
        if (SwingUtilities.isEventDispatchThread())
            r.run();
        else
            SwingUtilities.invokeLater(r);
    }

}