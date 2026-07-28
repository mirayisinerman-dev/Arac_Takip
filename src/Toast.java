import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toast {
    
    public static void basarili(Component parent, String message) {
        goster(parent, message, new Color(40, 167, 69)); // Yeşil (Success)
    }
    
    public static void hata(Component parent, String message) {
        goster(parent, message, new Color(220, 53, 69)); // Kırmızı (Error)
    }
    
    public static void uyari(Component parent, String message) {
        goster(parent, message, new Color(255, 193, 7)); // Sarı (Warning)
    }

    private static void goster(Component parent, String message, Color bgColor) {
        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.setAlwaysOnTop(true);
        dialog.setFocusableWindowState(false);
        dialog.setBackground(new Color(0, 0, 0, 0)); // Arkaplanı şeffaf yap
        
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Yuvarlak köşeler
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        if (bgColor.equals(new Color(255, 193, 7))) {
            label.setForeground(Color.BLACK); // Sarı uyarılarda siyah yazı daha okunaklıdır
        }
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Font boyutu 14'ten 12'ye küçültüldü
        panel.add(label);
        
        dialog.add(panel);
        dialog.pack();
        
        // Konumlandırma (Ana pencerenin SAĞ-ALT köşesi)
        if (parent != null && parent.isShowing()) {
            Window window = SwingUtilities.getWindowAncestor(parent);
            if (window == null && parent instanceof Window) window = (Window) parent;
            
            if (window != null) {
                int x = window.getLocationOnScreen().x + window.getWidth() - dialog.getWidth() - 20; // Sağa dayalı, 20px boşluk
                int y = window.getLocationOnScreen().y + window.getHeight() - dialog.getHeight() - 40; // Alta dayalı, 40px boşluk
                dialog.setLocation(x, y);
            }
        } else {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setLocation(screen.width - dialog.getWidth() - 30, screen.height - dialog.getHeight() - 60);
        }
        
        try {
            dialog.setOpacity(1.0f);
        } catch (Exception e) {
            // Sistem saydamlığı desteklemiyorsa görmezden gel
        }
        
        dialog.setVisible(true);
        
        // Animasyon ve otomatik kapanma zamanlayıcısı (Timer)
        Timer timer = new Timer(30, null);
        timer.addActionListener(new ActionListener() {
            float opacity = 1.0f;
            int delayCount = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (delayCount < 50) { // Yaklaşık 1.5 saniye ekranda kal
                    delayCount++;
                    return;
                }
                opacity -= 0.05f; // Yavaşça saydamlaşarak kaybol (Fade out)
                if (opacity <= 0.0f) {
                    timer.stop();
                    dialog.dispose();
                } else {
                    try {
                        dialog.setOpacity(opacity);
                    } catch (Exception ex) {
                        timer.stop();
                        dialog.dispose();
                    }
                }
            }
        });
        timer.start();
    }
}
