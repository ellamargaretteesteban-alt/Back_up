package ui.common;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Public HomePanel - shows website information before login.
 */
public class PublicHomePanel extends JPanel {

    private CardLayout contentLayout;
    private JPanel contentCards;
    private CardLayout rootLayout;
    private JPanel rootPanel;

    public PublicHomePanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Main container with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(80, 50, 80, 50));

        // Title
        JLabel titleLabel = new JLabel("WellCo", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(Color.WHITE);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Inspired by Patients — Driven by Science", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        subtitleLabel.setForeground(new Color(200, 200, 255));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));

        // Features panel
        JPanel featuresPanel = new JPanel();
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setOpaque(false);
        featuresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] features = {
            "✓ Comprehensive Health & Medication Management",
            "✓ Patient-Focused Care Solutions",
            "✓ Science-Based Treatment Information",
            "✓ Secure User Profiles & Data Protection",
            "✓ Easy-to-Use Interface",
            "✓ 24/7 Access to Your Health Information"
        };

        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            featureLabel.setForeground(Color.WHITE);
            featureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            featureLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            featuresPanel.add(featureLabel);
        }

        // Welcome message - clickable to navigate to login
        JLabel welcomeLabel = new JLabel("<html><a href=''>Get Started by Logging In</a></html>", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        welcomeLabel.setForeground(new Color(255, 255, 200));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
        welcomeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        welcomeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Navigate to LOGIN in contentCards
                if (contentLayout != null && contentCards != null) {
                    // Ensure we're on PUBLIC panel first
                    if (rootLayout != null && rootPanel != null) {
                        rootLayout.show(rootPanel, "PUBLIC");
                    }
                    // Then show LOGIN in contentCards
                    SwingUtilities.invokeLater(() -> {
                        contentLayout.show(contentCards, "LOGIN");
                    });
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                welcomeLabel.setForeground(new Color(255, 255, 150));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                welcomeLabel.setForeground(new Color(255, 255, 200));
            }
        });

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(titleLabel);
        centerPanel.add(subtitleLabel);
        centerPanel.add(featuresPanel);
        centerPanel.add(welcomeLabel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    // Set content cards reference (called after contentCards is created)
    public void setContentCards(CardLayout contentLayout, JPanel contentCards, CardLayout rootLayout, JPanel rootPanel) {
        this.contentLayout = contentLayout;
        this.contentCards = contentCards;
        this.rootLayout = rootLayout;
        this.rootPanel = rootPanel;
    }
}















