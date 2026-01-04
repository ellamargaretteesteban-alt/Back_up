package ui.common;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * IntroInterface displays the application intro screen with logo animation.
 */
public class IntroInterface {
    
    private static final Color BACKGROUND_COLOR = new Color(225, 245, 255); // Same as login background
    private static final Color ACCENT_BLUE = new Color(39, 174, 219);
    
    private Runnable onIntroComplete;
    private BufferedImage logoImage;
    
    public IntroInterface(JFrame frame, Runnable onIntroComplete) {
        this.onIntroComplete = onIntroComplete;
        // Preload the logo image
        loadLogoImage();
    }
    
    /**
     * Loads the logo image from file
     */
    private void loadLogoImage() {
        try {
            File logoFile = new File("pictures/logo.png");
            if (logoFile.exists()) {
                logoImage = ImageIO.read(logoFile);
                System.out.println("✓ Logo loaded successfully: " + logoFile.getAbsolutePath());
            } else {
                System.out.println("⚠ Logo file not found: " + logoFile.getAbsolutePath());
                logoImage = null;
            }
        } catch (IOException e) {
            System.err.println("✗ Error loading logo: " + e.getMessage());
            logoImage = null;
        }
    }
    
    /**
     * Creates the intro panel with animations
     */
    public JPanel createPanel() {
        JPanel introPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Subtle gradient background
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                
                // Soft gradient from top to bottom (matching login background)
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(225, 245, 255),
                    0, height, new Color(220, 240, 250)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
            }
        };
        introPanel.setBackground(BACKGROUND_COLOR);
        
        // Center panel for logo and text
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Logo panel (will be animated)
        JPanel logoPanel = createLogoPanel();
        logoPanel.setOpaque(false);
        
        // WellCo title (will fade in)
        JLabel titleLabel = new JLabel("WellCo");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(ACCENT_BLUE);
        titleLabel.setVisible(false);
        
        // Motto label (will fade in after title)
        JLabel mottoLabel = new JLabel("Inspired by Patients — Driven by Science");
        mottoLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        mottoLabel.setForeground(new Color(100, 100, 100));
        mottoLabel.setVisible(false);
        
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        centerPanel.add(logoPanel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 5, 0);
        centerPanel.add(titleLabel, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 0, 0, 0);
        centerPanel.add(mottoLabel, gbc);
        
        introPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Start animations
        startAnimations(logoPanel, titleLabel, mottoLabel);
        
        return introPanel;
    }
    
    /**
     * Creates the logo panel with image and checkmark animation
     */
    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Get animation properties
                Float scale = (Float) getClientProperty("scale");
                Float alpha = (Float) getClientProperty("alpha");
                Double checkmarkProgress = (Double) getClientProperty("checkmark");
                
                if (scale == null) scale = 0.3f;
                if (alpha == null) alpha = 0.0f;
                if (checkmarkProgress == null) checkmarkProgress = 0.0;
                
                // Apply alpha composite for fade-in
                AlphaComposite alphaComposite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alpha
                );
                g2d.setComposite(alphaComposite);
                
                // Draw the logo image if available
                if (logoImage != null) {
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    
                    // Use panel size or fallback to preferred size
                    if (panelWidth == 0) panelWidth = 300;
                    if (panelHeight == 0) panelHeight = 300;
                    
                    int imgWidth = logoImage.getWidth();
                    int imgHeight = logoImage.getHeight();
                    
                    // Base scale to fit while maintaining aspect ratio
                    double baseScale = Math.min(
                        (double) panelWidth / imgWidth,
                        (double) panelHeight / imgHeight
                    ) * 0.6; // 60% of available space
                    
                    // Apply animation scale
                    int scaledWidth = (int) (imgWidth * baseScale * scale);
                    int scaledHeight = (int) (imgHeight * baseScale * scale);
                    
                    int x = (panelWidth - scaledWidth) / 2;
                    int y = (panelHeight - scaledHeight) / 2;
                    
                    // Draw logo with scale animation
                    g2d.drawImage(logoImage, x, y, scaledWidth, scaledHeight, null);
                    
                    // Draw checkmark animation (like Nike)
                    if (checkmarkProgress > 0) {
                        drawCheckmark(g2d, x, y, scaledWidth, scaledHeight, checkmarkProgress);
                    }
                } else {
                    // Fallback: Draw placeholder if logo not found
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    g2d.setColor(ACCENT_BLUE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 24));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "Logo";
                    int panelWidth = getWidth() > 0 ? getWidth() : 300;
                    int panelHeight = getHeight() > 0 ? getHeight() : 300;
                    int x = (panelWidth - fm.stringWidth(text)) / 2;
                    int y = (panelHeight + fm.getAscent()) / 2;
                    g2d.drawString(text, x, y);
                }
            }
            
            /**
             * Draws an animated checkmark (Nike-style) on the logo
             */
            private void drawCheckmark(Graphics2D g2d, int logoX, int logoY, int logoWidth, int logoHeight, double progress) {
                // Reset composite to full opacity for checkmark
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                
                // Calculate checkmark position (bottom-right of logo)
                int checkSize = Math.min(logoWidth, logoHeight) / 3;
                int checkX = logoX + logoWidth - checkSize;
                int checkY = logoY + logoHeight - checkSize;
                
                // Checkmark color (green like Nike)
                g2d.setColor(new Color(34, 197, 94));
                g2d.setStroke(new BasicStroke(checkSize / 8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                // Draw checkmark path (animated)
                int startX = checkX;
                int startY = checkY + checkSize / 2;
                int midX = checkX + checkSize / 3;
                int midY = checkY + checkSize * 2 / 3;
                int endX = checkX + checkSize;
                int endY = checkY;
                
                // Animate the checkmark drawing
                if (progress < 0.5) {
                    // Draw first part of checkmark (down stroke)
                    double firstPart = progress / 0.5;
                    int currentMidX = (int) (startX + (midX - startX) * firstPart);
                    int currentMidY = (int) (startY + (midY - startY) * firstPart);
                    g2d.drawLine(startX, startY, currentMidX, currentMidY);
                } else {
                    // Draw complete first part
                    g2d.drawLine(startX, startY, midX, midY);
                    
                    // Draw second part of checkmark (up stroke)
                    double secondPart = (progress - 0.5) / 0.5;
                    int currentEndX = (int) (midX + (endX - midX) * secondPart);
                    int currentEndY = (int) (midY + (endY - midY) * secondPart);
                    g2d.drawLine(midX, midY, currentEndX, currentEndY);
                }
            }
        };
        
        // Set preferred size based on logo image if available
        if (logoImage != null) {
            int imgWidth = logoImage.getWidth();
            int imgHeight = logoImage.getHeight();
            // Set preferred size to accommodate the logo with some padding
            logoPanel.setPreferredSize(new Dimension(
                Math.max(300, (int)(imgWidth * 0.6)),
                Math.max(300, (int)(imgHeight * 0.6))
            ));
        } else {
            logoPanel.setPreferredSize(new Dimension(300, 300));
        }
        logoPanel.setOpaque(false);
        
        return logoPanel;
    }
    
    /**
     * Starts the animation sequence
     */
    private void startAnimations(JPanel logoPanel, JLabel titleLabel, JLabel mottoLabel) {
        // Animation timeline
        Timer timer = new Timer(50, null);
        final long startTime = System.currentTimeMillis();
        
        ActionListener animator = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                
                // Phase 1: Logo pop-up (0-800ms)
                if (elapsed < 800) {
                    double progress = elapsed / 800.0;
                    // Ease-out animation
                    progress = 1 - Math.pow(1 - progress, 3);
                    float scale = (float) (0.3f + progress * 0.7f); // Scale from 30% to 100%
                    float alpha = (float) progress;
                    
                    logoPanel.putClientProperty("scale", scale);
                    logoPanel.putClientProperty("alpha", alpha);
                    logoPanel.repaint();
                }
                
                // Phase 2: Check mark animation (800-1400ms)
                if (elapsed >= 800 && elapsed < 1400) {
                    double progress = (elapsed - 800) / 600.0;
                    logoPanel.putClientProperty("checkmark", progress);
                    logoPanel.repaint();
                }
                
                // Phase 3: Title fade-in (1400-2000ms)
                if (elapsed >= 1400 && elapsed < 2000) {
                    double progress = (elapsed - 1400) / 600.0;
                    float alpha = (float) progress;
                    titleLabel.setVisible(true);
                    setLabelAlpha(titleLabel, alpha);
                }
                
                // Phase 4: Motto fade-in (2000-2600ms)
                if (elapsed >= 2000 && elapsed < 2600) {
                    double progress = (elapsed - 2000) / 600.0;
                    float alpha = (float) progress;
                    mottoLabel.setVisible(true);
                    setLabelAlpha(mottoLabel, alpha);
                }
                
                // Phase 5: Hold and transition (2600-3500ms)
                if (elapsed >= 2600 && elapsed < 3500) {
                    // Hold all elements visible
                }
                
                // Complete animation and transition to login
                if (elapsed >= 3500) {
                    timer.stop();
                    if (onIntroComplete != null) {
                        onIntroComplete.run();
                    }
                }
            }
        };
        
        timer.addActionListener(animator);
        timer.start();
    }
    
    /**
     * Sets alpha transparency for a label
     */
    private void setLabelAlpha(JLabel label, float alpha) {
        label.setForeground(new Color(
            label.getForeground().getRed(),
            label.getForeground().getGreen(),
            label.getForeground().getBlue(),
            (int) (alpha * 255)
        ));
    }
}

