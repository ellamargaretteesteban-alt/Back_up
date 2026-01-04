package ui.customer;

import database.DatabaseManager;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Pill;

/**
 * CustomerInterface handles all customer-related UI components including products.
 */
public class CustomerInterface {
    
    private JFrame frame;
    private DatabaseManager dbManager;
    private List<Pill> pills;
    private JPanel pillsGridPanel;
    private JTextField searchField;
    
    public CustomerInterface(JFrame frame) {
        this.frame = frame;
        this.pills = new ArrayList<>();
    }
    
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    /**
     * Loads pills from database, falls back to seeded data if database is empty
     */
    public void seedPills() {
        // Try to load from database first
        if (dbManager != null) {
            List<Pill> dbPills = dbManager.getAllPills();
            if (dbPills != null && !dbPills.isEmpty()) {
                pills.addAll(dbPills);
                return; // Use database pills
            }
        }
        
        // Fallback to seeded data if database is empty or unavailable
        seedDefaultPills();
    }
    
    /**
     * Seeds the pills list with initial data (fallback)
     */
    private void seedDefaultPills() {
        pills.add(new Pill("Biogesic (Paracetamol)",
                "A popular OTC tablet for relieving fever and mild pain.",
                "Commonly used for fever and headache relief."));

        pills.add(new Pill("Neozep",
                "Cold medicine used for runny nose, sneezing, and congestion.",
                "Often used for cold symptoms."));

        pills.add(new Pill("Bioflu",
                "All-in-one relief for flu symptoms such as body pain, fever, and clogged nose.",
                "Common for flu-like discomfort."));

        pills.add(new Pill("Alaxan FR",
                "A combination tablet commonly used for body pains and aches.",
                "Often used for muscle pain relief."));

        pills.add(new Pill("Kremil-S",
                "A chewable tablet commonly taken for hyperacidity.",
                "Used for stomach discomfort related to acidity."));

        pills.add(new Pill("Decolgen",
                "Cold medicine used for nasal congestion and headaches.",
                "Used for colds and stuffy nose symptoms."));

        pills.add(new Pill("Solmux",
                "A well-known expectorant brand in the Philippines.",
                "Used to help loosen mucus in cough with phlegm."));

        pills.add(new Pill("Tuseran Forte",
                "Cold-and-cough relief tablet for cough and nasal congestion.",
                "Used for bothersome cough and cold symptoms."));

        pills.add(new Pill("Enervon",
                "A multivitamin tablet known for boosting energy.",
                "General energy and immunity support."));

        pills.add(new Pill("Myra-E",
                "Vitamin E supplement for skin and cell protection.",
                "Commonly used for skin wellness."));

        pills.add(new Pill("Robitussin",
                "A cough syrup brand used for cough control.",
                "Used for different types of cough depending on variant."));

        pills.add(new Pill("Ventolin (Salbutamol)",
                "Bronchodilator typically used for asthma relief.",
                "Used for breathing comfort."));

        pills.add(new Pill("Strepsils",
                "Lozenges used for sore throat soothing.",
                "Used for throat discomfort."));

        pills.add(new Pill("Ascof Lagundi",
                "Lagundi-based natural cough remedy.",
                "Used for cough and mild asthma symptoms."));

        pills.add(new Pill("Cetirizine",
                "Antihistamine for allergy relief.",
                "Used for runny nose, itching, and allergic reactions."));

        pills.add(new Pill("Loratadine",
                "Non-drowsy antihistamine for allergies.",
                "Used for allergy-related symptoms."));

        pills.add(new Pill("Diatabs",
                "Anti-diarrheal medicine commonly used in PH.",
                "Used for sudden diarrhea relief."));

        pills.add(new Pill("Imodium",
                "OTC anti-diarrhea capsule.",
                "Used to control loose bowel movement."));

        pills.add(new Pill("Buscopan",
                "Tablet used for stomach and intestinal cramps.",
                "Used for abdominal discomfort."));

        pills.add(new Pill("Motilium",
                "Aids with digestion-related discomfort.",
                "Used for bloating and slow digestion."));

        pills.add(new Pill("Gaviscon",
                "Chewable tablet/liquid for heartburn and indigestion.",
                "Used for acid reflux relief."));

        pills.add(new Pill("Mefenamic Acid",
                "Pain reliever used for various types of moderate pain.",
                "Commonly used for headache or body pains."));

        pills.add(new Pill("Alvedon",
                "Paracetamol brand used for pain and fever relief.",
                "Used similarly to paracetamol tablets."));

        pills.add(new Pill("Tempra",
                "Paracetamol brand popular for fever relief.",
                "Used for mild pain and fever."));

        pills.add(new Pill("Flanax",
                "Pain reliever/anti-inflammatory tablet.",
                "Often used for muscle pain and inflammation."));

        pills.add(new Pill("Ibuprofen (Medicol Advance)",
                "Anti-inflammatory and pain relief medicine.",
                "Used for headaches and body aches."));

        pills.add(new Pill("RiteMed Paracetamol",
                "Affordable paracetamol variant.",
                "Used for fever and mild pain."));

        pills.add(new Pill("Dolfenal",
                "A strong pain reliever for moderate to severe pain.",
                "Used commonly for headaches or dysmenorrhea."));

        pills.add(new Pill("Alaxan",
                "Pain relief tablet for body aches.",
                "Used for muscle and joint discomfort."));

        pills.add(new Pill("Revicon",
                "Multivitamin for energy and body resistance.",
                "Used for general wellness support."));
    }
    
    /**
     * Refreshes pills list from database
     */
    public void refreshPillsFromDatabase() {
        if (dbManager != null) {
            pills.clear();
            List<Pill> dbPills = dbManager.getAllPills();
            if (dbPills != null && !dbPills.isEmpty()) {
                pills.addAll(dbPills);
            } else {
                seedDefaultPills(); // Fallback to default if database empty
            }
            if (pillsGridPanel != null) {
                updatePillsGrid(pills);
            }
        }
    }
    
    /**
     * Creates the home panel with products
     */
    public JPanel createHomePanel() {
        JPanel home = new JPanel(new BorderLayout());
        home.setOpaque(false); // Transparent to show background gradient
        home.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel searchLabel = new JLabel("Search Medication: ");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(Color.WHITE);
        
        searchField = new JTextField(25);
        searchField.setPreferredSize(new Dimension(300, 30));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterPills(searchField.getText());
            }
        });
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        pillsGridPanel = new JPanel(new GridBagLayout());
        pillsGridPanel.setOpaque(false); // Transparent to show background
        updatePillsGrid(pills);

        JScrollPane sp = new JScrollPane(
                pillsGridPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        home.add(searchPanel, BorderLayout.NORTH);
        home.add(sp, BorderLayout.CENTER);
        return home;
    }
    
    /**
     * Updates the pills grid display
     */
    public void updatePillsGrid(List<Pill> list) {
        pillsGridPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0, col = 0;

        for (Pill p : list) {
            gbc.gridx = col;
            gbc.gridy = row;
            pillsGridPanel.add(buildPillCard(p), gbc);

            col++;
            if (col > 2) {   // 3 per row
                col = 0;
                row++;
            }
        }

        pillsGridPanel.revalidate();
        pillsGridPanel.repaint();
    }
    
    /**
     * Shows product details dialog
     */
    private void showProductDetails(Pill p) {
        JDialog dialog = new JDialog(frame, p.name, true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel(p.name, SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Description area
        JTextArea desc = new JTextArea("Description:\n" + p.description +
                "\n\nRecommendations:\n" + p.recommendation);
        desc.setFont(new Font("Arial", Font.PLAIN, 16));
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane scroll = new JScrollPane(desc);

        // Close Button
        JButton close = new JButton("Close");
        close.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel();
        bottom.add(close);

        dialog.add(title, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
    
    /**
     * Builds a pill card component
     */
    private JPanel buildPillCard(Pill p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(250, 150));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        card.setBackground(new Color(240, 255, 255));

        JLabel name = new JLabel(p.name);
        name.setFont(new Font("Arial", Font.BOLD, 16));
        name.setBorder(new EmptyBorder(10, 10, 0, 10));

        JTextArea desc = new JTextArea(p.description);
        desc.setWrapStyleWord(true);
        desc.setLineWrap(true);
        desc.setEditable(false);
        desc.setBorder(new EmptyBorder(10, 10, 10, 10));

        card.add(name, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);

        // Click card → open product details window
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProductDetails(p);
            }
        });

        return card;
    }
    
    /**
     * Filters pills based on search query
     */
    public void filterPills(String q) {
        if (q.trim().isEmpty()) {
            updatePillsGrid(pills);
            return;
        }

        List<Pill> filtered = new ArrayList<>();
        String s = q.toLowerCase();

        for (Pill p : pills) {
            if (p.name.toLowerCase().contains(s) || p.description.toLowerCase().contains(s)) {
                filtered.add(p);
            }
        }

        updatePillsGrid(filtered);
    }
    
    /**
     * Gets the search field for top bar integration
     */
    public JTextField getSearchField() {
        if (searchField == null) {
            // Fixed size search field - prevent expansion
            int fieldWidth = 300;
            int fieldHeight = 25;
            searchField = new JTextField(20);
            searchField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
            searchField.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
            searchField.setMaximumSize(new Dimension(fieldWidth, fieldHeight));
            searchField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    filterPills(searchField.getText());
                }
            });
        }
        return searchField;
    }
    
    /**
     * Gets the pills list
     */
    public List<Pill> getPills() {
        return pills;
    }
}

