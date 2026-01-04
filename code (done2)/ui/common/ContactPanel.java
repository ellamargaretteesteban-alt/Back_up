package ui.common;

import database.DatabaseManager;
import database.UserData;
import java.awt.*;
import java.util.List;
import javax.swing.*;

/**
 * ContactPanel allows users to send messages to Admin/Manager users.
 */
public class ContactPanel {
    
    private DatabaseManager dbManager;
    private JFrame frame;
    
    public ContactPanel(DatabaseManager dbManager, JFrame frame) {
        this.dbManager = dbManager;
        this.frame = frame;
    }
    
    public JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);

        // Main container with spacing
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        // Title "CONTACT US" - moved up with space above and bigger font
        JLabel titleLabel = new JLabel("CONTACT US", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(Box.createVerticalStrut(20), BorderLayout.NORTH);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        wrap.add(titlePanel, BorderLayout.NORTH);

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Name field
        JLabel n = new JLabel("Name:");
        n.setForeground(Color.WHITE);
        n.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        form.add(n, gbc);

        // Fixed size text fields - prevent expansion
        int fieldWidth = 300;
        int fieldHeight = 25;
        int textAreaWidth = 300;
        int textAreaHeight = 150;
        
        JTextField nameField = new JTextField(20);
        nameField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        nameField.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
        nameField.setMaximumSize(new Dimension(fieldWidth, fieldHeight));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        form.add(nameField, gbc);

        // Email field
        JLabel e = new JLabel("Email:");
        e.setForeground(Color.WHITE);
        e.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        form.add(e, gbc);

        JTextField emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        emailField.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
        emailField.setMaximumSize(new Dimension(fieldWidth, fieldHeight));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        form.add(emailField, gbc);

        // Message field
        JLabel m = new JLabel("Message:");
        m.setForeground(Color.WHITE);
        m.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(m, gbc);

        JTextArea messageArea = new JTextArea(8, 20);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        // Fixed size for text area
        messageArea.setPreferredSize(new Dimension(textAreaWidth, textAreaHeight));
        messageArea.setMinimumSize(new Dimension(textAreaWidth, textAreaHeight));
        messageArea.setMaximumSize(new Dimension(textAreaWidth, textAreaHeight));
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setPreferredSize(new Dimension(textAreaWidth, textAreaHeight));
        messageScroll.setMinimumSize(new Dimension(textAreaWidth, textAreaHeight));
        messageScroll.setMaximumSize(new Dimension(textAreaWidth, textAreaHeight));
        messageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        form.add(messageScroll, gbc);

        wrap.add(form, BorderLayout.CENTER);

        // Status label
        JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setForeground(new Color(0, 200, 0));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        statusLabel.setVisible(false);

        // Button panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        
        JButton send = new JButton("Send");
        send.setPreferredSize(new Dimension(100, 35));
        send.addActionListener(evt -> {
            handleSend(nameField, emailField, messageArea, statusLabel);
        });
        
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonContainer.setOpaque(false);
        buttonContainer.add(send);
        buttonPanel.add(buttonContainer, BorderLayout.CENTER);
        buttonPanel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        buttonPanel.add(statusLabel, BorderLayout.SOUTH);

        wrap.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrap);
        return panel;
    }
    
    private void handleSend(JTextField nameField, JTextField emailField, JTextArea messageArea, JLabel statusLabel) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String message = messageArea.getText().trim();

        // Validate fields
        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            statusLabel.setForeground(new Color(255, 50, 50));
            statusLabel.setText("Please fill up all fields.");
            statusLabel.setVisible(true);

            Timer timer = new Timer(3000, e -> {
                statusLabel.setVisible(false);
            });
            timer.setRepeats(false);
            timer.start();
            return;
        }

        // Check if Admin or Manager users exist
        List<UserData> allUsers = dbManager.getAllUsers();
        boolean hasAdminOrManager = false;
        for (UserData user : allUsers) {
            if ("Admin".equalsIgnoreCase(user.role) || "Manager".equalsIgnoreCase(user.role)) {
                hasAdminOrManager = true;
                break;
            }
        }

        if (!hasAdminOrManager) {
            statusLabel.setForeground(new Color(255, 50, 50));
            statusLabel.setText("No Admin or Manager available to receive messages.");
            statusLabel.setVisible(true);

            Timer timer = new Timer(3000, e -> {
                statusLabel.setVisible(false);
            });
            timer.setRepeats(false);
            timer.start();
            return;
        }

        // Clear fields
        nameField.setText("");
        emailField.setText("");
        messageArea.setText("");

        // Show success message
        statusLabel.setForeground(new Color(0, 200, 0));
        statusLabel.setText("Message sent successfully!");
        statusLabel.setVisible(true);

        // In a real application, you would save this message to a database
        // For now, we just show success
        System.out.println("Contact message from: " + name + " (" + email + ")");
        System.out.println("Message: " + message);
        System.out.println("Sent to Admin/Manager users");

        Timer timer = new Timer(3000, e -> {
            statusLabel.setVisible(false);
        });
        timer.setRepeats(false);
        timer.start();
    }
}















