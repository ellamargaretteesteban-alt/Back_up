package ui.common;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

/**
 * NavigationBar with smooth animations and modern design.
 * Logo (left) + nav buttons (center) + Login (right).
 */
public class NavigationBar extends JPanel {

    private final Object mainFrame; // MainFrame reference (Object to avoid package issues)
    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final CardLayout rootLayout;
    private final JPanel rootPanel;

    // panels
    private final JPanel leftPanel;     // holds logo
    private final JPanel centerPanel;   // holds nav buttons
    private final JPanel rightPanel;    // holds login
    private final JLabel logoLabel;

    // logical buttons
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final String[] publicPages = {"HOME", "ABOUT", "SERVICES", "CONTACT"};
    private final String[] loggedInPages = {"HOME", "PROFILE", "HEALTH_TIPS", "ABOUT"};
    private final JButton loginButton;

    // state
    private String currentActivePage = "HOME";
    private boolean isLoggedIn = false;

    // animation
    private Timer animationTimer;
    private boolean animating = false;
    private int animationStep = 0;
    private static final int ANIMATION_STEPS = 25;
    private static final int ANIMATION_DELAY = 12; // ms
    private Map<JButton, AnimItem> animItems = new LinkedHashMap<>();
    private JPanel animationOverlay; // Overlay panel for animation

    // style
    private final Color textColor = Color.WHITE;
    private final Color highlightColor = new Color(255, 255, 100); // Bright yellow highlight for better contrast

    /**
     * Helper class to track animation state for each button
     */
    private static class AnimItem {
        Point startPos;
        Point targetPos;
        JButton button;
        boolean visible;

        AnimItem(JButton btn, Point start, Point target) {
            this.button = btn;
            this.startPos = start;
            this.targetPos = target;
            this.visible = false;
        }
    }

    public NavigationBar(Object mainFrame, CardLayout contentLayout, JPanel contentPanel, CardLayout rootLayout, JPanel rootPanel) {
        this.mainFrame = mainFrame;
        this.contentLayout = contentLayout;
        this.contentPanel = contentPanel;
        this.rootLayout = rootLayout;
        this.rootPanel = rootPanel;

        // Keep layout: West (logo), Center (nav), East (login)
        setLayout(new BorderLayout());
        // Use a stylish gradient background similar to FinalProject but with current color scheme
        setOpaque(false); // We'll paint custom background
        setPreferredSize(new Dimension(getWidth(), 60)); // Set preferred height
        
        // Add component listener to update overlay size
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (animationOverlay != null) {
                    animationOverlay.setBounds(0, 0, getWidth(), getHeight());
                }
            }
        });

        // LEFT: logo area
        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);

        // Use Logo helper to load and scale the icon
        ImageIcon logoIcon = null;
        try {
            Logo l = new Logo();
            logoIcon = l.getIcon();
        } catch (Exception ex) {
            // fallback text-logo if Logo fails
        }

        if (logoIcon != null) {
            logoLabel = new JLabel(logoIcon);
        } else {
            logoLabel = new JLabel("WellCo");
            logoLabel.setForeground(textColor);
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        }

        logoLabel.setCursor(Cursor.getDefaultCursor());
        JPanel logoContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        logoContainer.setOpaque(false);
        logoContainer.add(logoLabel);
        leftPanel.add(logoContainer, BorderLayout.NORTH);

        add(leftPanel, BorderLayout.WEST);

        // CENTER: nav buttons - use BoxLayout with glue for true centering
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(Box.createHorizontalGlue()); // Push buttons to center
        
        // Add all possible pages to navButtons (both public and logged-in)
        // Public pages
        for (String p : publicPages) {
            JButton b = createNavButton(p);
            b.addActionListener(e -> {
                showPage(p);
                setActive(p);
            });
            navButtons.put(p, b);
        }
        
        // Logged-in pages (PROFILE)
        for (String p : loggedInPages) {
            if (!navButtons.containsKey(p)) { // Don't duplicate HOME and ABOUT
                JButton b = createNavButton(p);
                b.addActionListener(e -> {
                    showPage(p);
                    setActive(p);
                });
                navButtons.put(p, b);
            }
        }
        
        // Add public pages to center panel initially
        for (int i = 0; i < publicPages.length; i++) {
            String p = publicPages[i];
            JButton b = navButtons.get(p);
            if (b != null) {
                centerPanel.add(b);
                if (i < publicPages.length - 1) {
                    centerPanel.add(Box.createHorizontalStrut(25)); // Spacing between buttons
                }
            }
        }
        centerPanel.add(Box.createHorizontalGlue()); // Balance the centering
        add(centerPanel, BorderLayout.CENTER);

        // RIGHT: login area
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        rightPanel.setOpaque(false);
        loginButton = createNavButton("LOGIN");
        loginButton.addActionListener(e -> {
            if (isLoggedIn) {
                // Logout functionality
                handleLogout();
            } else {
                showPage("LOGIN");
                setActive("LOGIN");
            }
        });
        rightPanel.add(loginButton);

        add(rightPanel, BorderLayout.EAST);

        // Initial active
        setActive("HOME");

        // Animate buttons on initial load - with delay
        SwingUtilities.invokeLater(() -> {
            Timer initTimer = new Timer(200, e -> {
                rebuildCenterPanel();
                if (!animating) {
                    animateButtonsFromLogo();
                }
            });
            initTimer.setRepeats(false);
            initTimer.start();
        });
    }

    private JButton createNavButton(String text) {
        // Format button text: convert "HEALTH_TIPS" to "Health Tips", "HOME" to "Home", etc.
        String displayText = formatButtonText(text);
        JButton b = new JButton(displayText);
        b.setForeground(textColor);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false); // transparent look
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // hover effect: simple background tint without changing layout
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Only change to highlight if not already active
                if (!text.equals(currentActivePage)) {
                    b.setForeground(highlightColor);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                // Only reset if not currently active
                if (!text.equals(currentActivePage)) {
                    b.setForeground(textColor);
                }
            }
        });
        return b;
    }
    
    /**
     * Format button text for display (e.g., "HEALTH_TIPS" -> "Health Tips")
     */
    private String formatButtonText(String text) {
        if (text == null) return "";
        if ("HEALTH_TIPS".equals(text)) {
            return "Health Tips";
        }
        // Convert "HOME" -> "Home", "PROFILE" -> "Profile", etc.
        if (text.length() > 0) {
            return text.charAt(0) + text.substring(1).toLowerCase();
        }
        return text;
    }

    /**
     * Show the specified page in the content panel
     */
    private void showPage(String pageName) {
        // All pages are in content cards
        if (contentLayout != null && contentPanel != null) {
            // If logged in and showing HOME, show logged-in version
            if (isLoggedIn && "HOME".equals(pageName)) {
                contentLayout.show(contentPanel, "HOME_LOGGED_IN");
            } else {
                contentLayout.show(contentPanel, pageName);
            }
        }
    }

    /**
     * Rebuild centerPanel structure to ensure proper centering
     */
    private void rebuildCenterPanel() {
        if (centerPanel == null) return;
        
        // Remove all components
        centerPanel.removeAll();
        
        // Rebuild structure: glue, buttons with struts, glue
        centerPanel.add(Box.createHorizontalGlue());
        
        // Use appropriate pages based on login status
        String[] pagesToShow = isLoggedIn ? loggedInPages : publicPages;
        
        for (int i = 0; i < pagesToShow.length; i++) {
            String p = pagesToShow[i];
            JButton btn = navButtons.get(p);
            if (btn != null) {
                // Remove from any other parent first
                Container currentParent = btn.getParent();
                if (currentParent != null && currentParent != centerPanel) {
                    currentParent.remove(btn);
                }
                centerPanel.add(btn);
                if (i < pagesToShow.length - 1) {
                    centerPanel.add(Box.createHorizontalStrut(25));
                }
            }
        }
        
        centerPanel.add(Box.createHorizontalGlue());
        centerPanel.revalidate();
        centerPanel.repaint();
    }
    
    /**
     * Clean up animation overlay and restore buttons to their proper parents
     */
    private void cleanupAnimationOverlay() {
        if (animationOverlay != null) {
            Component[] components = animationOverlay.getComponents();
            for (Component comp : components) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    animationOverlay.remove(btn);
                    
                    // Restore to proper parent
                    if (btn == loginButton) {
                        rightPanel.add(btn);
                    }
                    // navButtons will be handled by rebuildCenterPanel
                }
            }
            remove(animationOverlay);
            animationOverlay = null;
        }
        animating = false;
        animItems.clear();
        
        // Always rebuild centerPanel structure to ensure proper centering
        rebuildCenterPanel();
    }

    /**
     * Animate buttons from logo position to their final positions (horizontal)
     */
    private void animateButtonsFromLogo() {
        if (animating) return;
        animating = true;
        animationStep = 0;
        animItems.clear();

        // Wait for layout to be ready
        SwingUtilities.invokeLater(() -> {
            // Get logo position
            Point logoPos = getLogoPosition();
            if (logoPos == null) {
                animating = false;
                return;
            }

            // Get final positions of buttons after layout
            revalidate();
            doLayout();
            
            int delay = 50; // Stagger delay between buttons
            int index = 0;
            
            // Animate public pages or logged-in pages based on state
            String[] pagesToAnimate = isLoggedIn ? loggedInPages : publicPages;
            for (String p : pagesToAnimate) {
                JButton btn = navButtons.get(p);
                if (btn != null && btn.getParent() != null) {
                    Point finalPos = btn.getLocation();
                    SwingUtilities.convertPoint(btn.getParent(), finalPos, this);
                    
                    AnimItem item = new AnimItem(btn, new Point(logoPos), new Point(finalPos));
                    animItems.put(btn, item);
                    
                    // Temporarily hide button and position at logo
                    btn.setVisible(false);
                    btn.setLocation(logoPos);
                    btn.setSize(btn.getPreferredSize());
                    
                    // Start animation with delay
                    Timer startTimer = new Timer(delay * index, e -> {
                        item.visible = true;
                        btn.setVisible(true);
                    });
                    startTimer.setRepeats(false);
                    startTimer.start();
                    index++;
                }
            }

            // Animate login button
            if (loginButton != null && loginButton.getParent() != null) {
                Point finalPos = loginButton.getLocation();
                SwingUtilities.convertPoint(loginButton.getParent(), finalPos, this);
                
                AnimItem item = new AnimItem(loginButton, new Point(logoPos), new Point(finalPos));
                animItems.put(loginButton, item);
                
                loginButton.setVisible(false);
                loginButton.setLocation(logoPos);
                loginButton.setSize(loginButton.getPreferredSize());
                
                Timer startTimer = new Timer(delay * index, e -> {
                    item.visible = true;
                    loginButton.setVisible(true);
                });
                startTimer.setRepeats(false);
                startTimer.start();
            }

            // Start main animation timer
            if (animationTimer != null) {
                animationTimer.stop();
            }

            animationTimer = new Timer(ANIMATION_DELAY, e -> {
                animationStep++;
                double progress = (double) animationStep / ANIMATION_STEPS;
                if (progress > 1.0) progress = 1.0;
                
                // Easing function for smooth animation
                progress = easeOutCubic(progress);

                boolean allDone = true;
                for (AnimItem item : animItems.values()) {
                    if (!item.visible) {
                        allDone = false;
                        continue;
                    }
                    
                    Point currentPos = new Point(
                        (int) (item.startPos.x + (item.targetPos.x - item.startPos.x) * progress),
                        (int) (item.startPos.y + (item.targetPos.y - item.startPos.y) * progress)
                    );
                    
                    // Temporarily remove from layout manager to allow absolute positioning
                    Container parent = item.button.getParent();
                    if (parent != null && !(parent.getLayout() == null)) {
                        parent.remove(item.button);
                        if (animationOverlay == null) {
                            animationOverlay = new JPanel(null);
                            animationOverlay.setOpaque(false);
                            add(animationOverlay);
                            animationOverlay.setBounds(0, 0, getWidth(), getHeight());
                        }
                        animationOverlay.add(item.button);
                    }
                    item.button.setLocation(currentPos);
                    item.button.setOpaque(false);
                }

                if (animationStep >= ANIMATION_STEPS && allDone) {
                    animationTimer.stop();
                    animating = false;
                    
                    // Clean up overlay - buttons will be restored by rebuildCenterPanel
                    if (animationOverlay != null) {
                        Component[] components = animationOverlay.getComponents();
                        for (Component comp : components) {
                            if (comp instanceof JButton) {
                                JButton btn = (JButton) comp;
                                animationOverlay.remove(btn);
                                if (btn == loginButton) {
                                    rightPanel.add(btn);
                                }
                                // navButtons will be handled by rebuildCenterPanel
                            }
                        }
                        remove(animationOverlay);
                        animationOverlay = null;
                    }
                    animItems.clear();
                    
                    // Rebuild centerPanel to ensure proper structure
                    rebuildCenterPanel();
                    revalidate();
                    repaint();
                } else {
                    repaint();
                }
            });
            animationTimer.start();
        });
    }

    /**
     * Get logo position relative to this panel
     */
    private Point getLogoPosition() {
        if (logoLabel == null || !logoLabel.isVisible()) return null;
        Point logoPos = new Point();
        SwingUtilities.convertPointToScreen(logoPos, logoLabel);
        SwingUtilities.convertPointFromScreen(logoPos, this);
        return logoPos;
    }

    /**
     * Easing function for smooth animation
     */
    private double easeOutCubic(double t) {
        return 1 - Math.pow(1 - t, 3);
    }

    /**
     * Highlight active page name
     */
    public void setActive(String pageName) {
        if (pageName == null) pageName = "HOME";
        String up = pageName.toUpperCase();
        
        // Update current active page
        currentActivePage = up;

        // reset nav buttons
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            e.getValue().setForeground(textColor);
        }
        loginButton.setForeground(textColor);

        // set active color on matching button
        if ("LOGIN".equals(up)) {
            loginButton.setForeground(highlightColor);
        } else {
            JButton b = navButtons.get(up);
            if (b != null) b.setForeground(highlightColor);
        }
        
        // Also handle HOME_LOGGED_IN
        if ("HOME_LOGGED_IN".equals(pageName)) {
            JButton b = navButtons.get("HOME");
            if (b != null) b.setForeground(highlightColor);
        }
        
        // Force repaint to ensure highlight is visible
        repaint();
    }
    
    /**
     * Hide navigation buttons (Home, Profile, About) but keep logo visible
     * Called when user is logged in and viewing dashboard
     */
    public void hideNavigationButtons() {
        isLoggedIn = true;
        loginButton.setText("LOGOUT");
        // Rebuild with logged-in pages
        rebuildCenterPanel();
        centerPanel.setVisible(true);
        revalidate();
        repaint();
    }
    
    /**
     * Show navigation buttons again
     * Called when user logs out
     */
    public void showNavigationButtons() {
        isLoggedIn = false;
        loginButton.setText("LOGIN");
        // Rebuild with public pages
        rebuildCenterPanel();
        centerPanel.setVisible(true);
        revalidate();
        repaint();
    }
    
    /**
     * Handle logout - clear session and return to login page
     */
    private void handleLogout() {
        // Call MainFrame's logout handler using reflection to avoid package dependency
        try {
            java.lang.reflect.Method method = mainFrame.getClass().getMethod("handleLogout");
            method.invoke(mainFrame);
        } catch (Exception e) {
            // Fallback: just switch to login
            rootLayout.show(rootPanel, "LOGIN");
            showNavigationButtons();
        }
    }
    
    /**
     * Paint custom gradient background
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Gradient from deep blue to accent blue (similar to FinalProject but with current colors)
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(10, 78, 120),    // Deep blue
            0, getHeight(), new Color(39, 174, 219) // Accent blue
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.dispose();
    }
}


