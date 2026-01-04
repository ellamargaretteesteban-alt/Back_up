package ui.common;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * AboutPanel displays information about WellCo.
 */
public class AboutPanel {
    
    private JFrame frame;
    
    public AboutPanel(JFrame frame) {
        this.frame = frame;
    }
    
    public JPanel createPanel() {
        JPanel about = new JPanel();
        about.setLayout(new BoxLayout(about, BoxLayout.Y_AXIS));
        about.setOpaque(false); // Transparent to show background
        about.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("About Us");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE); // White text for contrast
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton team = createStyledButton("Our Team");
        team.addActionListener(e -> {
            team.getModel().setPressed(false);
            team.getModel().setArmed(false);
            team.setOpaque(false);
            team.setBackground(null);
            JOptionPane.showMessageDialog(frame,
                "Team Members:\n- Ctrl C/V Team\n- Developer\n- Designer");
        });

        JButton faq = createStyledButton("FAQ");
        faq.addActionListener(e -> {
            faq.getModel().setPressed(false);
            faq.getModel().setArmed(false);
            faq.setOpaque(false);
            faq.setBackground(null);
            JOptionPane.showMessageDialog(frame,
                "Q: What is WellCo?\nA: A health & pill assistant app.\n\n" +
                        "Q: Is this accurate?\nA: Yes, continuously improved.");
        });

        JButton mission = createStyledButton("Mission Statement");
        mission.addActionListener(e -> {
            mission.getModel().setPressed(false);
            mission.getModel().setArmed(false);
            mission.setOpaque(false);
            mission.setBackground(null);
            JOptionPane.showMessageDialog(frame,
                "Our mission: Provide accurate, patient-focused health information.");
        });

        JButton website = createStyledButton("Visit Website");
        website.addActionListener(e -> {
            website.getModel().setPressed(false);
            website.getModel().setArmed(false);
            website.setOpaque(false);
            website.setBackground(null);
            try {
                Desktop.getDesktop().browse(new java.net.URI("https://wellco.com"));
            } catch (Exception ignored) {}
        });

        about.add(title);
        about.add(Box.createVerticalStrut(20));
        about.add(team);
        about.add(faq);
        about.add(mission);
        about.add(website);

        return about;
    }
    
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false); // Prevent focus painting
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hover effect - ensure button returns to normal state after click
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(255, 255, 255, 30));
                btn.setOpaque(true);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setOpaque(false);
                btn.setBackground(null);
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // Prevent default pressed state
                btn.setOpaque(false);
                btn.setBackground(null);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                // Return to hover state if mouse is still over button
                if (btn.contains(e.getPoint())) {
                    btn.setBackground(new Color(255, 255, 255, 30));
                    btn.setOpaque(true);
                } else {
                    btn.setOpaque(false);
                    btn.setBackground(null);
                }
            }
        });
        
        return btn;
    }
}

