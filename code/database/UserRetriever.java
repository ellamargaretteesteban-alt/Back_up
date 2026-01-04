package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserRetriever handles retrieving user data from the database.
 */
public class UserRetriever {
    
    private Connection connection;
    
    /**
     * Constructor
     * @param connection Database connection
     */
    public UserRetriever(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Loads the most recent user from the database
     * @return UserData object containing user information, or null if no user found
     */
    public UserData loadMostRecentUser() {
        if (connection == null) return null;
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT role, username, email, created_date, original_username, name, age FROM users ORDER BY id DESC LIMIT 1")) {
            if (rs.next()) {
                String role = rs.getString("role");
                // Normalize role
                role = normalizeRole(role);
                
                return new UserData(
                    role,
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getTimestamp("created_date"),
                    rs.getString("original_username"),
                    rs.getString("name"),
                    rs.getInt("age")
                );
            }
        } catch (Exception ex) {
            System.err.println("Error loading user: " + ex.getMessage());
        }
        return null;
    }
    
    /**
     * Gets all users from the database
     * @return List of UserData objects
     */
    public List<UserData> getAllUsers() {
        List<UserData> users = new ArrayList<>();
        if (connection == null) return users;
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT role, username, password, email, created_date, original_username, name, age FROM users ORDER BY created_date DESC")) {
            while (rs.next()) {
                String role = rs.getString("role");
                // Normalize role
                role = normalizeRole(role);
                
                UserData user = new UserData(
                    role,
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getTimestamp("created_date"),
                    rs.getString("original_username"),
                    rs.getString("name"),
                    rs.getInt("age")
                );
                user.password = rs.getString("password");
                users.add(user);
            }
        } catch (Exception ex) {
            System.err.println("Error getting all users: " + ex.getMessage());
        }
        return users;
    }
    
    /**
     * Gets a user by username
     * @param username Username to find
     * @return UserData if found, null otherwise
     */
    public UserData getUserByUsername(String username) {
        if (connection == null) return null;
        
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT role, username, password, email, created_date, original_username, name, age FROM users WHERE username = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                // Normalize role
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
                
                UserData user = new UserData(
                    role,
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getTimestamp("created_date"),
                    rs.getString("original_username"),
                    rs.getString("name"),
                    rs.getInt("age")
                );
                user.password = rs.getString("password");
                return user;
            }
        } catch (Exception ex) {
            System.err.println("Error getting user by username: " + ex.getMessage());
        }
        return null;
    }
    
    /**
     * Normalizes a role string to proper case
     * @param role Role string to normalize
     * @return Normalized role (Admin, Manager, or Customer)
     */
    private String normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "Customer";
        }
        role = role.trim();
        if (role.equalsIgnoreCase("Admin")) {
            return "Admin";
        } else if (role.equalsIgnoreCase("Manager")) {
            return "Manager";
        } else {
            return "Customer";
        }
    }
}

