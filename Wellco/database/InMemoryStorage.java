package database;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * InMemoryStorage provides ArrayList-based storage as a backup when SQL is unavailable.
 * Supports bidirectional sync with SQL database.
 */
public class InMemoryStorage {
    
    private List<UserData> users;
    
    /**
     * Constructor - initializes empty storage
     */
    public InMemoryStorage() {
        this.users = new ArrayList<>();
    }
    
    /**
     * Adds a new user to ArrayList storage
     * @param username Username
     * @param password Password
     * @param email Email
     * @param role Role (defaults to Customer)
     * @return true if added successfully, false if username already exists
     */
    public boolean addUser(String username, String password, String email, String role) {
        // Check if username already exists
        if (usernameExists(username)) {
            return false;
        }
        
        // Create new user
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        UserData user = new UserData(
            role != null ? role : "Customer",
            username,
            email,
            now,
            username, // original username
            null,     // name
            0         // age
        );
        user.password = password;
        
        users.add(user);
        return true;
    }
    
    /**
     * Authenticates a user
     * @param username Username
     * @param password Password (empty to skip password check)
     * @return UserData if found and password matches, null otherwise
     */
    public UserData authenticateUser(String username, String password) {
        System.out.println("  [ArrayList] Searching for user: " + username);
        System.out.println("  [ArrayList] Total users in memory: " + users.size());
        
        for (UserData user : users) {
            if (user.username != null && user.username.equals(username)) {
                System.out.println("  [ArrayList] Found user: " + username);
                System.out.println("  [ArrayList] Raw role: '" + (user.role != null ? user.role : "null") + "'");
                
                // If password is empty, skip password check (for profile reload)
                if (password.isEmpty() || (user.password != null && user.password.equals(password))) {
                    // Normalize role before returning
                    String role = user.role != null ? user.role.trim() : "Customer";
                    if (role.equalsIgnoreCase("Admin")) {
                        role = "Admin";
                    } else if (role.equalsIgnoreCase("Manager")) {
                        role = "Manager";
                    } else {
                        role = "Customer";
                    }
                    
                    System.out.println("  [ArrayList] Normalized role: '" + role + "'");
                    
                    // Return user with normalized role
                    if (!role.equals(user.role)) {
                        // Create new UserData with normalized role
                        UserData normalizedUser = new UserData(
                            role,
                            user.username,
                            user.email,
                            user.createdDate,
                            user.originalUsername,
                            user.name,
                            user.age
                        );
                        normalizedUser.password = user.password;
                        return normalizedUser;
                    }
                    return user;
                } else {
                    System.out.println("  [ArrayList] Password mismatch");
                }
            }
        }
        System.out.println("  [ArrayList] User not found");
        return null;
    }
    
    /**
     * Checks if username exists
     * @param username Username to check
     * @return true if exists, false otherwise
     */
    public boolean usernameExists(String username) {
        return users.stream()
            .anyMatch(user -> user.username != null && user.username.equals(username));
    }
    
    /**
     * Checks if email exists
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    public boolean emailExists(String email) {
        return users.stream()
            .anyMatch(user -> user.email != null && user.email.equals(email));
    }
    
    /**
     * Gets all users
     * @return List of all UserData objects
     */
    public List<UserData> getAllUsers() {
        return new ArrayList<>(users);
    }
    
    /**
     * Gets a user by username
     * @param username Username
     * @return UserData if found, null otherwise
     */
    public UserData getUserByUsername(String username) {
        return users.stream()
            .filter(user -> user.username != null && user.username.equals(username))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Updates user profile
     * @param username Username
     * @param name Name
     * @param email Email
     * @param age Age
     * @return true if updated, false if user not found
     */
    public boolean updateUserProfile(String username, String name, String email, int age) {
        for (int i = 0; i < users.size(); i++) {
            UserData user = users.get(i);
            if (user.username != null && user.username.equals(username)) {
                // Create updated user (UserData fields are final, so we need to replace it)
                UserData updatedUser = new UserData(
                    user.role,
                    user.username,
                    email,
                    user.createdDate,
                    user.originalUsername,
                    name,
                    age
                );
                updatedUser.password = user.password;
                
                users.set(i, updatedUser);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Deletes a user
     * @param username Username to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteUser(String username) {
        return users.removeIf(user -> user.username != null && user.username.equals(username));
    }
    
    /**
     * Deletes all users
     */
    public void deleteAllUsers() {
        users.clear();
    }
    
    /**
     * Gets the count of users
     * @return Number of users
     */
    public int getUserCount() {
        return users.size();
    }
    
    /**
     * Syncs users from SQL database into ArrayList (merges, doesn't replace)
     * @param sqlUsers List of users from SQL
     */
    public void syncFromSQL(List<UserData> sqlUsers) {
        if (sqlUsers == null) return;
        
        // Add users from SQL that don't exist in ArrayList
        for (UserData sqlUser : sqlUsers) {
            if (sqlUser.username != null && !usernameExists(sqlUser.username)) {
                // Add password if available
                if (sqlUser.password != null) {
                    sqlUser.password = sqlUser.password;
                }
                users.add(sqlUser);
            } else if (sqlUser.username != null) {
                // User exists, update password if SQL has it and ArrayList doesn't
                UserData existing = getUserByUsername(sqlUser.username);
                if (existing != null && existing.password == null && sqlUser.password != null) {
                    existing.password = sqlUser.password;
                }
            }
        }
    }
    
    /**
     * Gets users that need to be synced to SQL (users not in SQL)
     * @param sqlUsernames List of usernames already in SQL
     * @return List of UserData to sync to SQL
     */
    public List<UserData> getUsersToSyncToSQL(List<String> sqlUsernames) {
        return users.stream()
            .filter(user -> user.username != null && !sqlUsernames.contains(user.username))
            .collect(Collectors.toList());
    }
    
    /**
     * Syncs ArrayList data to SQL (sends ArrayList users to SQL)
     * @param signupHandler SignupHandler to register users
     * @return Number of users synced
     */
    public int syncToSQL(SignupHandler signupHandler) {
        if (signupHandler == null) return 0;
        
        int synced = 0;
        for (UserData user : users) {
            if (user.username != null && user.password != null && user.email != null) {
                try {
                    // Try to add to SQL (will fail if already exists, which is fine)
                    signupHandler.registerUser(user.username, user.password, user.email);
                    synced++;
                } catch (Exception e) {
                    // User might already exist in SQL, skip
                }
            }
        }
        return synced;
    }
    
    /**
     * Updates user role
     * @param username Username
     * @param newRole New role
     * @return true if updated, false if user not found
     */
    public boolean updateUserRole(String username, String newRole) {
        for (int i = 0; i < users.size(); i++) {
            UserData user = users.get(i);
            if (user.username != null && user.username.equals(username)) {
                UserData updatedUser = new UserData(
                    newRole,
                    user.username,
                    user.email,
                    user.createdDate,
                    user.originalUsername,
                    user.name,
                    user.age
                );
                updatedUser.password = user.password;
                users.set(i, updatedUser);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Updates user password
     * @param username Username
     * @param newPassword New password
     * @return true if updated, false if user not found
     */
    public boolean updateUserPassword(String username, String newPassword) {
        for (UserData user : users) {
            if (user.username != null && user.username.equals(username)) {
                user.password = newPassword;
                return true;
            }
        }
        return false;
    }
}

