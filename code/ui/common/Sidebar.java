package ui.common;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Sidebar component for navigation menu.
 */
public class Sidebar {
    
    private static final Color SIDEBAR_BLUE = new Color(39, 174, 219);
    
    private JFrame frame;
    
    public Sidebar(JFrame frame) {
        this.frame = frame;
    }
    
    public JPanel createPanel(JPanel contentCards) {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(SIDEBAR_BLUE);
        side.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Top buttons
        JPanel menu = new JPanel(new GridLayout(6, 1, 10, 10));
        menu.setBackground(SIDEBAR_BLUE);

        JButton b1 = createSidebarButton("HOME");
        JButton b2 = createSidebarButton("PROFILE");
        JButton b3 = createSidebarButton("ABOUT");

        // Add listeners DIRECTLY here
        b1.addActionListener(e -> ((CardLayout) contentCards.getLayout()).show(contentCards, "HOME"));
        b2.addActionListener(e -> ((CardLayout) contentCards.getLayout()).show(contentCards, "PROFILE"));
        b3.addActionListener(e -> ((CardLayout) contentCards.getLayout()).show(contentCards, "ABOUT"));

        menu.add(b1);
        menu.add(b2);
        menu.add(b3);

        // Bottom Customer Service button
        JButton support = new JButton("Customer Service");
        support.setFont(new Font("Arial", Font.BOLD, 14));
        support.setBackground(Color.WHITE);
        support.addActionListener(e ->
                JOptionPane.showMessageDialog(frame,
                        "📞 Phone: +1 (800) 555-9000\n✉ Email: support@wellco.com",
                        "Customer Service",
                        JOptionPane.INFORMATION_MESSAGE)
        );

        JPanel bottom = new JPanel();
        bottom.setBackground(SIDEBAR_BLUE);
        bottom.add(support);

        side.add(menu, BorderLayout.NORTH);
        side.add(bottom, BorderLayout.SOUTH);

        return side;
    }
    
    private JButton createSidebarButton(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(200, 60));
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setActionCommand(text);
        return b;
    }
}

