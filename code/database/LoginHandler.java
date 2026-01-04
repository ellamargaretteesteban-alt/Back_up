package database;

import java.sql.*;

/**
 * LoginHandler handles all login-related database operations.
 */
public class LoginHandler {
    
    private Connection connection;
    
    /**
     * Constructor
     * @param connection Database connection
     */
    public LoginHandler(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Authenticates a user login
     * @param username Username
     * @param password Password (empty string to skip password check for profile reload)
     * @return UserData if login successful, null otherwise
     */
    public UserData authenticateUser(String username, String password) {
        if (connection == null) return null;
        
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT role, username, password, email, created_date, original_username, name, age FROM users WHERE username = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String dbRole = rs.getString("role");
                String dbPassword = rs.getString("password");
                System.out.println("  [SQL] Found user in database");
                System.out.println("  [SQL] Raw role from DB: '" + dbRole + "'");
                System.out.println("  [SQL] Password match: " + (password.isEmpty() || dbPassword != null && dbPassword.equals(password)));
                
                // If password is empty, skip password check (for profile reload)
                if (password.isEmpty() || dbPassword != null && dbPassword.equals(password)) {
                    String role = dbRole;
                    // Normalize role to proper case
                    if (role != null) {
                        role = role.trim();
                        if (role.equalsIgnoreCase("Admin")) {
                            role = "Admin";
                        } else if (role.equalsIgnoreCase("Manager")) {
                            role = "Manager";
                        } else {
                            role = "Customer";
                        }
                    } else {
                        role = "Customer";
                    }
                    
                    System.out.println("  [SQL] Normalized role: '" + role + "'");
                    
                    return new UserData(
                        role,
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getTimestamp("created_date"),
                        rs.getString("original_username"),
                        rs.getString("name"),
                        rs.getInt("age")
                    );
                } else {
                    System.out.println("  [SQL] Password mismatch");
                }
            } else {
                System.out.println("  [SQL] User not found in database");
            }
        } catch (Exception ex) {
            System.err.println("Error during login: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
}

