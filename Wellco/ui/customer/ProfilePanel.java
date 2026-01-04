package ui.customer;

import database.DatabaseManager;
import database.UserData;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * ProfilePanel handles user profile management interface.
 */
public class ProfilePanel {
    
    private DatabaseManager dbManager;
    private java.util.function.Supplier<UserData> currentUserSupplier;
    private JFrame frame;
    private CardLayout rootLayout;
    private JPanel rootPanel;
    private CardLayout contentLayout;
    private JPanel contentCards;
    private Runnable onAccountDeleted;
    
    public ProfilePanel(DatabaseManager dbManager, java.util.function.Supplier<UserData> currentUserSupplier, 
                       JFrame frame, CardLayout rootLayout, JPanel rootPanel,
                       CardLayout contentLayout, JPanel contentCards, Runnable onAccountDeleted) {
        this.dbManager = dbManager;
        this.currentUserSupplier = currentUserSupplier;
        this.frame = frame;
        this.rootLayout = rootLayout;
        this.rootPanel = rootPanel;
        this.contentLayout = contentLayout;
        this.contentCards = contentCards;
        this.onAccountDeleted = onAccountDeleted;
    }
    
    public JPanel createPanel(java.util.function.Consumer<UserData> onUserUpdate) {
        JPanel prof = new JPanel();
        prof.setLayout(new BoxLayout(prof, BoxLayout.Y_AXIS));
        prof.setBorder(new EmptyBorder(20, 40, 40, 40));

        JLabel title = new JLabel("Profile");
        title.setFont(new Font("Arial", Font.BOLD, 22));

        // Get current user from supplier (always gets latest)
        UserData currentUser = currentUserSupplier.get();
        
        // Display user info
        String userName = currentUser != null && currentUser.name != null ? currentUser.name : "";
        String userEmail = currentUser != null ? currentUser.email : "";
        int userAge = currentUser != null && currentUser.age > 0 ? currentUser.age : 20;
        String userRole = currentUser != null && currentUser.role != null ? currentUser.role.trim() : "Customer";
        String username = currentUser != null && currentUser.username != null ? currentUser.username : "";
        
        // Normalize role to proper case
        if (userRole.equalsIgnoreCase("Admin")) {
            userRole = "Admin";
        } else if (userRole.equalsIgnoreCase("Manager")) {
            userRole = "Manager";
        } else {
            userRole = "Customer";
        }

        final int safeAge = (userAge < 1 || userAge > 120) ? 20 : userAge;

        JLabel roleLabel = new JLabel("Role: " + userRole);
        roleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel usernameLabel = new JLabel("Username: " + username);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Fixed size text fields - prevent expansion
        int fieldWidth = 250;
        int fieldHeight = 25;
        
        JTextField tfName = new JTextField(userName, 20);
        tfName.setFont(new Font("Arial", Font.PLAIN, 14));
        tfName.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        tfName.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
        tfName.setMaximumSize(new Dimension(fieldWidth, fieldHeight));

        JTextField tfEmail = new JTextField(userEmail, 20);
        tfEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        tfEmail.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        tfEmail.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
        tfEmail.setMaximumSize(new Dimension(fieldWidth, fieldHeight));

        JSpinner spAge = new JSpinner(new SpinnerNumberModel(safeAge, 1, 120, 1));
        spAge.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton save = new JButton("Save");
        JButton reset = new JButton("Reset");
        JButton reload = new JButton("Reload From DB");
        JButton delete = new JButton("Delete Account");
        delete.setForeground(Color.RED);

        save.setFont(new Font("Arial", Font.PLAIN, 14));
        reset.setFont(new Font("Arial", Font.PLAIN, 14));
        reload.setFont(new Font("Arial", Font.PLAIN, 14));
        delete.setFont(new Font("Arial", Font.PLAIN, 14));

        save.addActionListener(e -> {
            UserData user = currentUserSupplier.get();
            if (user == null || user.username == null) {
                JOptionPane.showMessageDialog(frame, "No user logged in.");
                return;
            }
            String name = tfName.getText();
            String email = tfEmail.getText();
            int age = (Integer) spAge.getValue();
            boolean success = dbManager.saveUserProfile(user.username, name, email, age);
            if (success) {
                // Reload user data
                UserData updated = dbManager.loginUser(user.username, "");
                if (updated != null) {
                    onUserUpdate.accept(updated);
                }
                JOptionPane.showMessageDialog(frame, "Profile Saved.");
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to save profile.");
            }
        });

        reset.addActionListener(e -> {
            tfName.setText(userName);
            tfEmail.setText(userEmail);
            spAge.setValue(safeAge);
        });

        reload.addActionListener(e -> {
            UserData user = currentUserSupplier.get();
            if (user != null && user.username != null) {
                UserData reloaded = dbManager.loginUser(user.username, "");
                if (reloaded != null) {
                    // Normalize role
                    String normalizedRole = reloaded.role != null ? reloaded.role.trim() : "Customer";
                    if (normalizedRole.equalsIgnoreCase("Admin")) {
                        normalizedRole = "Admin";
                    } else if (normalizedRole.equalsIgnoreCase("Manager")) {
                        normalizedRole = "Manager";
                    } else {
                        normalizedRole = "Customer";
                    }
                    
                    // Create new UserData with normalized role if needed
                    if (!normalizedRole.equals(reloaded.role)) {
                        // Preserve password from original
                        String originalPassword = reloaded.password;
                        reloaded = new UserData(
                            normalizedRole,
                            reloaded.username,
                            reloaded.email,
                            reloaded.createdDate,
                            reloaded.originalUsername,
                            reloaded.name,
                            reloaded.age
                        );
                        reloaded.password = originalPassword; // Preserve password from original
                    }
                    
                    onUserUpdate.accept(reloaded);
                    tfName.setText(reloaded.name != null ? reloaded.name : "");
                    tfEmail.setText(reloaded.email != null ? reloaded.email : "");
                    int newAge = (reloaded.age > 0 && reloaded.age <= 120) ? reloaded.age : 20;
                    spAge.setValue(newAge);
                    roleLabel.setText("Role: " + normalizedRole);
                    usernameLabel.setText("Username: " + (reloaded.username != null ? reloaded.username : ""));
                }
            }
        });

        delete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, 
                "Are you sure you want to delete your account?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                UserData user = currentUserSupplier.get();
                if (user != null && user.username != null) {
                    boolean success = dbManager.deleteUser(user.username);
                    if (success) {
                        onUserUpdate.accept(null); // Clear user
                        
                        // Create and show message dialog with a window listener to navigate after it closes
                        JDialog messageDialog = new JDialog(frame, "Account Deleted", true);
                        messageDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        JLabel message = new JLabel("Account Deleted. Redirecting to login...");
                        message.setBorder(new EmptyBorder(20, 30, 20, 30));
                        messageDialog.add(message);
                        messageDialog.pack();
                        messageDialog.setLocationRelativeTo(frame);
                        
                        // Navigate after dialog closes
                        messageDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                            @Override
                            public void windowClosed(java.awt.event.WindowEvent e) {
                                if (onAccountDeleted != null) {
                                    onAccountDeleted.run();
                                } else {
                                    // Fallback: manual navigation
                                    if (rootLayout != null && rootPanel != null) {
                                        rootLayout.show(rootPanel, "PUBLIC");
                                    }
                                    if (contentLayout != null && contentCards != null) {
                                        contentLayout.show(contentCards, "LOGIN");
                                    }
                                    if (frame != null) {
                                        frame.revalidate();
                                        frame.repaint();
                                    }
                                }
                            }
                        });
                        
                        messageDialog.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Failed to delete account.");
                    }
                }
            }
        });

        prof.add(title);
        prof.add(Box.createVerticalStrut(15));
        prof.add(roleLabel);
        prof.add(Box.createVerticalStrut(5));
        prof.add(usernameLabel);
        prof.add(Box.createVerticalStrut(15));
        prof.add(new JLabel("Name:")); prof.add(tfName);
        prof.add(Box.createVerticalStrut(5));
        prof.add(new JLabel("Email:")); prof.add(tfEmail);
        prof.add(Box.createVerticalStrut(5));
        prof.add(new JLabel("Age:")); prof.add(spAge);
        prof.add(Box.createVerticalStrut(15));
        prof.add(save);
        prof.add(reset);
        prof.add(reload);
        prof.add(delete);

        return prof;
    }
}

