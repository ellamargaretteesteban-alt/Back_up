package ui.common;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * BackgroundPanel provides gradient background similar to FinalProject.
 */
public class BackgroundPanel extends JPanel {

    private BufferedImage image;

    // Gradient only
    public BackgroundPanel() {
        setOpaque(false);
    }

    // Image + gradient fallback
    public BackgroundPanel(BufferedImage img) {
        this.image = img;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        if (image != null) {
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Gradient from deep blue to soft blue (same as FinalProject)
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(10, 78, 120),    // deep blue
                    0, getHeight(), new Color(180, 210, 230) // soft blue
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.dispose();
    }
}















