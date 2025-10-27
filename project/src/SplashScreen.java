import javax.swing.*;
import java.awt.*;

public class SplashScreen {

    public static JWindow showSplash(String message) {
        JWindow splash = new JWindow();

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(25, 25, 25));
        splash.setContentPane(content);

        JLabel label = new JLabel(message, JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        content.add(label, BorderLayout.CENTER);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBackground(new Color(40, 40, 40));
        bar.setForeground(new Color(0, 122, 204));
        content.add(bar, BorderLayout.SOUTH);

        splash.pack();
        splash.setSize(400, 200);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        return splash;
    }
}
