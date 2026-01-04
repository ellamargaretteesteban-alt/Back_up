package ui.auth;

import database.DatabaseManager;
import database.UserData;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;
import javax.swing.*;

/**
 * LoginPanel handles user authentication interface.
 */
public class LoginPanel {
    
    // Background is now handled by BackgroundPanel, so we don't need this
    private static final String PREFS_NODE = "wellco_login";
    private static final String PREFS_USERNAME = "username";
    private static final String PREFS_PASSWORD = "password";
    private static final String PREFS_REMEMBER = "remember";
    
    private DatabaseManager dbManager;
    private CardLayout rootLayout;
    private JPanel rootPanel;
    private CardLayout contentLayout;
    private JPanel contentCards;
    private Preferences prefs;
    
    public LoginPanel(DatabaseManager dbManager, CardLayout rootLayout, JPanel rootPanel, 
                     java.util.function.Consumer<UserData> onLoginSuccess) {
        this.dbManager = dbManager;
        this.rootLayout = rootLayout;
        this.rootPanel = rootPanel;
        this.prefs = Preferences.userRoot().node(PREFS_NODE);
    }
    
    // Set content cards reference (called after contentCards is created)
    public void setContentCards(CardLayout contentLayout, JPanel contentCards) {
        this.contentLayout = contentLayout;
        this.contentCards = contentCards;
    }
    
    public JPanel createPanel(java.util.function.Consumer<UserData> onLoginSuccess) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false); // Transparent to show background gradient

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("WellCo Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE); // White text for contrast

        JLabel userLbl = new JLabel("Username:");
        userLbl.setForeground(Color.WHITE);
        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(Color.WHITE);

        // Fixed size text fields - prevent expansion
        int fieldWidth = 250;
        int fieldHeight = 25;
        JTextField tfUser = new JTextField(20);
        tfUser.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        tfUser.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
        tfUser.setMaximumSize(new Dimension(fieldWidth, fieldHeight));
        
        JPasswordField tfPass = new JPasswordField(20);
        tfPass.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        tfPass.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
        tfPass.setMaximumSize(new Dimension(fieldWidth, fieldHeight));
        
        // Remember Me checkbox
        JCheckBox rememberMeCheckbox = new JCheckBox("Remember Me");
        rememberMeCheckbox.setForeground(Color.WHITE);
        rememberMeCheckbox.setOpaque(false);
        
        // Load saved credentials if Remember Me was checked
        boolean rememberMe = prefs.getBoolean(PREFS_REMEMBER, false);
        if (rememberMe) {
            String savedUsername = prefs.get(PREFS_USERNAME, "");
            String savedPassword = prefs.get(PREFS_PASSWORD, "");
            if (!savedUsername.isEmpty()) {
                tfUser.setText(savedUsername);
                tfPass.setText(savedPassword);
                rememberMeCheckbox.setSelected(true);
            }
        }
        
        // Message label for login errors
        JLabel messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> {
            String username = tfUser.getText().trim();
            String password = new String(tfPass.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please fill in all fields.");
                return;
            }
            
            // Login works with both SQL and ArrayList (works offline)
            UserData user = dbManager.loginUser(username, password);
            if (user != null) {
                // Handle Remember Me functionality
                if (rememberMeCheckbox.isSelected()) {
                    // Save login info
                    prefs.put(PREFS_USERNAME, username);
                    prefs.put(PREFS_PASSWORD, password);
                    prefs.putBoolean(PREFS_REMEMBER, true);
                } else {
                    // Clear login info and fields
                    prefs.remove(PREFS_USERNAME);
                    prefs.remove(PREFS_PASSWORD);
                    prefs.putBoolean(PREFS_REMEMBER, false);
                    tfUser.setText("");
                    tfPass.setText("");
                }
                
                messageLabel.setText(" ");
                onLoginSuccess.accept(user);
                // Navigation is handled by the callback in MainFrame
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        });

        // Signup link
        JLabel signupLabel = new JLabel("<html>Don't have an account? <a href=''>Register here</a></html>");
        signupLabel.setForeground(Color.WHITE);
        signupLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Navigate to SIGNUP in contentCards
                if (contentLayout != null && contentCards != null) {
                    // Ensure we're on PUBLIC panel first
                    rootLayout.show(rootPanel, "PUBLIC");
                    // Then show SIGNUP in contentCards
                    SwingUtilities.invokeLater(() -> {
                        contentLayout.show(contentCards, "SIGNUP");
                    });
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                signupLabel.setForeground(new Color(200, 220, 255));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                signupLabel.setForeground(Color.WHITE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; p.add(title, gbc);
        gbc.gridy = 1; p.add(userLbl, gbc);
        gbc.gridy = 2; p.add(tfUser, gbc);
        gbc.gridy = 3; p.add(passLbl, gbc);
        gbc.gridy = 4; p.add(tfPass, gbc);
        gbc.gridy = 5; p.add(rememberMeCheckbox, gbc);
        gbc.gridy = 6; p.add(messageLabel, gbc);
        gbc.gridy = 7; p.add(loginBtn, gbc);
        gbc.gridy = 8; p.add(signupLabel, gbc);

        return p;
    }
}

