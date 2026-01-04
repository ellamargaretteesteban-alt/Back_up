package database;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DatabaseManager is the main class that interconnects all database operations.
 * Uses MySQL database (JDBC MySQL Connector) with ArrayList backup storage.
 * Supports bidirectional sync between SQL and ArrayList.
 */
public class DatabaseManager {
    
    // Database connection settings
    private static final String DB_URL = "jdbc:mysql://localhost:3306/wellco?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "hfqe72161954_";
    
    private Connection connection;
    private boolean connected;
    
    // Handler classes
    private LoginHandler loginHandler;
    private SignupHandler signupHandler;
    private UserValidator userValidator;
    private UserRetriever userRetriever;
    private ProfileManager profileManager;
    private NotepadWriter notepadWriter;
    private RequestHandler requestHandler;
    private ActivityLogHandler activityLogHandler;
    private ManagerPermissionHandler managerPermissionHandler;
    private PillHandler pillHandler;
    private HealthTipHandler healthTipHandler;
    
    // ArrayList backup storage
    private InMemoryStorage inMemoryStorage;
    
    // File reader for accounts.txt
    private AccountsFileReader fileReader;
    
    /**
     * Constructor - initializes the database manager and all handlers
     */
    public DatabaseManager() {
        this.connection = null;
        this.connected = false;
        this.notepadWriter = new NotepadWriter();
        this.inMemoryStorage = new InMemoryStorage();
        this.fileReader = new AccountsFileReader();
    }
    
    /**
     * Attempts to connect to the MySQL database
     * @return true if connection successful, false otherwise
     */
    public boolean connect() {
        try {
            // Try to load MySQL JDBC driver
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("ERROR: MySQL JDBC Driver not found!");
                System.err.println("Please add mysql-connector-java.jar to your classpath.");
                System.err.println("Download from: https://dev.mysql.com/downloads/connector/j/");
                connected = false;
                System.out.println("→ Using ArrayList backup storage (In-Memory Mode)");
                // Load accounts from accounts.txt file into ArrayList
                loadAccountsFromFile();
                updateNotepadFile(); // Update notepad with ArrayList data
                return false;
            }
            
            // Attempt connection with timeout
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            connected = true;
            ensureUsersTable();
            
            // Initialize handlers with connection
            loginHandler = new LoginHandler(connection);
            signupHandler = new SignupHandler(connection);
            userValidator = new UserValidator(connection);
            userRetriever = new UserRetriever(connection);
            profileManager = new ProfileManager(connection);
            requestHandler = new RequestHandler(connection);
            activityLogHandler = new ActivityLogHandler(connection);
            managerPermissionHandler = new ManagerPermissionHandler(connection);
            pillHandler = new PillHandler(connection);
            healthTipHandler = new HealthTipHandler(connection);
            
            // Ensure all tables exist
            requestHandler.ensureRequestsTable();
            activityLogHandler.ensureActivityLogTable();
            managerPermissionHandler.ensureManagerPermissionsTable();
            pillHandler.ensurePillsTable();
            healthTipHandler.ensureHealthTipsTable();
            
            // Load accounts from accounts.txt file into both SQL and ArrayList
            System.out.println("\n=== Loading accounts from accounts.txt on startup ===");
            loadAccountsFromFile();
            System.out.println("=== Finished loading accounts ===\n");
            
            // Sync: Load SQL data into ArrayList, then sync ArrayList data to SQL
            syncFromSQLToArrayList();
            syncFromArrayListToSQL();
            
            // Create/update notepad file immediately with existing accounts
            updateNotepadFile();
            
            System.out.println("✓ Connected to MySQL database successfully.");
            System.out.println("✓ Bidirectional sync enabled (SQL ↔ ArrayList)");
            return true;
        } catch (java.sql.SQLException ex) {
            connected = false;
            System.err.println("✗ Database Connection Failed!");
            System.err.println("Error: " + ex.getMessage());
            
            // Provide helpful error messages
            if (ex.getMessage().contains("Access denied")) {
                System.err.println("→ Check your database username and password in DatabaseManager.java");
            } else if (ex.getMessage().contains("Unknown database")) {
                System.err.println("→ Database 'wellco' does not exist. Please create it first.");
                System.err.println("  Run in MySQL: CREATE DATABASE wellco;");
            } else if (ex.getMessage().contains("Communications link failure") || 
                      ex.getMessage().contains("Connection refused")) {
                System.err.println("→ MySQL server is not running or not accessible.");
                System.err.println("  Please start your MySQL server.");
            } else {
                System.err.println("→ Check if MySQL server is running on localhost:3306");
            }
            System.out.println("→ Using ArrayList backup storage (In-Memory Mode)");
            System.out.println("→ Signup and all features work with ArrayList backup");
            // Load accounts from accounts.txt file into ArrayList
            loadAccountsFromFile();
            updateNotepadFile(); // Update notepad with ArrayList data
            return false;
        } catch (Exception ex) {
            connected = false;
            System.err.println("✗ Unexpected error during database connection:");
            System.err.println("Error: " + ex.getMessage());
            System.out.println("→ Using ArrayList backup storage (In-Memory Mode)");
            System.out.println("→ Signup and all features work with ArrayList backup");
            // Load accounts from accounts.txt file into ArrayList
            loadAccountsFromFile();
            updateNotepadFile(); // Update notepad with ArrayList data
            return false;
        }
    }
    
    /**
     * Checks if the database is currently connected
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected && connection != null;
    }
    
    /**
     * Ensures the users table exists in the database with all required fields
     */
    private void ensureUsersTable() {
        if (!isConnected()) return;
        
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "role VARCHAR(50) DEFAULT 'Customer',"
                    + "username VARCHAR(255) UNIQUE NOT NULL,"
                    + "password VARCHAR(255) NOT NULL,"
                    + "email VARCHAR(255) NOT NULL,"
                    + "created_date DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "original_username VARCHAR(255),"
                    + "name VARCHAR(255),"
                    + "age INT)");
        } catch (Exception ex) {
            System.err.println("Error creating users table: " + ex.getMessage());
        }
    }
    
    // ============================================
    // FILE OPERATIONS (Load from accounts.txt)
    // ============================================
    
    /**
     * Loads accounts from accounts.txt file into both SQL and ArrayList
     */
    private void loadAccountsFromFile() {
        if (fileReader == null) return;
        
        List<UserData> fileAccounts = fileReader.readAccountsFromFile();
        if (fileAccounts.isEmpty()) {
            return;
        }
        
        int loadedToSQL = 0;
        int updatedInSQL = 0;
        int loadedToArrayList = 0;
        int updatedInArrayList = 0;
        
        for (UserData account : fileAccounts) {
            if (account.username == null || account.username.isEmpty()) {
                continue;
            }
            
            // Normalize role from file
            String fileRole = account.role != null ? account.role.trim() : "Customer";
            if (fileRole.equalsIgnoreCase("Admin")) {
                fileRole = "Admin";
            } else if (fileRole.equalsIgnoreCase("Manager")) {
                fileRole = "Manager";
            } else {
                fileRole = "Customer";
            }
            
            String filePassword = account.password != null ? account.password : "";
            String fileEmail = account.email != null ? account.email : "";
            
            // Sync to ArrayList (always)
            if (!inMemoryStorage.usernameExists(account.username)) {
                // New user - add to ArrayList
                inMemoryStorage.addUser(
                    account.username,
                    filePassword,
                    fileEmail,
                    fileRole
                );
                loadedToArrayList++;
            } else {
                // Existing user - update all fields from file
                UserData existingUser = inMemoryStorage.getUserByUsername(account.username);
                if (existingUser != null) {
                    boolean needsUpdate = false;
                    
                    // Check if role needs update
                    String existingRole = existingUser.role != null ? existingUser.role.trim() : "Customer";
                    if (existingRole.equalsIgnoreCase("Admin")) existingRole = "Admin";
                    else if (existingRole.equalsIgnoreCase("Manager")) existingRole = "Manager";
                    else existingRole = "Customer";
                    
                    if (!fileRole.equals(existingRole)) {
                        inMemoryStorage.updateUserRole(account.username, fileRole);
                        needsUpdate = true;
                    }
                    
                    // Check if password needs update
                    String existingPassword = existingUser.password != null ? existingUser.password : "";
                    if (!filePassword.equals(existingPassword)) {
                        inMemoryStorage.updateUserPassword(account.username, filePassword);
                        needsUpdate = true;
                    }
                    
                    // Check if email needs update
                    String existingEmail = existingUser.email != null ? existingUser.email : "";
                    if (!fileEmail.equals(existingEmail)) {
                        inMemoryStorage.updateUserProfile(account.username, existingUser.name, fileEmail, existingUser.age);
                        needsUpdate = true;
                    }
                    
                    if (needsUpdate) {
                        updatedInArrayList++;
                        System.out.println("  → Updated " + account.username + " in ArrayList from accounts.txt");
                    }
                }
            }
            
            // Sync to SQL (if connected)
            if (isConnected() && signupHandler != null) {
                try {
                    // Check if user already exists in SQL
                    if (!userValidator.usernameExists(account.username)) {
                        // New user - register with data from file
                        boolean success = signupHandler.registerUser(
                            account.username,
                            filePassword,
                            fileEmail,
                            fileRole);
                        if (success) {
                            loadedToSQL++;
                            if ("Admin".equals(fileRole) || "Manager".equals(fileRole)) {
                                System.out.println("  → Loaded " + fileRole + " account to SQL: " + account.username);
                            }
                        }
                    } else {
                        // Existing user - update all fields from file
                        System.out.println("  [SYNC] User " + account.username + " already exists in SQL, checking for updates...");
                        UserData existingUser = userRetriever.getUserByUsername(account.username);
                        if (existingUser != null) {
                            // Check if role needs update
                            String existingRole = existingUser.role != null ? existingUser.role.trim() : "Customer";
                            if (existingRole.equalsIgnoreCase("Admin")) existingRole = "Admin";
                            else if (existingRole.equalsIgnoreCase("Manager")) existingRole = "Manager";
                            else existingRole = "Customer";
                            
                            System.out.println("  [SYNC] File role: '" + fileRole + "', DB role: '" + existingRole + "'");
                            
                            // Check if password needs update
                            String existingPassword = existingUser.password != null ? existingUser.password : "";
                            
                            // Check if email needs update
                            String existingEmail = existingUser.email != null ? existingUser.email : "";
                            
                            // Update if any field changed
                            if (!fileRole.equals(existingRole) || !filePassword.equals(existingPassword) || !fileEmail.equals(existingEmail)) {
                                System.out.println("  [SYNC] Changes detected, updating...");
                                boolean updated = signupHandler.updateUserFromFile(account.username, fileRole, filePassword, fileEmail);
                                if (updated) {
                                    updatedInSQL++;
                                    
                                    // Build update message
                                    StringBuilder changes = new StringBuilder();
                                    if (!fileRole.equals(existingRole)) {
                                        changes.append("role: ").append(existingRole).append(" → ").append(fileRole);
                                    }
                                    if (!filePassword.equals(existingPassword)) {
                                        if (changes.length() > 0) changes.append(", ");
                                        changes.append("password updated");
                                    }
                                    if (!fileEmail.equals(existingEmail)) {
                                        if (changes.length() > 0) changes.append(", ");
                                        changes.append("email: ").append(existingEmail).append(" → ").append(fileEmail);
                                    }
                                    
                                    String updateMessage = "  → Updated " + account.username + " in SQL from accounts.txt (" + changes.toString() + ")";
                                    System.out.println(updateMessage);
                                    
                                    // Verify the update worked
                                    UserData verifyUser = userRetriever.getUserByUsername(account.username);
                                    if (verifyUser != null) {
                                        String verifyRole = verifyUser.role != null ? verifyUser.role.trim() : "Customer";
                                        if (verifyRole.equalsIgnoreCase("Admin")) verifyRole = "Admin";
                                        else if (verifyRole.equalsIgnoreCase("Manager")) verifyRole = "Manager";
                                        else verifyRole = "Customer";
                                        System.out.println("  [SYNC] Verification - Role in DB after update: '" + verifyRole + "'");
                                    }
                                } else {
                                    System.out.println("  [SYNC] WARNING: Update returned false!");
                                }
                            } else {
                                System.out.println("  [SYNC] No changes needed for " + account.username);
                            }
                        } else {
                            System.out.println("  [SYNC] WARNING: Could not retrieve existing user from DB!");
                        }
                    }
                } catch (Exception e) {
                    // User might already exist or SQL error, skip
                    System.err.println("Error syncing account " + account.username + " to SQL: " + e.getMessage());
                }
            }
        }
        
        // Summary output
        if (loadedToArrayList > 0) {
            System.out.println("✓ Loaded " + loadedToArrayList + " new accounts from accounts.txt into ArrayList");
        }
        if (updatedInArrayList > 0) {
            System.out.println("✓ Updated " + updatedInArrayList + " existing accounts in ArrayList from accounts.txt");
        }
        if (loadedToSQL > 0) {
            System.out.println("✓ Loaded " + loadedToSQL + " new accounts from accounts.txt into SQL");
        }
        if (updatedInSQL > 0) {
            System.out.println("✓ Updated " + updatedInSQL + " existing accounts in SQL from accounts.txt");
        }
        
        // Debug: List all Admin/Manager accounts with their final state
        System.out.println("\n=== Accounts.txt Sync Summary ===");
        for (UserData account : fileAccounts) {
            String role = account.role != null ? account.role.trim() : "Customer";
            if (role.equalsIgnoreCase("Admin")) role = "Admin";
            else if (role.equalsIgnoreCase("Manager")) role = "Manager";
            else role = "Customer";
            
            // Verify the account exists in both storages with correct role
            if (isConnected()) {
                UserData sqlUser = userRetriever.getUserByUsername(account.username);
                if (sqlUser != null) {
                    String sqlRole = sqlUser.role != null ? sqlUser.role.trim() : "Customer";
                    if (sqlRole.equalsIgnoreCase("Admin")) sqlRole = "Admin";
                    else if (sqlRole.equalsIgnoreCase("Manager")) sqlRole = "Manager";
                    else sqlRole = "Customer";
                    
                    if (!role.equals(sqlRole)) {
                        System.out.println("  ⚠ WARNING: " + account.username + " role mismatch - File: " + role + ", SQL: " + sqlRole);
                    } else if ("Admin".equals(role) || "Manager".equals(role)) {
                        System.out.println("  ✓ " + role + " account verified in SQL: " + account.username);
                    }
                }
            }
            
            UserData arrayUser = inMemoryStorage.getUserByUsername(account.username);
            if (arrayUser != null) {
                String arrayRole = arrayUser.role != null ? arrayUser.role.trim() : "Customer";
                if (arrayRole.equalsIgnoreCase("Admin")) arrayRole = "Admin";
                else if (arrayRole.equalsIgnoreCase("Manager")) arrayRole = "Manager";
                else arrayRole = "Customer";
                
                if (!role.equals(arrayRole)) {
                    System.out.println("  ⚠ WARNING: " + account.username + " role mismatch - File: " + role + ", ArrayList: " + arrayRole);
                } else if ("Admin".equals(role) || "Manager".equals(role)) {
                    System.out.println("  ✓ " + role + " account verified in ArrayList: " + account.username);
                }
            }
        }
        System.out.println("=== End Sync Summary ===\n");
    }
    
    /**
     * Public method to reload accounts from accounts.txt file
     * Useful for manually syncing changes made to accounts.txt
     */
    public void reloadAccountsFromFile() {
        System.out.println("\n=== Manually reloading accounts from accounts.txt ===");
        loadAccountsFromFile();
        System.out.println("=== Reload complete ===\n");
    }
    
    // ============================================
    // SYNC OPERATIONS (Bidirectional)
    // ============================================
    
    /**
     * Syncs data from SQL to ArrayList (SQL → ArrayList)
     */
    private void syncFromSQLToArrayList() {
        if (!isConnected() || userRetriever == null) return;
        
        try {
            List<UserData> sqlUsers = userRetriever.getAllUsers();
            inMemoryStorage.syncFromSQL(sqlUsers);
            System.out.println("✓ Synced " + sqlUsers.size() + " users from SQL to ArrayList");
        } catch (Exception ex) {
            System.err.println("Error syncing from SQL to ArrayList: " + ex.getMessage());
        }
    }
    
    /**
     * Syncs data from ArrayList to SQL (ArrayList → SQL)
     */
    private void syncFromArrayListToSQL() {
        if (!isConnected() || signupHandler == null) return;
        
        try {
            // Get SQL usernames
            List<String> sqlUsernames = userRetriever.getAllUsers().stream()
                .map(u -> u.username)
                .filter(u -> u != null)
                .collect(Collectors.toList());
            
            // Get users from ArrayList that aren't in SQL
            List<UserData> usersToSync = inMemoryStorage.getUsersToSyncToSQL(sqlUsernames);
            
            // Add them to SQL
            int synced = 0;
            for (UserData user : usersToSync) {
                if (user.username != null && user.password != null && user.email != null) {
                    try {
                        signupHandler.registerUser(user.username, user.password, user.email);
                        synced++;
                    } catch (Exception e) {
                        // User might already exist, skip
                    }
                }
            }
            
            if (synced > 0) {
                System.out.println("✓ Synced " + synced + " users from ArrayList to SQL");
            }
        } catch (Exception ex) {
            System.err.println("Error syncing from ArrayList to SQL: " + ex.getMessage());
        }
    }
    
    // ============================================
    // LOGIN OPERATIONS
    // ============================================
    
    /**
     * Authenticates a user login (tries SQL first, then ArrayList)
     * @param username Username
     * @param password Password (empty string to skip password check for profile reload)
     * @return UserData if login successful, null otherwise
     */
    public UserData loginUser(String username, String password) {
        System.out.println("\n=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + username);
        System.out.println("Database connected: " + isConnected());
        
        UserData user = null;
        String source = "";
        
        // Try SQL first
        if (isConnected() && loginHandler != null) {
            System.out.println("Attempting SQL authentication...");
            user = loginHandler.authenticateUser(username, password);
            if (user != null) {
                source = "SQL";
                System.out.println("✓ Login successful from SQL");
                System.out.println("  Role from SQL: " + (user.role != null ? user.role : "null"));
            } else {
                System.out.println("✗ SQL authentication failed");
            }
        } else {
            System.out.println("SQL not available, trying ArrayList...");
        }
        
        // Fall back to ArrayList if SQL didn't work
        if (user == null) {
            System.out.println("Attempting ArrayList authentication...");
            user = inMemoryStorage.authenticateUser(username, password);
            if (user != null) {
                source = "ArrayList";
                System.out.println("✓ Login successful from ArrayList");
                System.out.println("  Role from ArrayList: " + (user.role != null ? user.role : "null"));
            } else {
                System.out.println("✗ ArrayList authentication failed");
            }
        }
        
        // Ensure role is normalized in the returned user object
        if (user != null && user.role != null) {
            String originalRole = user.role;
            String role = user.role.trim();
            if (role.equalsIgnoreCase("Admin")) {
                role = "Admin";
            } else if (role.equalsIgnoreCase("Manager")) {
                role = "Manager";
            } else {
                role = "Customer";
            }
            
            System.out.println("Role normalization: '" + originalRole + "' → '" + role + "'");
            
            // Update role in user object if it changed
            if (!role.equals(user.role)) {
                System.out.println("  → Creating new UserData with normalized role");
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
                    user.age
                );
                user.password = originalPassword; // Preserve password from original
            }
            
            System.out.println("Final user role: " + user.role);
            System.out.println("Source: " + source);
        } else {
            System.out.println("✗ Login failed - user is null");
        }
        
        System.out.println("=== END LOGIN ATTEMPT ===\n");
        return user;
    }
    
    // ============================================
    // SIGNUP OPERATIONS
    // ============================================
    
    /**
     * Registers a new user account with default Customer role
     * Saves to both SQL (if available) and ArrayList
     * @param username Username (must be unique)
     * @param password Password (should be hashed in production)
     * @param email Email address
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, String email) {
        // Check if username/email exists in either storage
        if (usernameExists(username) || emailExists(email)) {
            return false;
        }
        
        boolean sqlSuccess = false;
        boolean arrayListSuccess = false;
        
        // Try SQL first
        if (isConnected() && signupHandler != null) {
            try {
                sqlSuccess = signupHandler.registerUser(username, password, email);
            } catch (Exception e) {
                // SQL failed, continue with ArrayList
            }
        }
        
        // Always save to ArrayList (backup)
        arrayListSuccess = inMemoryStorage.addUser(username, password, email, "Customer");
        
        if (arrayListSuccess) {
            // Update notepad file after successful registration (always works, even offline)
            updateNotepadFile();
            
            // If SQL is now available but registration failed, try to sync
            if (isConnected() && !sqlSuccess) {
                try {
                    // Try to register in SQL now
                    signupHandler.registerUser(username, password, email);
                } catch (Exception e) {
                    // If it fails, it will sync later
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    // ============================================
    // VALIDATION OPERATIONS
    // ============================================
    
    /**
     * Checks if username already exists (checks both SQL and ArrayList)
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        // Check SQL first
        if (isConnected() && userValidator != null) {
            if (userValidator.usernameExists(username)) {
                return true;
            }
        }
        
        // Check ArrayList
        return inMemoryStorage.usernameExists(username);
    }
    
    /**
     * Checks if email already exists (checks both SQL and ArrayList)
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        // Check SQL first
        if (isConnected() && userValidator != null) {
            if (userValidator.emailExists(email)) {
                return true;
            }
        }
        
        // Check ArrayList
        return inMemoryStorage.emailExists(email);
    }
    
    // ============================================
    // USER RETRIEVAL OPERATIONS
    // ============================================
    
    /**
     * Loads the most recent user (from SQL or ArrayList)
     * @return UserData object containing user information, or null if no user found
     */
    public UserData loadUser() {
        // Try SQL first
        if (isConnected() && userRetriever != null) {
            UserData user = userRetriever.loadMostRecentUser();
            if (user != null) return user;
        }
        
        // Fall back to ArrayList
        List<UserData> allUsers = inMemoryStorage.getAllUsers();
        if (!allUsers.isEmpty()) {
            // Return most recent (last added)
            return allUsers.get(allUsers.size() - 1);
        }
        
        return null;
    }
    
    /**
     * Gets all users (from SQL and ArrayList, merged)
     * @return List of UserData objects
     */
    public List<UserData> getAllUsers() {
        List<UserData> allUsers = new java.util.ArrayList<>();
        
        // Get from SQL
        if (isConnected() && userRetriever != null) {
            try {
                allUsers.addAll(userRetriever.getAllUsers());
            } catch (Exception e) {
                // SQL failed, continue with ArrayList
            }
        }
        
        // Get from ArrayList and add unique users
        List<UserData> arrayListUsers = inMemoryStorage.getAllUsers();
        for (UserData arrayUser : arrayListUsers) {
            // Only add if not already in list (by username)
            boolean exists = allUsers.stream()
                .anyMatch(u -> u.username != null && u.username.equals(arrayUser.username));
            if (!exists) {
                allUsers.add(arrayUser);
            }
        }
        
        return allUsers;
    }
    
    // ============================================
    // PROFILE OPERATIONS
    // ============================================
    
    /**
     * Saves/updates user profile information (saves to both SQL and ArrayList)
     * @param username Username
     * @param name User's name
     * @param email User's email
     * @param age User's age
     * @return true if save successful, false otherwise
     */
    public boolean saveUserProfile(String username, String name, String email, int age) {
        boolean sqlSuccess = false;
        boolean arrayListSuccess = false;
        
        // Try SQL first
        if (isConnected() && profileManager != null) {
            try {
                sqlSuccess = profileManager.saveUserProfile(username, name, email, age);
            } catch (Exception e) {
                // SQL failed, continue with ArrayList
            }
        }
        
        // Always update ArrayList
        arrayListSuccess = inMemoryStorage.updateUserProfile(username, name, email, age);
        
        if (arrayListSuccess || sqlSuccess) {
            updateNotepadFile();
            return true;
        }
        
        return false;
    }
    
    /**
     * Deletes a user account (from both SQL and ArrayList)
     * @param username Username to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteUser(String username) {
        boolean sqlSuccess = false;
        boolean arrayListSuccess = false;
        
        // Try SQL first
        if (isConnected() && profileManager != null) {
            try {
                sqlSuccess = profileManager.deleteUser(username);
            } catch (Exception e) {
                // SQL failed, continue with ArrayList
            }
        }
        
        // Always delete from ArrayList
        arrayListSuccess = inMemoryStorage.deleteUser(username);
        
        if (arrayListSuccess || sqlSuccess) {
            updateNotepadFile();
            return true;
        }
        
        return false;
    }
    
    /**
     * Deletes all users (from both SQL and ArrayList)
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteAllUsers() {
        boolean sqlSuccess = false;
        boolean arrayListSuccess = false;
        
        // Try SQL first
        if (isConnected() && profileManager != null) {
            try {
                sqlSuccess = profileManager.deleteAllUsers();
            } catch (Exception e) {
                // SQL failed, continue with ArrayList
            }
        }
        
        // Always delete from ArrayList
        inMemoryStorage.deleteAllUsers();
        arrayListSuccess = true;
        
        if (arrayListSuccess || sqlSuccess) {
            updateNotepadFile();
            return true;
        }
        
        return false;
    }
    
    // ============================================
    // NOTEPAD FILE OPERATIONS
    // ============================================
    
    /**
     * Updates the notepad file with current account list (from both SQL and ArrayList)
     * Works even when SQL is offline (uses ArrayList data)
     */
    public void updateNotepadFile() {
        if (notepadWriter == null) return;
        
        try {
            List<UserData> users = getAllUsers();
            notepadWriter.writeAccountsToFile(users);
            System.out.println("✓ Updated accounts.txt with " + users.size() + " accounts");
        } catch (Exception ex) {
            System.err.println("Error updating accounts.txt: " + ex.getMessage());
        }
    }
    
    /**
     * Attempts to reconnect to SQL and sync data bidirectionally
     * Can be called periodically or when SQL becomes available
     */
    public void attemptReconnectAndSync() {
        if (connected) {
            // Already connected, just sync
            syncFromSQLToArrayList();
            syncFromArrayListToSQL();
            updateNotepadFile();
            return;
        }
        
        // Try to reconnect
        if (connect()) {
            // Successfully reconnected, sync both ways
            syncFromSQLToArrayList();
            syncFromArrayListToSQL();
            updateNotepadFile();
            System.out.println("✓ Reconnected to SQL and synced all data");
        }
    }
    
    /**
     * Closes the database connection
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                connected = false;
                System.out.println("Database connection closed.");
            } catch (Exception ex) {
                System.err.println("Error closing connection: " + ex.getMessage());
            }
        }
    }
    
    // ============================================
    // PILL OPERATIONS
    // ============================================
    
    public boolean addPill(model.Pill pill) {
        if (isConnected() && pillHandler != null) {
            try {
                return pillHandler.addPill(pill);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    public boolean updatePill(String oldName, model.Pill pill) {
        if (isConnected() && pillHandler != null) {
            try {
                return pillHandler.updatePill(oldName, pill);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    public boolean deletePill(String name) {
        if (isConnected() && pillHandler != null) {
            try {
                return pillHandler.deletePill(name);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    public java.util.List<model.Pill> getAllPills() {
        if (isConnected() && pillHandler != null) {
            try {
                return pillHandler.getAllPills();
            } catch (Exception e) {
                return new java.util.ArrayList<>();
            }
        }
        return new java.util.ArrayList<>();
    }
    
    public model.Pill getPillByName(String name) {
        if (isConnected() && pillHandler != null) {
            try {
                return pillHandler.getPillByName(name);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    // ============================================
    // REQUEST OPERATIONS
    // ============================================
    
    public boolean createRequest(model.Request request) {
        if (isConnected() && requestHandler != null) {
            try {
                return requestHandler.createRequest(request);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    public java.util.List<model.Request> getAllRequests() {
        if (isConnected() && requestHandler != null) {
            try {
                return requestHandler.getAllRequests();
            } catch (Exception e) {
                return new java.util.ArrayList<>();
            }
        }
        return new java.util.ArrayList<>();
    }
    
    public java.util.List<model.Request> getPendingRequests() {
        if (isConnected() && requestHandler != null) {
            try {
                return requestHandler.getPendingRequests();
            } catch (Exception e) {
                return new java.util.ArrayList<>();
            }
        }
        return new java.util.ArrayList<>();
    }
    
    public boolean updateRequestStatus(int requestId, String status, String processedBy) {
        if (isConnected() && requestHandler != null) {
            try {
                return requestHandler.updateRequestStatus(requestId, status, processedBy);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    public boolean deleteRequest(int requestId) {
        if (isConnected() && requestHandler != null) {
            try {
                return requestHandler.deleteRequest(requestId);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    // ============================================
    // ACTIVITY LOG OPERATIONS
    // ============================================
    
    public boolean logActivity(model.ActivityLog log) {
        if (isConnected() && activityLogHandler != null) {
            try {
                return activityLogHandler.logActivity(log);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    public java.util.List<model.ActivityLog> getAllActivityLogs() {
        if (isConnected() && activityLogHandler != null) {
            try {
                return activityLogHandler.getAllActivityLogs();
            } catch (Exception e) {
                return new java.util.ArrayList<>();
            }
        }
        return new java.util.ArrayList<>();
    }
    
    public java.util.List<model.ActivityLog> getActivityLogsByAdmin(String adminUsername) {
        if (isConnected() && activityLogHandler != null) {
            try {
                return activityLogHandler.getActivityLogsByAdmin(adminUsername);
            } catch (Exception e) {
                return new java.util.ArrayList<>();
            }
        }
        return new java.util.ArrayList<>();
    }
    
    // ============================================
    // MANAGER PERMISSION OPERATIONS
    // ============================================
    
    public boolean saveManagerPermission(model.ManagerPermission permission) {
        if (isConnected() && managerPermissionHandler != null) {
            try {
                return managerPermissionHandler.savePermission(permission);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    public model.ManagerPermission getManagerPermission(String managerUsername) {
        if (isConnected() && managerPermissionHandler != null) {
            try {
                return managerPermissionHandler.getPermission(managerUsername);
            } catch (Exception e) {
                return new model.ManagerPermission(managerUsername);
            }
        }
        return new model.ManagerPermission(managerUsername);
    }
    
    public java.util.List<model.ManagerPermission> getAllManagerPermissions() {
        if (isConnected() && managerPermissionHandler != null) {
            try {
                return managerPermissionHandler.getAllPermissions();
            } catch (Exception e) {
                return new java.util.ArrayList<>();
            }
        }
        return new java.util.ArrayList<>();
    }
    
    // ============================================
    // HEALTH TIP OPERATIONS
    // ============================================
    
    public boolean addHealthTip(model.HealthTip tip) {
        if (isConnected() && healthTipHandler != null) {
            try {
                return healthTipHandler.addHealthTip(tip);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    public boolean updateHealthTip(int id, model.HealthTip tip) {
        if (isConnected() && healthTipHandler != null) {
            try {
                return healthTipHandler.updateHealthTip(id, tip);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    public boolean deleteHealthTip(int id) {
        if (isConnected() && healthTipHandler != null) {
            try {
                return healthTipHandler.deleteHealthTip(id);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    public java.util.List<model.HealthTip> getAllHealthTips() {
        if (isConnected() && healthTipHandler != null) {
            try {
                return healthTipHandler.getAllHealthTips();
            } catch (Exception e) {
                return new java.util.ArrayList<>();
            }
        }
        return new java.util.ArrayList<>();
    }
    
    public model.HealthTip getHealthTipById(int id) {
        if (isConnected() && healthTipHandler != null) {
            try {
                return healthTipHandler.getHealthTipById(id);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    // ============================================
    // ROLE CHANGE OPERATIONS
    // ============================================
    
    /**
     * Changes user role
     * @param username Username to change
     * @param newRole New role (Customer, Manager, Admin)
     * @param currentAdminRole Role of admin making the change
     * @return true if successful, false otherwise
     */
    public boolean changeUserRole(String username, String newRole, String currentAdminRole) {
        // Managers can only change roles to Customer (lower role)
        if ("Manager".equalsIgnoreCase(currentAdminRole)) {
            if ("Admin".equalsIgnoreCase(newRole) || "Manager".equalsIgnoreCase(newRole)) {
                return false; // Cannot change to Admin or Manager
            }
        }
        // Admins can change to any role (Customer, Manager, or Admin)
        
        if (isConnected() && profileManager != null) {
            try {
                // Get current user
                UserData user = userRetriever.getUserByUsername(username);
                if (user == null) return false;
                
                // Update role in database
                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE users SET role = ? WHERE username = ?")) {
                    ps.setString(1, newRole);
                    ps.setString(2, username);
                    ps.executeUpdate();
                }
                
                // Update in ArrayList
                inMemoryStorage.updateUserRole(username, newRole);
                updateNotepadFile();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Generates a random password with regex requirements
     * @return Generated password
     */
    public String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        java.util.Random random = new java.util.Random();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one uppercase, lowercase, digit, and special char
        password.append((char)('A' + random.nextInt(26))); // Uppercase
        password.append((char)('a' + random.nextInt(26))); // Lowercase
        password.append((char)('0' + random.nextInt(10))); // Digit
        password.append("!@#$%^&*".charAt(random.nextInt(8))); // Special
        
        // Fill rest randomly (total length 12)
        for (int i = 4; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        // Shuffle
        char[] arr = password.toString().toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        
        return new String(arr);
    }
    
    /**
     * Resets user password to a randomly generated one
     * @param username Username to reset
     * @return New password (encapsulated - admin doesn't see it, just sends via email dialog)
     */
    public String resetUserPassword(String username) {
        String newPassword = generateRandomPassword();
        
        if (isConnected()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE users SET password = ? WHERE username = ?")) {
                ps.setString(1, newPassword);
                ps.setString(2, username);
                ps.executeUpdate();
            } catch (Exception e) {
                return null;
            }
        }
        
        // Update in ArrayList
        inMemoryStorage.updateUserPassword(username, newPassword);
        updateNotepadFile();
        
        return newPassword; // Return for email sending (but admin doesn't see it in UI)
    }
}
