package ui.auth;

import database.DatabaseManager;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * SignupPanel handles user registration interface.
 */
public class SignupPanel {
    
    // Background is now handled by BackgroundPanel, so we don't need this
    
    private DatabaseManager dbManager;
    private CardLayout rootLayout;
    private JPanel rootPanel;
    private CardLayout contentLayout;
    private JPanel contentCards;
    
    public SignupPanel(DatabaseManager dbManager, CardLayout rootLayout, JPanel rootPanel) {
        this.dbManager = dbManager;
        this.rootLayout = rootLayout;
        this.rootPanel = rootPanel;
    }
    
    // Set content cards reference (called after contentCards is created)
    public void setContentCards(CardLayout contentLayout, JPanel contentCards) {
        this.contentLayout = contentLayout;
        this.contentCards = contentCards;
    }
    
    public JPanel createPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false); // Transparent to show background gradient

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("WellCo Registration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE); // White text for contrast

        // Fixed size text fields - prevent expansion
        int fieldWidth = 250;
        int fieldHeight = 25;
        
        // Username field
        JTextField tfUsername = new JTextField(20);
        tfUsername.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        tfUsername.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
        tfUsername.setMaximumSize(new Dimension(fieldWidth, fieldHeight));
        tfUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c == ' ') {
                    e.consume(); // Prevent spaces
                }
            }
        });

        // Email field
        JTextField tfEmail = new JTextField(20);
        tfEmail.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        tfEmail.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
        tfEmail.setMaximumSize(new Dimension(fieldWidth, fieldHeight));

        // Password fields (confidential format - JPasswordField shows dots/asterisks)
        JPasswordField tfPassword = new JPasswordField(20);
        tfPassword.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        tfPassword.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
        tfPassword.setMaximumSize(new Dimension(fieldWidth, fieldHeight));
        
        JPasswordField tfConfirmPassword = new JPasswordField(20);
        tfConfirmPassword.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        tfConfirmPassword.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
        tfConfirmPassword.setMaximumSize(new Dimension(fieldWidth, fieldHeight));

        // Message label for validation errors (red) and success (green)
        JLabel messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Create labels with white text
        JLabel userLbl = new JLabel("Username:");
        userLbl.setForeground(Color.WHITE);
        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setForeground(Color.WHITE);
        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(Color.WHITE);
        JLabel confirmPassLbl = new JLabel("Confirm Password:");
        confirmPassLbl.setForeground(Color.WHITE);

        JButton signupBtn = new JButton("Create Account");
        signupBtn.addActionListener(e -> {
            String username = tfUsername.getText().trim();
            String email = tfEmail.getText().trim();
            String password = new String(tfPassword.getPassword());
            String confirmPassword = new String(tfConfirmPassword.getPassword());

            // Clear previous messages
            messageLabel.setText(" ");
            messageLabel.setForeground(Color.RED);

            // Validation
            java.util.List<String> errors = SignupValidator.validate(username, email, password, confirmPassword);
            if (!errors.isEmpty()) {
                // Show all validation errors
                String errorMessage = "<html>" + String.join("<br>", errors) + "</html>";
                messageLabel.setText(errorMessage);
                return;
            }

            // Check if username exists (works with both SQL and ArrayList)
            if (dbManager.usernameExists(username)) {
                messageLabel.setText("Username already exists.");
                return;
            }

            // Check if email exists
            if (dbManager.emailExists(email)) {
                messageLabel.setText("Email already registered.");
                return;
            }

            // All validations passed - show green message
            messageLabel.setForeground(new Color(0, 150, 0)); // Green
            messageLabel.setText("All fields are valid!");

            // Show confirmation dialog with bold name
            String confirmationMessage = "<html>Do you wish to register as <b>" + username + "</b>?</html>";
            int response = JOptionPane.showConfirmDialog(
                p,
                confirmationMessage,
                "Confirm Registration",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (response == JOptionPane.YES_OPTION) {
                // Register user (default role is Customer, set automatically)
                boolean success = dbManager.registerUser(username, password, email);
                if (success) {
                    messageLabel.setForeground(new Color(0, 150, 0)); // Green
                    messageLabel.setText("Account created successfully! Redirecting...");
                    signupBtn.setEnabled(false); // Prevent spam clicking
                    
                    // Delay before redirecting
                    Timer timer = new Timer(1500, evt -> {
                        // Navigate to LOGIN in contentCards
                        if (contentLayout != null && contentCards != null) {
                            // Ensure we're on PUBLIC panel first
                            rootLayout.show(rootPanel, "PUBLIC");
                            // Then show LOGIN in contentCards
                            SwingUtilities.invokeLater(() -> {
                                contentLayout.show(contentCards, "LOGIN");
                            });
                        }
                        // Clear fields
                        tfUsername.setText("");
                        tfEmail.setText("");
                        tfPassword.setText("");
                        tfConfirmPassword.setText("");
                        signupBtn.setEnabled(true);
                        messageLabel.setText(" ");
                    });
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Registration failed. Please try again.");
                }
            } else {
                // User clicked "No" - don't clear fields, just reset message
                messageLabel.setText(" ");
            }
        });

        // Back to login link
        JLabel backLabel = new JLabel("<html><a href=''>Back to Login</a></html>");
        backLabel.setForeground(Color.WHITE);
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Navigate to LOGIN in contentCards
                if (contentLayout != null && contentCards != null) {
                    // Ensure we're on PUBLIC panel first
                    rootLayout.show(rootPanel, "PUBLIC");
                    // Then show LOGIN in contentCards
                    SwingUtilities.invokeLater(() -> {
                        contentLayout.show(contentCards, "LOGIN");
                    });
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                backLabel.setForeground(new Color(200, 220, 255));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                backLabel.setForeground(Color.WHITE);
            }
        });

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; p.add(title, gbc);
        gbc.gridy = row++; p.add(userLbl, gbc);
        gbc.gridy = row++; p.add(tfUsername, gbc);
        gbc.gridy = row++; p.add(emailLbl, gbc);
        gbc.gridy = row++; p.add(tfEmail, gbc);
        gbc.gridy = row++; p.add(passLbl, gbc);
        gbc.gridy = row++; p.add(tfPassword, gbc);
        gbc.gridy = row++; p.add(confirmPassLbl, gbc);
        gbc.gridy = row++; p.add(tfConfirmPassword, gbc);
        gbc.gridy = row++; p.add(messageLabel, gbc);
        gbc.gridy = row++; p.add(signupBtn, gbc);
        gbc.gridy = row++; p.add(backLabel, gbc);

        return p;
    }
}

