package ui.common;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * TopBar component for search and title.
 */
public class TopBar {
    
    private JTextField searchField;
    private java.util.function.Consumer<String> onSearch;
    
    public TopBar(java.util.function.Consumer<String> onSearch) {
        this.onSearch = onSearch;
    }
    
    public JPanel createPanel() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false); // Transparent to show background gradient
        top.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("INSPIRED BY PATIENTS — DRIVEN BY SCIENCE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE); // White text for contrast
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                onSearch.accept(searchField.getText());
            }
        });

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        searchRow.add(searchField, BorderLayout.CENTER);
        JButton sBtn = new JButton("Search");
        sBtn.addActionListener(e -> onSearch.accept(searchField.getText()));
        searchRow.add(sBtn, BorderLayout.EAST);

        top.add(title);
        top.add(Box.createVerticalStrut(10));
        top.add(searchRow);

        return top;
    }
    
    public JTextField getSearchField() {
        return searchField;
    }
}

