package ui.common;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * ServicesPanel displays WellCo services with search functionality.
 */
public class ServicesPanel extends JPanel {

    private JPanel servicesGrid;
    private JTextField searchField;
    private List<String[]> allServices;
    private JPanel gridContainer;

    public ServicesPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Main container with padding - using BorderLayout for proper structure
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 60));

        // Top section with title and search
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // Title
        JLabel titleLabel = new JLabel("Our Services", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        topSection.add(titleLabel);

        // Beautiful search bar
        JPanel searchPanel = createSearchBar();
        searchPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        topSection.add(searchPanel);

        mainPanel.add(topSection, BorderLayout.NORTH);

        // Services grid panel
        gridContainer = new JPanel(new BorderLayout());
        gridContainer.setOpaque(false);
        
        servicesGrid = new JPanel(new GridLayout(0, 2, 30, 25));
        servicesGrid.setOpaque(false);
        servicesGrid.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Service items
        allServices = new ArrayList<>();
        allServices.add(new String[]{"💊 Medication Management", "Track and manage your medications with reminders and dosage information"});
        allServices.add(new String[]{"📋 Health Records", "Maintain comprehensive health records and medical history"});
        allServices.add(new String[]{"👤 Patient Profiles", "Secure user profiles with personalized health information"});
        allServices.add(new String[]{"🔔 Health Reminders", "Get timely reminders for medications and health checkups"});
        allServices.add(new String[]{"📊 Health Analytics", "View your health trends and medication adherence statistics"});
        allServices.add(new String[]{"🔐 Secure Data Storage", "Your health information is securely stored and protected"});
        allServices.add(new String[]{"📱 Easy Access", "Access your health information anytime, anywhere"});
        allServices.add(new String[]{"👨‍⚕️ Healthcare Integration", "Connect with healthcare providers for better care coordination"});

        updateServicesGrid(allServices);

        // Scroll pane for services
        JScrollPane scrollPane = new JScrollPane(servicesGrid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        gridContainer.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(gridContainer, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSearchBar() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchPanel.setOpaque(false);
        
        // "Search here:" label
        JLabel searchLabel = new JLabel("Search here:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(Color.WHITE);
        searchPanel.add(searchLabel);
        
        // Search field - fixed size, about half the tab width (500px)
        int searchWidth = 500;
        int searchHeight = 30;
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Set fixed size - prevent expansion
        searchField.setPreferredSize(new Dimension(searchWidth, searchHeight));
        searchField.setMinimumSize(new Dimension(searchWidth, searchHeight));
        searchField.setMaximumSize(new Dimension(searchWidth, searchHeight));
        // Enable horizontal scrolling for long text
        searchField.setHorizontalAlignment(JTextField.LEFT);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterServices(searchField.getText());
            }
        });
        searchPanel.add(searchField);
        
        // Beautiful search button
        JButton searchBtn = new JButton("🔍 Search");
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchBtn.setPreferredSize(new Dimension(120, 30));
        searchBtn.setMinimumSize(new Dimension(120, 30));
        searchBtn.setMaximumSize(new Dimension(120, 30));
        searchBtn.setBackground(new Color(64, 149, 255));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorderPainted(false);
        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        searchBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                searchBtn.setBackground(new Color(84, 169, 255));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                searchBtn.setBackground(new Color(64, 149, 255));
            }
        });
        
        searchBtn.addActionListener(e -> filterServices(searchField.getText()));
        searchPanel.add(searchBtn);
        
        return searchPanel;
    }
    
    private void filterServices(String query) {
        if (query == null) query = "";
        String searchQuery = query.trim().toLowerCase();
        
        List<String[]> filtered;
        if (searchQuery.isEmpty()) {
            filtered = new ArrayList<>(allServices);
        } else {
            filtered = new ArrayList<>();
            for (String[] service : allServices) {
                String title = service[0].toLowerCase();
                String desc = service[1].toLowerCase();
                if (title.contains(searchQuery) || desc.contains(searchQuery)) {
                    filtered.add(service);
                }
            }
        }
        
        updateServicesGrid(filtered);
    }
    
    private void updateServicesGrid(List<String[]> services) {
        servicesGrid.removeAll();
        
        for (String[] service : services) {
            JPanel serviceCard = createServiceCard(service[0], service[1]);
            servicesGrid.add(serviceCard);
        }
        
        // If no results, show message
        if (services.isEmpty()) {
            JLabel noResults = new JLabel("No services found matching your search.");
            noResults.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            noResults.setForeground(new Color(255, 255, 200));
            noResults.setHorizontalAlignment(SwingConstants.CENTER);
            servicesGrid.add(noResults);
            // Add empty panel to maintain grid layout
            servicesGrid.add(new JPanel());
        }
        
        servicesGrid.revalidate();
        servicesGrid.repaint();
    }

    private JPanel createServiceCard(String title, String description) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(200, 220, 255));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Description
        JTextArea descArea = new JTextArea(description);
        descArea.setOpaque(false);
        descArea.setEditable(false);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setForeground(Color.WHITE);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(descArea);

        return card;
    }
}















