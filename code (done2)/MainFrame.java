import database.DatabaseManager;
import database.UserData;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import model.Pill;
import ui.admin.AdminInterface;
import ui.auth.LoginPanel;
import ui.auth.SignupPanel;
import ui.common.AboutPanel;
import ui.common.BackgroundPanel;
import ui.common.ContactPanel;
import ui.common.IntroInterface;
import ui.common.NavigationBar;
import ui.common.PublicHomePanel;
import ui.common.ServicesPanel;
import ui.common.TopBar;
import ui.customer.CustomerInterface;
import ui.customer.HealthTipsPanel;
import ui.customer.ProfilePanel;
import ui.manager.ManagerInterface;

/**
 * MainFrame is the main application window that coordinates all UI components.
 */
public class MainFrame {

    // Theme colors
    private static final Color BACKGROUND_LIGHT = new Color(225, 245, 255);

    private JFrame frame;
    private CardLayout rootLayout;
    private JPanel rootPanel;

    // Database manager
    private DatabaseManager dbManager;

    // Current logged in user
    private UserData currentUser;

    // UI Components
    private CustomerInterface customerInterface;
    private NavigationBar navigationBar;
    private TopBar topBar;
    private AboutPanel aboutPanel;
    private IntroInterface introInterface;
    private LoginPanel loginPanel;
    private SignupPanel signupPanel;
    private ProfilePanel profilePanel;
    private AdminInterface adminInterface;
    private ManagerInterface managerInterface;
    private CardLayout contentLayout;
    private JPanel contentCards;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().start());
    }

    private void start() {
        // Initialize database manager
        dbManager = new DatabaseManager();
        dbManager.connect(); // This automatically creates/updates notepad file with existing accounts

        // Seed pills into database if empty
        seedPillsToDatabaseIfEmpty();

        // Initialize UI components
        createAndShowGUI();
    }

    /**
     * Seeds default pills into database if database is empty
     */
    private void seedPillsToDatabaseIfEmpty() {
        List<Pill> existingPills = dbManager.getAllPills();
        if (existingPills == null || existingPills.isEmpty()) {
            // Database is empty, seed default pills
            List<Pill> defaultPills = createDefaultPills();
            for (Pill pill : defaultPills) {
                dbManager.addPill(pill);
            }
            System.out.println("✓ Seeded " + defaultPills.size() + " default medications to database");
        }
    }

    /**
     * Creates list of default pills to seed
     */
    private List<Pill> createDefaultPills() {
        List<Pill> pills = new ArrayList<>();
        pills.add(new Pill("Biogesic (Paracetamol)", "A popular OTC tablet for relieving fever and mild pain.", "Commonly used for fever and headache relief."));
        pills.add(new Pill("Neozep", "Cold medicine used for runny nose, sneezing, and congestion.", "Often used for cold symptoms."));
        pills.add(new Pill("Bioflu", "All-in-one relief for flu symptoms such as body pain, fever, and clogged nose.", "Common for flu-like discomfort."));
        pills.add(new Pill("Alaxan FR", "A combination tablet commonly used for body pains and aches.", "Often used for muscle pain relief."));
        pills.add(new Pill("Kremil-S", "A chewable tablet commonly taken for hyperacidity.", "Used for stomach discomfort related to acidity."));
        pills.add(new Pill("Decolgen", "Cold medicine used for nasal congestion and headaches.", "Used for colds and stuffy nose symptoms."));
        pills.add(new Pill("Solmux", "A well-known expectorant brand in the Philippines.", "Used to help loosen mucus in cough with phlegm."));
        pills.add(new Pill("Tuseran Forte", "Cold-and-cough relief tablet for cough and nasal congestion.", "Used for bothersome cough and cold symptoms."));
        pills.add(new Pill("Enervon", "A multivitamin tablet known for boosting energy.", "General energy and immunity support."));
        pills.add(new Pill("Myra-E", "Vitamin E supplement for skin and cell protection.", "Commonly used for skin wellness."));
        pills.add(new Pill("Robitussin", "A cough syrup brand used for cough control.", "Used for different types of cough depending on variant."));
        pills.add(new Pill("Ventolin (Salbutamol)", "Bronchodilator typically used for asthma relief.", "Used for breathing comfort."));
        pills.add(new Pill("Strepsils", "Lozenges used for sore throat soothing.", "Used for throat discomfort."));
        pills.add(new Pill("Ascof Lagundi", "Lagundi-based natural cough remedy.", "Used for cough and mild asthma symptoms."));
        pills.add(new Pill("Cetirizine", "Antihistamine for allergy relief.", "Used for runny nose, itching, and allergic reactions."));
        pills.add(new Pill("Loratadine", "Non-drowsy antihistamine for allergies.", "Used for allergy-related symptoms."));
        pills.add(new Pill("Diatabs", "Anti-diarrheal medicine commonly used in PH.", "Used for sudden diarrhea relief."));
        pills.add(new Pill("Imodium", "OTC anti-diarrhea capsule.", "Used to control loose bowel movement."));
        pills.add(new Pill("Buscopan", "Tablet used for stomach and intestinal cramps.", "Used for abdominal discomfort."));
        pills.add(new Pill("Motilium", "Aids with digestion-related discomfort.", "Used for bloating and slow digestion."));
        pills.add(new Pill("Gaviscon", "Chewable tablet/liquid for heartburn and indigestion.", "Used for acid reflux relief."));
        pills.add(new Pill("Mefenamic Acid", "Pain reliever used for various types of moderate pain.", "Commonly used for headache or body pains."));
        pills.add(new Pill("Alvedon", "Paracetamol brand used for pain and fever relief.", "Used similarly to paracetamol tablets."));
        pills.add(new Pill("Tempra", "Paracetamol brand popular for fever relief.", "Used for mild pain and fever."));
        pills.add(new Pill("Flanax", "Pain reliever/anti-inflammatory tablet.", "Often used for muscle pain and inflammation."));
        pills.add(new Pill("Ibuprofen (Medicol Advance)", "Anti-inflammatory and pain relief medicine.", "Used for headaches and body aches."));
        pills.add(new Pill("RiteMed Paracetamol", "Affordable paracetamol variant.", "Used for fever and mild pain."));
        pills.add(new Pill("Dolfenal", "A strong pain reliever for moderate to severe pain.", "Used commonly for headaches or dysmenorrhea."));
        pills.add(new Pill("Alaxan", "Pain relief tablet for body aches.", "Used for muscle and joint discomfort."));
        pills.add(new Pill("Revicon", "Multivitamin for energy and body resistance.", "Used for general wellness support."));
        return pills;
    }

    // ------------------------------------
    //        UI CREATION
    // ------------------------------------

    private void createAndShowGUI() {
        frame = new JFrame("WellCo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);

        rootLayout = new CardLayout();
        rootPanel = new JPanel(rootLayout);

        // Initialize UI components
        customerInterface = new CustomerInterface(frame);
        customerInterface.setDatabaseManager(dbManager);
        customerInterface.seedPills();

        aboutPanel = new AboutPanel(frame);

        loginPanel = new LoginPanel(dbManager, rootLayout, rootPanel, null);
        signupPanel = new SignupPanel(dbManager, rootLayout, rootPanel);

        topBar = new TopBar(query -> customerInterface.filterPills(query));

        // Create public interface (this will create profilePanel inside)
        JPanel publicInterface = createPublicInterface();

        // Create intro interface - transitions to public home after animation
        introInterface = new IntroInterface(frame, () -> {
            rootLayout.show(rootPanel, "PUBLIC");
            // Show HOME page in public interface
            SwingUtilities.invokeLater(() -> {
                if (contentLayout != null && contentCards != null) {
                    contentLayout.show(contentCards, "HOME");
                    if (navigationBar != null) {
                        navigationBar.setActive("HOME");
                    }
                }
            });
        });

        // Add panels in order: INTRO -> PUBLIC (with nav bar)
        // PUBLIC panel is used for both logged-in and logged-out states
        rootPanel.add(introInterface.createPanel(), "INTRO");
        rootPanel.add(publicInterface, "PUBLIC");

        frame.add(rootPanel);
        // Show intro first
        rootLayout.show(rootPanel, "INTRO");
        frame.setVisible(true);
    }

    // ------------------------------------
    // PUBLIC INTERFACE (with NavigationBar)
    // ------------------------------------

    private JPanel createPublicInterface() {
        // Use BackgroundPanel for gradient background (like FinalProject)
        BackgroundPanel container = new BackgroundPanel();
        container.setLayout(new BorderLayout());

        // create content cards first - shared between public and logged-in
        contentLayout = new CardLayout();
        contentCards = new JPanel(contentLayout);
        contentCards.setOpaque(false);

        // Add public pages
        PublicHomePanel publicHomePanel = new PublicHomePanel();
        contentCards.add(publicHomePanel, "HOME");
        contentCards.add(aboutPanel.createPanel(), "ABOUT");
        ServicesPanel servicesPanel = new ServicesPanel();
        contentCards.add(servicesPanel, "SERVICES");
        ContactPanel contactPanel = new ContactPanel(dbManager, frame);
        contentCards.add(contactPanel.createPanel(), "CONTACT");

        // Add auth pages
        contentCards.add(loginPanel.createPanel(user -> {
            handleLoginSuccess(user);
        }), "LOGIN");
        contentCards.add(signupPanel.createPanel(), "SIGNUP");

        // Set content cards references so LoginPanel, SignupPanel, and PublicHomePanel can navigate
        loginPanel.setContentCards(contentLayout, contentCards);
        signupPanel.setContentCards(contentLayout, contentCards);
        publicHomePanel.setContentCards(contentLayout, contentCards, rootLayout, rootPanel);

        // Initialize profile panel now that contentLayout and contentCards are available
        // Pass a callback to handle account deletion and redirect to login
        // Use a method reference to handleLogout to ensure consistency
        profilePanel = new ProfilePanel(dbManager, () -> currentUser, frame, rootLayout, rootPanel,
                                       contentLayout, contentCards, this::handleLogout);

        // Add logged-in pages
        contentCards.add(customerInterface.createHomePanel(), "HOME_LOGGED_IN");
        contentCards.add(profilePanel.createPanel(user -> {
            currentUser = user;
        }), "PROFILE");
        HealthTipsPanel healthTipsPanel = new HealthTipsPanel();
        contentCards.add(healthTipsPanel, "HEALTH_TIPS");

        // Create navigation bar (needs access to content cards and this MainFrame)
        navigationBar = new NavigationBar(this, contentLayout, contentCards, rootLayout, rootPanel);
        container.add(navigationBar, BorderLayout.NORTH);

        // Center panel - transparent to show BackgroundPanel gradient
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        // TopBar is only shown on Services page, not on all pages
        // We'll add it directly to ServicesPanel instead
        center.add(contentCards, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        JLabel footerLabel = new JLabel("© WellCo 2025");
        footerLabel.setForeground(Color.WHITE);
        footer.add(footerLabel);
        center.add(footer, BorderLayout.SOUTH);

        container.add(center, BorderLayout.CENTER);

        return container;
    }

    /**
     * Show login page (called from NavigationBar)
     */
    public void showLogin() {
        // Show PUBLIC panel and then LOGIN page in content cards
        rootLayout.show(rootPanel, "PUBLIC");
        if (contentLayout != null && contentCards != null) {
            contentLayout.show(contentCards, "LOGIN");
        }
    }

    /**
     * Handle login success - route based on user role
     */
    private void handleLoginSuccess(UserData user) {
        if (user == null) {
            System.out.println("⚠ Login failed: user is null");
            return;
        }

        // Ensure role is normalized
        String role = user.role != null ? user.role.trim() : "Customer";
        if (role.equalsIgnoreCase("Admin")) {
            role = "Admin";
        } else if (role.equalsIgnoreCase("Manager")) {
            role = "Manager";
        } else {
            role = "Customer";
        }

        // Update user object with normalized role if needed
        if (!role.equals(user.role)) {
            // Preserve password from original user
            String originalPassword = user.password;
            // Create new UserData with normalized role
            user = new UserData(
                role,
                user.username,
                user.email,
                user.createdDate,
                user.originalUsername,
                user.name,
                user.age,
                user.nickname,
                user.status
            );
            user.password = originalPassword; // Preserve password from original
        }

        currentUser = user; // Set current user with normalized role

        System.out.println("→ User logged in: " + user.username + " with role: " + role);
        System.out.println("→ User object role field: " + (user.role != null ? user.role : "null"));

        // Ensure root panel is showing PUBLIC view (needed for contentCards to be visible)
        if (rootLayout != null && rootPanel != null) {
            rootLayout.show(rootPanel, "PUBLIC");
        }

        if ("Admin".equals(role)) {
            System.out.println("→ Routing to Admin Interface");
            // Show admin interface
            if (adminInterface == null) {
                adminInterface = new AdminInterface(dbManager, frame, user);
                System.out.println("  → Created new AdminInterface instance");
            }
            // Remove admin panel if exists and add new one
            Component[] components = contentCards.getComponents();
            for (Component comp : components) {
                if (comp.getName() != null && comp.getName().equals("ADMIN_PANEL")) {
                    contentCards.remove(comp);
                    System.out.println("  → Removed existing ADMIN_PANEL");
                    break;
                }
            }
            JPanel adminPanel = adminInterface.createAdminPanel();
            adminPanel.setName("ADMIN_PANEL");
            contentCards.add(adminPanel, "ADMIN");
            System.out.println("  → Added ADMIN panel to contentCards");
            contentLayout.show(contentCards, "ADMIN");
            System.out.println("  → Showing ADMIN card");

            // Hide navigation bar for admin
            if (navigationBar != null) {
                navigationBar.setVisible(false);
                System.out.println("  → Navigation bar hidden");
            }
        } else if ("Manager".equals(role)) {
            System.out.println("→ Routing to Manager Interface");
            // Show manager interface
            if (managerInterface == null) {
                managerInterface = new ManagerInterface(dbManager, frame, user);
                System.out.println("  → Created new ManagerInterface instance");
            }
            // Remove manager panel if exists and add new one
            Component[] components = contentCards.getComponents();
            for (Component comp : components) {
                if (comp.getName() != null && comp.getName().equals("MANAGER_PANEL")) {
                    contentCards.remove(comp);
                    System.out.println("  → Removed existing MANAGER_PANEL");
                    break;
                }
            }
            JPanel managerPanel = managerInterface.createManagerPanel();
            managerPanel.setName("MANAGER_PANEL");
            contentCards.add(managerPanel, "MANAGER");
            System.out.println("  → Added MANAGER panel to contentCards");
            contentLayout.show(contentCards, "MANAGER");
            System.out.println("  → Showing MANAGER card");

            // Hide navigation bar for manager
            if (navigationBar != null) {
                navigationBar.setVisible(false);
                System.out.println("  → Navigation bar hidden");
            }
        } else {
            // Show customer interface
            System.out.println("→ Routing to Customer Interface");
            if (navigationBar != null) {
                navigationBar.hideNavigationButtons();
                navigationBar.setActive("HOME");
                navigationBar.setVisible(true);
            }
            contentLayout.show(contentCards, "HOME_LOGGED_IN");
            System.out.println("  → Showing HOME_LOGGED_IN card");
        }

        // Force repaint to ensure UI updates
        if (frame != null) {
            frame.revalidate();
            frame.repaint();
        }
    }

    /**
     * Handle logout - clear session and return to login page
     */
    public void handleLogout() {
        currentUser = null;
        if (navigationBar != null) {
            navigationBar.showNavigationButtons();
            navigationBar.setVisible(true);
        }
        // Ensure we're on PUBLIC panel first
        if (rootLayout != null && rootPanel != null) {
            rootLayout.show(rootPanel, "PUBLIC");
        }
        // Then show LOGIN page in content cards
        if (contentLayout != null && contentCards != null) {
            contentLayout.show(contentCards, "LOGIN");
        }
        // Force UI refresh
        if (frame != null) {
            frame.revalidate();
            frame.repaint();
        }
    }
}