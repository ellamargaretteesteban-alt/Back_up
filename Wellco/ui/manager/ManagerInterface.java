package ui.manager;

import database.DatabaseManager;
import database.UserData;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.*;

/**
 * ManagerInterface handles all manager-related UI components.
 */
public class ManagerInterface {
    
    private DatabaseManager dbManager;
    private JFrame frame;
    private UserData currentManager;
    private CardLayout managerLayout;
    private JPanel managerCards;
    private ManagerPermission permissions;
    
    public ManagerInterface(DatabaseManager dbManager, JFrame frame, UserData currentManager) {
        this.dbManager = dbManager;
        this.frame = frame;
        this.currentManager = currentManager;
        loadPermissions();
    }
    
    private void loadPermissions() {
        permissions = dbManager.getManagerPermission(currentManager.username);
    }
    
    public JPanel createManagerPanel() {
        managerLayout = new CardLayout();
        managerCards = new JPanel(managerLayout);
        managerCards.setOpaque(false);
        
        // Create different manager panels
        managerCards.add(createDashboardPanel(), "DASHBOARD");
        managerCards.add(createMedicineRequestPanel(), "MEDICINE_REQUEST");
        managerCards.add(createUserRequestPanel(), "USER_REQUEST");
        managerCards.add(createHealthTipRequestPanel(), "HEALTH_TIP_REQUEST");
        managerCards.add(createMyRequestsPanel(), "MY_REQUESTS");
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.add(createManagerSidebar(), BorderLayout.WEST);
        mainPanel.add(managerCards, BorderLayout.CENTER);
        
        // Show dashboard by default
        managerLayout.show(managerCards, "DASHBOARD");
        
        return mainPanel;
    }
    
    private JPanel createManagerSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(new Color(20, 50, 70));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        JLabel title = new JLabel("Manager Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(20));
        
        String[] menuItems = {"Dashboard", "Request Medicine", "Request User", "Request Health Tip", "My Requests"};
        String[] cardNames = {"DASHBOARD", "MEDICINE_REQUEST", "USER_REQUEST", "HEALTH_TIP_REQUEST", "MY_REQUESTS"};
        boolean[] enabled = {
            true, // Dashboard always enabled
            permissions.canRequestMedicine,
            permissions.canRequestUser,
            permissions.canRequestHealthTip,
            true // My Requests always enabled
        };
        
        for (int i = 0; i < menuItems.length; i++) {
            final int index = i; // Make a final copy for lambda
            JButton btn = new JButton(menuItems[i]);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            btn.setBackground(new Color(30, 70, 100));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setEnabled(enabled[i]);
            
            if (!enabled[i]) {
                btn.setToolTipText("This function is disabled by Admin");
                btn.setBackground(new Color(50, 50, 50));
            }
            
            final String cardName = cardNames[i];
            btn.addActionListener(e -> {
                if (enabled[index]) {
                    managerLayout.show(managerCards, cardName);
                    if (cardName.equals("MY_REQUESTS") && refreshMyRequests != null) {
                        refreshMyRequests.run();
                    }
                }
            });
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(5));
        }
        
        return sidebar;
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Manager Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(new JLabel("<html><b>Welcome, " + currentManager.username + "!</b></html>"), gbc);
        gbc.gridy = 1;
        infoPanel.add(new JLabel("Your Permissions:"), gbc);
        gbc.gridy = 2;
        infoPanel.add(new JLabel(permissions.canRequestMedicine ? "✓ Can Request Medicine" : "✗ Cannot Request Medicine"), gbc);
        gbc.gridy = 3;
        infoPanel.add(new JLabel(permissions.canRequestUser ? "✓ Can Request User Changes" : "✗ Cannot Request User Changes"), gbc);
        gbc.gridy = 4;
        infoPanel.add(new JLabel(permissions.canRequestHealthTip ? "✓ Can Request Health Tips" : "✗ Cannot Request Health Tips"), gbc);
        
        // Get pending requests count
        List<Request> myRequests = dbManager.getAllRequests();
        long pendingCount = myRequests.stream()
            .filter(r -> r.requesterUsername != null && r.requesterUsername.equals(currentManager.username))
            .filter(r -> "PENDING".equals(r.status))
            .count();
        
        gbc.gridy = 5;
        infoPanel.add(new JLabel("<html><br><b>Pending Requests: " + pendingCount + "</b></html>"), gbc);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMedicineRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        if (!permissions.canRequestMedicine) {
            JLabel disabledLabel = new JLabel("This function is disabled by Admin.", SwingConstants.CENTER);
            disabledLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            disabledLabel.setForeground(Color.RED);
            panel.add(disabledLabel, BorderLayout.CENTER);
            return panel;
        }
        
        JLabel title = new JLabel("Request Medicine Change", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Request type
        JLabel typeLabel = new JLabel("Request Type:");
        typeLabel.setForeground(Color.WHITE);
        String[] requestTypes = {"ADD_MEDICINE", "EDIT_MEDICINE", "REMOVE_MEDICINE"};
        JComboBox<String> typeCombo = new JComboBox<>(requestTypes);
        typeCombo.setPreferredSize(new Dimension(200, 30));
        
        // Medicine name (for edit/remove)
        JLabel nameLabel = new JLabel("Medicine Name:");
        nameLabel.setForeground(Color.WHITE);
        JTextField nameField = new JTextField(30);
        nameField.setPreferredSize(new Dimension(300, 30));
        
        // Description
        JLabel descLabel = new JLabel("Description:");
        descLabel.setForeground(Color.WHITE);
        JTextArea descArea = new JTextArea(5, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        
        // Recommendation
        JLabel recLabel = new JLabel("Recommendation:");
        recLabel.setForeground(Color.WHITE);
        JTextArea recArea = new JTextArea(5, 30);
        recArea.setLineWrap(true);
        recArea.setWrapStyleWord(true);
        
        // Details text area for request details
        JLabel detailsLabel = new JLabel("Request Details (JSON format):");
        detailsLabel.setForeground(Color.WHITE);
        JTextArea detailsArea = new JTextArea(8, 40);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Update details when fields change
        ActionListener updateDetails = e -> {
            String type = (String) typeCombo.getSelectedItem();
            String name = nameField.getText().trim();
            String desc = descArea.getText().trim();
            String rec = recArea.getText().trim();
            
            StringBuilder details = new StringBuilder();
            details.append("{\n");
            details.append("  \"type\": \"").append(type).append("\",\n");
            if (!name.isEmpty()) details.append("  \"name\": \"").append(name).append("\",\n");
            if (!desc.isEmpty()) details.append("  \"description\": \"").append(desc).append("\",\n");
            if (!rec.isEmpty()) details.append("  \"recommendation\": \"").append(rec).append("\"\n");
            details.append("}");
            
            detailsArea.setText(details.toString());
        };
        
        typeCombo.addActionListener(updateDetails);
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateDetails.actionPerformed(null);
            }
        });
        descArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateDetails.actionPerformed(null);
            }
        });
        recArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateDetails.actionPerformed(null);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(typeLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(typeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(descLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(descArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(recLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(recArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(detailsLabel, gbc);
        gbc.gridy = 5;
        formPanel.add(new JScrollPane(detailsArea), gbc);
        
        JButton submitBtn = new JButton("Submit Request");
        submitBtn.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            String details = detailsArea.getText().trim();
            
            if (details.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in request details.");
                return;
            }
            
            Request request = new Request(type, currentManager.username, details);
            boolean success = dbManager.createRequest(request);
            if (success) {
                JOptionPane.showMessageDialog(frame, "Request submitted successfully!");
                nameField.setText("");
                descArea.setText("");
                recArea.setText("");
                detailsArea.setText("");
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to submit request.");
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitBtn);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createUserRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        if (!permissions.canRequestUser) {
            JLabel disabledLabel = new JLabel("This function is disabled by Admin.", SwingConstants.CENTER);
            disabledLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            disabledLabel.setForeground(Color.RED);
            panel.add(disabledLabel, BorderLayout.CENTER);
            return panel;
        }
        
        JLabel title = new JLabel("Request User Change", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Request type
        JLabel typeLabel = new JLabel("Request Type:");
        typeLabel.setForeground(Color.WHITE);
        String[] requestTypes = {"ADD_USER", "EDIT_USER", "REMOVE_USER"};
        JComboBox<String> typeCombo = new JComboBox<>(requestTypes);
        typeCombo.setPreferredSize(new Dimension(200, 30));
        
        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        JTextField usernameField = new JTextField(30);
        usernameField.setPreferredSize(new Dimension(300, 30));
        
        // Name
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(Color.WHITE);
        JTextField nameField = new JTextField(30);
        nameField.setPreferredSize(new Dimension(300, 30));
        
        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(Color.WHITE);
        JTextField emailField = new JTextField(30);
        emailField.setPreferredSize(new Dimension(300, 30));
        
        // Age
        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setForeground(Color.WHITE);
        JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 120, 1));
        
        // Details
        JLabel detailsLabel = new JLabel("Request Details:");
        detailsLabel.setForeground(Color.WHITE);
        JTextArea detailsArea = new JTextArea(8, 40);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailsArea.setEditable(false);
        
        // Update details
        ActionListener updateDetails = e -> {
            String type = (String) typeCombo.getSelectedItem();
            String username = usernameField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            int age = (Integer) ageSpinner.getValue();
            
            StringBuilder details = new StringBuilder();
            details.append("{\n");
            details.append("  \"type\": \"").append(type).append("\",\n");
            if (!username.isEmpty()) details.append("  \"username\": \"").append(username).append("\",\n");
            if (!name.isEmpty()) details.append("  \"name\": \"").append(name).append("\",\n");
            if (!email.isEmpty()) details.append("  \"email\": \"").append(email).append("\",\n");
            details.append("  \"age\": ").append(age).append("\n");
            details.append("}");
            
            detailsArea.setText(details.toString());
        };
        
        typeCombo.addActionListener(updateDetails);
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateDetails.actionPerformed(null);
            }
        });
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateDetails.actionPerformed(null);
            }
        });
        emailField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateDetails.actionPerformed(null);
            }
        });
        ageSpinner.addChangeListener(e -> updateDetails.actionPerformed(null));
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(typeLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(typeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(ageLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ageSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(detailsLabel, gbc);
        gbc.gridy = 6;
        formPanel.add(new JScrollPane(detailsArea), gbc);
        
        JButton submitBtn = new JButton("Submit Request");
        submitBtn.addActionListener(e -> {
            String details = detailsArea.getText().trim();
            if (details.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in request details.");
                return;
            }
            
            String type = (String) typeCombo.getSelectedItem();
            Request request = new Request(type, currentManager.username, details);
            boolean success = dbManager.createRequest(request);
            if (success) {
                JOptionPane.showMessageDialog(frame, "Request submitted successfully!");
                usernameField.setText("");
                nameField.setText("");
                emailField.setText("");
                ageSpinner.setValue(20);
                detailsArea.setText("");
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to submit request.");
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitBtn);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createHealthTipRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        if (!permissions.canRequestHealthTip) {
            JLabel disabledLabel = new JLabel("This function is disabled by Admin.", SwingConstants.CENTER);
            disabledLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            disabledLabel.setForeground(Color.RED);
            panel.add(disabledLabel, BorderLayout.CENTER);
            return panel;
        }
        
        JLabel title = new JLabel("Request Health Tip Change", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Request type
        JLabel typeLabel = new JLabel("Request Type:");
        typeLabel.setForeground(Color.WHITE);
        String[] requestTypes = {"ADD_TIP", "EDIT_TIP", "REMOVE_TIP"};
        JComboBox<String> typeCombo = new JComboBox<>(requestTypes);
        typeCombo.setPreferredSize(new Dimension(200, 30));
        
        // Tip ID (for edit/remove)
        JLabel idLabel = new JLabel("Tip ID (for Edit/Remove):");
        idLabel.setForeground(Color.WHITE);
        JTextField idField = new JTextField(30);
        idField.setPreferredSize(new Dimension(300, 30));
        
        // Title
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setForeground(Color.WHITE);
        JTextField titleField = new JTextField(30);
        titleField.setPreferredSize(new Dimension(300, 30));
        
        // Content
        JLabel contentLabel = new JLabel("Content:");
        contentLabel.setForeground(Color.WHITE);
        JTextArea contentArea = new JTextArea(5, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        
        // Link Source (MANDATORY)
        JLabel linkLabel = new JLabel("Link Source (MANDATORY):");
        linkLabel.setForeground(Color.RED);
        linkLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField linkField = new JTextField(30);
        linkField.setPreferredSize(new Dimension(300, 30));
        
        // Details
        JLabel detailsLabel = new JLabel("Request Details:");
        detailsLabel.setForeground(Color.WHITE);
        JTextArea detailsArea = new JTextArea(8, 40);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailsArea.setEditable(false);
        
        // Update details
        ActionListener updateDetails = e -> {
            String type = (String) typeCombo.getSelectedItem();
            String id = idField.getText().trim();
            String titleText = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String link = linkField.getText().trim();
            
            StringBuilder details = new StringBuilder();
            details.append("{\n");
            details.append("  \"type\": \"").append(type).append("\",\n");
            if (!id.isEmpty()) details.append("  \"id\": ").append(id).append(",\n");
            if (!titleText.isEmpty()) details.append("  \"title\": \"").append(titleText).append("\",\n");
            if (!content.isEmpty()) details.append("  \"content\": \"").append(content).append("\",\n");
            if (!link.isEmpty()) details.append("  \"linkSource\": \"").append(link).append("\"\n");
            details.append("}");
            
            detailsArea.setText(details.toString());
        };
        
        typeCombo.addActionListener(updateDetails);
        idField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateDetails.actionPerformed(null);
            }
        });
        titleField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateDetails.actionPerformed(null);
            }
        });
        contentArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateDetails.actionPerformed(null);
            }
        });
        linkField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateDetails.actionPerformed(null);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(typeLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(typeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(idLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(idField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(titleLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(contentLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(contentArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(linkLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(linkField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(detailsLabel, gbc);
        gbc.gridy = 6;
        formPanel.add(new JScrollPane(detailsArea), gbc);
        
        JButton submitBtn = new JButton("Submit Request");
        submitBtn.addActionListener(e -> {
            String link = linkField.getText().trim();
            if (link.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Link Source is MANDATORY! Please provide a link source.");
                return;
            }
            
            String details = detailsArea.getText().trim();
            if (details.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in request details.");
                return;
            }
            
            String type = (String) typeCombo.getSelectedItem();
            Request request = new Request(type, currentManager.username, details);
            boolean success = dbManager.createRequest(request);
            if (success) {
                JOptionPane.showMessageDialog(frame, "Request submitted successfully!");
                idField.setText("");
                titleField.setText("");
                contentArea.setText("");
                linkField.setText("");
                detailsArea.setText("");
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to submit request.");
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitBtn);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createMyRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("My Requests", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        // Request table
        String[] columns = {"ID", "Type", "Status", "Created Date", "Processed Date"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable requestTable = new JTable(tableModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.setRowHeight(25);
        requestTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        requestTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        requestTable.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setPreferredSize(new Dimension(700, 400));
        
        JButton viewBtn = new JButton("View Details");
        viewBtn.addActionListener(e -> {
            int selectedRow = requestTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a request to view.");
                return;
            }
            
            int requestId = (Integer) tableModel.getValueAt(selectedRow, 0);
            List<Request> allRequests = dbManager.getAllRequests();
            Request req = allRequests.stream()
                .filter(r -> r.id == requestId)
                .findFirst()
                .orElse(null);
            
            if (req != null) {
                JDialog dialog = new JDialog(frame, "Request Details", true);
                dialog.setSize(600, 400);
                dialog.setLocationRelativeTo(frame);
                
                JPanel detailPanel = new JPanel(new BorderLayout());
                detailPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
                
                JTextArea detailsArea = new JTextArea(req.details != null ? req.details : "No details");
                detailsArea.setEditable(false);
                detailsArea.setLineWrap(true);
                detailsArea.setWrapStyleWord(true);
                
                JLabel infoLabel = new JLabel("<html><b>Type:</b> " + req.type + "<br>" +
                                             "<b>Status:</b> " + req.status + "<br>" +
                                             "<b>Created:</b> " + (req.createdDate != null ? req.createdDate.toString() : "") + "<br>" +
                                             "<b>Processed By:</b> " + (req.processedBy != null ? req.processedBy : "N/A") + "</html>");
                
                detailPanel.add(infoLabel, BorderLayout.NORTH);
                detailPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
                
                JButton closeBtn = new JButton("Close");
                closeBtn.addActionListener(evt -> dialog.dispose());
                JPanel btnPanel = new JPanel(new FlowLayout());
                btnPanel.add(closeBtn);
                detailPanel.add(btnPanel, BorderLayout.SOUTH);
                
                dialog.add(detailPanel);
                dialog.setVisible(true);
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewBtn);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Store table model reference for refresh
        refreshMyRequests = () -> {
            List<Request> allRequests = dbManager.getAllRequests();
            tableModel.setRowCount(0);
            for (Request req : allRequests) {
                if (req.requesterUsername != null && req.requesterUsername.equals(currentManager.username)) {
                    Object[] row = {
                        req.id,
                        req.type != null ? req.type : "",
                        req.status != null ? req.status : "PENDING",
                        req.createdDate != null ? req.createdDate.toString().substring(0, 16) : "",
                        req.processedDate != null ? req.processedDate.toString().substring(0, 16) : "N/A"
                    };
                    tableModel.addRow(row);
                }
            }
        };
        
        refreshMyRequests.run();
        
        return panel;
    }
    
    private Runnable refreshMyRequests;
}












