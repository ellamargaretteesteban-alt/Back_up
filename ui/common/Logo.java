package ui.common;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Logo helper class to load and scale the logo image.
 */
public class Logo {
    private ImageIcon icon;
    
    public Logo() {
        loadLogo();
    }
    
    private void loadLogo() {
        try {
            File logoFile = new File("pictures/logo.png");
            if (logoFile.exists()) {
                BufferedImage img = ImageIO.read(logoFile);
                // Scale to appropriate size for navigation bar (about 40-50px height)
                int targetHeight = 45;
                int targetWidth = (int) (img.getWidth() * ((double) targetHeight / img.getHeight()));
                Image scaled = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaled);
            } else {
                // Fallback: create a text logo
                icon = null;
            }
        } catch (IOException e) {
            System.err.println("Error loading logo: " + e.getMessage());
            icon = null;
        }
    }
    
    public ImageIcon getIcon() {
        return icon;
    }
}















