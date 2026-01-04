package ui.customer;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * HealthTipsPanel provides health tips, FAQs, and information about common illnesses
 * with verified links to health organizations, research, and medical sources.
 */
public class HealthTipsPanel extends JPanel {

    private JPanel mainContent;
    private JScrollPane scrollPane;
    private JTextField searchField;
    
    // Data storage for filtering
    private String[][] tips;
    private String[][] faqs;
    private String[][][] illnesses;
    
    private JPanel healthTipsSection;
    private JPanel faqSection;
    private JPanel illnessesSection;

    public HealthTipsPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(30, 60, 30, 60));

        // Initialize data
        initializeData();

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel searchLabel = new JLabel("Search Health Tips: ");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(Color.WHITE);
        
        searchField = new JTextField(25);
        searchField.setPreferredSize(new Dimension(300, 30));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterContent(searchField.getText());
            }
        });
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        // Main scrollable container
        mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // Title
        JLabel titleLabel = new JLabel("Health Tips & Information", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        mainContent.add(titleLabel);

        // Create sections
        rebuildContent();

        // Scroll pane
        scrollPane = new JScrollPane(mainContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void initializeData() {
        tips = new String[][]{
            {"Stay Hydrated", "Drink at least 8 glasses of water daily to maintain proper body function.", "https://www.cdc.gov/nutrition/data-statistics/plain-water-the-healthier-choice.html"},
            {"Regular Exercise", "Aim for at least 150 minutes of moderate-intensity exercise per week.", "https://www.who.int/news-room/fact-sheets/detail/physical-activity"},
            {"Balanced Diet", "Eat a variety of fruits, vegetables, whole grains, and lean proteins.", "https://www.hsph.harvard.edu/nutritionsource/healthy-eating-plate/"},
            {"Adequate Sleep", "Get 7-9 hours of quality sleep each night for optimal health.", "https://www.sleepfoundation.org/how-sleep-works/how-much-sleep-do-we-really-need"},
            {"Stress Management", "Practice relaxation techniques like meditation or deep breathing.", "https://www.nimh.nih.gov/health/publications/stress"}
        };
        
        faqs = new String[][]{
            {"What should I do if I have a fever?", "Rest, stay hydrated, and monitor your temperature. If fever persists or is above 103°F, consult a healthcare provider.", "https://www.mayoclinic.org/diseases-conditions/fever/symptoms-causes/syc-20352759"},
            {"How often should I get a health checkup?", "Adults should have annual checkups. Those with chronic conditions may need more frequent visits.", "https://www.cdc.gov/prevention/index.html"},
            {"What are the signs of dehydration?", "Symptoms include dry mouth, dark urine, fatigue, and dizziness. Drink water immediately if experiencing these.", "https://www.mayoclinic.org/diseases-conditions/dehydration/symptoms-causes/syc-20354086"},
            {"When should I see a doctor?", "Seek medical attention for persistent symptoms, severe pain, difficulty breathing, or any emergency situation.", "https://www.nhs.uk/common-health-questions/nhs-services-and-treatments/when-should-i-see-a-doctor/"},
            {"How can I boost my immune system?", "Maintain a healthy diet, exercise regularly, get adequate sleep, manage stress, and avoid smoking.", "https://www.harvard.edu/in-news/how-to-boost-your-immune-system/"}
        };
        
        illnesses = new String[][][]{
            {{"Common Cold", "Symptoms: Runny nose, sneezing, sore throat"}, {"What to do:", "Rest, stay hydrated, use over-the-counter remedies for symptoms"}, {"Self-care:", "Gargle with salt water, use a humidifier, get plenty of rest"}, {"Verified Sources:", "CDC: https://www.cdc.gov/antibiotic-use/colds.html", "Mayo Clinic: https://www.mayoclinic.org/diseases-conditions/common-cold/symptoms-causes/syc-20351605", "PubMed: https://pubmed.ncbi.nlm.nih.gov/?term=common+cold+treatment"}},
            {{"Influenza (Flu)", "Symptoms: Fever, body aches, fatigue, cough"}, {"What to do:", "Rest, stay hydrated, take antiviral medication if prescribed early"}, {"Self-care:", "Isolate to prevent spread, use fever reducers, maintain fluid intake"}, {"Verified Sources:", "WHO: https://www.who.int/news-room/fact-sheets/detail/influenza-(seasonal)", "CDC: https://www.cdc.gov/flu/index.htm", "Google Scholar: https://scholar.google.com/scholar?q=influenza+treatment"}},
            {{"Hypertension (High Blood Pressure)", "Symptoms: Often asymptomatic, may include headaches, shortness of breath"}, {"What to do:", "Monitor blood pressure regularly, follow medication regimen, reduce sodium intake"}, {"Self-care:", "Exercise regularly, maintain healthy weight, limit alcohol, manage stress"}, {"Verified Sources:", "American Heart Association: https://www.heart.org/en/health-topics/high-blood-pressure", "NIH: https://www.nhlbi.nih.gov/health/high-blood-pressure", "PubMed: https://pubmed.ncbi.nlm.nih.gov/?term=hypertension+management"}},
            {{"Diabetes", "Symptoms: Increased thirst, frequent urination, fatigue, blurred vision"}, {"What to do:", "Monitor blood sugar, follow medication plan, maintain regular checkups"}, {"Self-care:", "Follow a balanced diet, exercise regularly, monitor glucose levels"}, {"Verified Sources:", "American Diabetes Association: https://www.diabetes.org/", "CDC: https://www.cdc.gov/diabetes/basics/index.html", "Google Scholar: https://scholar.google.com/scholar?q=diabetes+self+management"}},
            {{"Asthma", "Symptoms: Wheezing, shortness of breath, chest tightness, coughing"}, {"What to do:", "Use prescribed inhalers, avoid triggers, have an action plan"}, {"Self-care:", "Identify and avoid triggers, monitor symptoms, keep rescue medication handy"}, {"Verified Sources:", "American Lung Association: https://www.lung.org/lung-health-diseases/lung-disease-lookup/asthma", "NIH: https://www.nhlbi.nih.gov/health/asthma", "PubMed: https://pubmed.ncbi.nlm.nih.gov/?term=asthma+management"}},
            {{"COVID-19", "Symptoms: Fever, cough, fatigue, loss of taste/smell, difficulty breathing"}, {"What to do:", "Isolate immediately, monitor symptoms, seek medical care if severe"}, {"Self-care:", "Rest, stay hydrated, use over-the-counter fever reducers, monitor oxygen levels"}, {"Verified Sources:", "WHO: https://www.who.int/emergencies/diseases/novel-coronavirus-2019", "CDC: https://www.cdc.gov/coronavirus/2019-ncov/index.html", "Google Scholar: https://scholar.google.com/scholar?q=COVID-19+treatment"}}
        };
    }
    
    private void rebuildContent() {
        // Remove all existing sections except title
        Component[] components = mainContent.getComponents();
        List<Component> toKeep = new ArrayList<>();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if ("Health Tips & Information".equals(label.getText())) {
                    toKeep.add(comp);
                }
            }
        }
        mainContent.removeAll();
        for (Component comp : toKeep) {
            mainContent.add(comp);
        }
        
        // Rebuild sections
        healthTipsSection = createHealthTipsSection();
        faqSection = createFAQSection();
        illnessesSection = createCommonIllnessesSection();
        
        mainContent.add(healthTipsSection);
        mainContent.add(Box.createVerticalStrut(30));
        mainContent.add(faqSection);
        mainContent.add(Box.createVerticalStrut(30));
        mainContent.add(illnessesSection);
        
        mainContent.revalidate();
        mainContent.repaint();
    }
    
    private void filterContent(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            rebuildContent();
            return;
        }
        
        String query = searchText.toLowerCase().trim();
        
        // Filter tips
        List<String[]> filteredTips = new ArrayList<>();
        for (String[] tip : tips) {
            if (tip[0].toLowerCase().contains(query) || tip[1].toLowerCase().contains(query)) {
                filteredTips.add(tip);
            }
        }
        
        // Filter FAQs
        List<String[]> filteredFaqs = new ArrayList<>();
        for (String[] faq : faqs) {
            if (faq[0].toLowerCase().contains(query) || faq[1].toLowerCase().contains(query)) {
                filteredFaqs.add(faq);
            }
        }
        
        // Filter illnesses
        List<String[][]> filteredIllnesses = new ArrayList<>();
        for (String[][] illness : illnesses) {
            String searchableText = illness[0][0] + " " + illness[0][1] + " " + 
                                   illness[1][1] + " " + illness[2][1];
            if (searchableText.toLowerCase().contains(query)) {
                filteredIllnesses.add(illness);
            }
        }
        
        // Rebuild content with filtered data
        rebuildFilteredContent(filteredTips, filteredFaqs, filteredIllnesses);
    }
    
    private void rebuildFilteredContent(List<String[]> filteredTips, List<String[]> filteredFaqs, List<String[][]> filteredIllnesses) {
        Component[] components = mainContent.getComponents();
        List<Component> toKeep = new ArrayList<>();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if ("Health Tips & Information".equals(label.getText())) {
                    toKeep.add(comp);
                }
            }
        }
        mainContent.removeAll();
        for (Component comp : toKeep) {
            mainContent.add(comp);
        }
        
        // Rebuild sections with filtered data
        if (!filteredTips.isEmpty()) {
            healthTipsSection = createHealthTipsSection(filteredTips);
            mainContent.add(healthTipsSection);
            mainContent.add(Box.createVerticalStrut(30));
        }
        
        if (!filteredFaqs.isEmpty()) {
            faqSection = createFAQSection(filteredFaqs);
            mainContent.add(faqSection);
            mainContent.add(Box.createVerticalStrut(30));
        }
        
        if (!filteredIllnesses.isEmpty()) {
            illnessesSection = createCommonIllnessesSection(filteredIllnesses);
            mainContent.add(illnessesSection);
        }
        
        mainContent.revalidate();
        mainContent.repaint();
    }

    private JPanel createHealthTipsSection() {
        return createHealthTipsSection(java.util.Arrays.asList(tips));
    }
    
    private JPanel createHealthTipsSection(List<String[]> tipsList) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel sectionTitle = new JLabel("💡 Health Tips");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        sectionTitle.setForeground(new Color(200, 220, 255));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(15));

        for (String[] tip : tipsList) {
            section.add(createTipItem(tip[0], tip[1], tip[2]));
            section.add(Box.createVerticalStrut(10));
        }

        return section;
    }

    private JPanel createFAQSection() {
        return createFAQSection(java.util.Arrays.asList(faqs));
    }
    
    private JPanel createFAQSection(List<String[]> faqsList) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel sectionTitle = new JLabel("❓ Frequently Asked Questions");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        sectionTitle.setForeground(new Color(200, 220, 255));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(15));

        for (String[] faq : faqsList) {
            section.add(createFAQItem(faq[0], faq[1], faq[2]));
            section.add(Box.createVerticalStrut(10));
        }

        return section;
    }

    private JPanel createCommonIllnessesSection() {
        return createCommonIllnessesSection(java.util.Arrays.asList(illnesses));
    }
    
    private JPanel createCommonIllnessesSection(List<String[][]> illnessesList) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel sectionTitle = new JLabel("🏥 Common Illnesses & Diseases");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        sectionTitle.setForeground(new Color(200, 220, 255));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(15));

        for (String[][] illness : illnessesList) {
            section.add(createIllnessCard(illness));
            section.add(Box.createVerticalStrut(15));
        }

        return section;
    }

    private JPanel createTipItem(String title, String description, String link) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("• " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.add(titleLabel);

        JLabel descLabel = new JLabel("<html><div style='width:800px;'>" + description + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(240, 240, 240));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.add(descLabel);

        JLabel linkLabel = createHyperlink("Learn more from verified source →", link);
        linkLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.add(linkLabel);

        return item;
    }

    private JPanel createFAQItem(String question, String answer, String link) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel questionLabel = new JLabel("Q: " + question);
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.add(questionLabel);

        JLabel answerLabel = new JLabel("<html><div style='width:800px;'>A: " + answer + "</div></html>");
        answerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        answerLabel.setForeground(new Color(240, 240, 240));
        answerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.add(answerLabel);

        JLabel linkLabel = createHyperlink("Verified source →", link);
        linkLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.add(linkLabel);

        return item;
    }

    private JPanel createIllnessCard(String[][] illnessData) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 150), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Illness name and symptoms
        JLabel nameLabel = new JLabel(illnessData[0][0]);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(new Color(200, 220, 255));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(nameLabel);

        JLabel symptomsLabel = new JLabel(illnessData[0][1]);
        symptomsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        symptomsLabel.setForeground(new Color(220, 220, 220));
        symptomsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(symptomsLabel);
        card.add(Box.createVerticalStrut(10));

        // What to do
        JLabel whatToDoTitle = new JLabel(illnessData[1][0]);
        whatToDoTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        whatToDoTitle.setForeground(Color.WHITE);
        whatToDoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(whatToDoTitle);

        JLabel whatToDoDesc = new JLabel("<html><div style='width:800px;'>" + illnessData[1][1] + "</div></html>");
        whatToDoDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        whatToDoDesc.setForeground(new Color(240, 240, 240));
        whatToDoDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(whatToDoDesc);
        card.add(Box.createVerticalStrut(8));

        // Self-care
        JLabel selfCareTitle = new JLabel(illnessData[2][0]);
        selfCareTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        selfCareTitle.setForeground(Color.WHITE);
        selfCareTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(selfCareTitle);

        JLabel selfCareDesc = new JLabel("<html><div style='width:800px;'>" + illnessData[2][1] + "</div></html>");
        selfCareDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        selfCareDesc.setForeground(new Color(240, 240, 240));
        selfCareDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(selfCareDesc);
        card.add(Box.createVerticalStrut(10));

        // Verified Sources
        JLabel sourcesTitle = new JLabel(illnessData[3][0]);
        sourcesTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sourcesTitle.setForeground(new Color(255, 255, 150));
        sourcesTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sourcesTitle);

        for (int i = 1; i < illnessData[3].length; i++) {
            String sourceLine = illnessData[3][i];
            String[] parts = sourceLine.split(": ", 2);
            if (parts.length == 2) {
                String sourceName = parts[0];
                String sourceUrl = parts[1];
                JLabel sourceLink = createHyperlink("  • " + sourceName, sourceUrl);
                sourceLink.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(sourceLink);
            }
        }

        return card;
    }

    private JLabel createHyperlink(String text, String url) {
        JLabel linkLabel = new JLabel("<html><a href=''>" + text + "</a></html>");
        linkLabel.setForeground(new Color(150, 200, 255));
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openURL(url);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                linkLabel.setForeground(new Color(200, 230, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                linkLabel.setForeground(new Color(150, 200, 255));
            }
        });

        return linkLabel;
    }

    private void openURL(String urlString) {
        try {
            URI uri = new URI(urlString);
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(uri);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Unable to open browser. Please visit: " + urlString,
                    "Open Link",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (URISyntaxException | IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error opening link: " + e.getMessage() + "\nURL: " + urlString,
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}






