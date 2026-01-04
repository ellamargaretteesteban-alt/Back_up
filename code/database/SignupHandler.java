package database;

import java.sql.*;

/**
 * SignupHandler handles all user registration operations.
 */
public class SignupHandler {
    
    private Connection connection;
    
    /**
     * Constructor
     * @param connection Database connection
     */
    public SignupHandler(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Registers a new user account with default Customer role
     * @param username Username (must be unique)
     * @param password Password (should be hashed in production)
     * @param email Email address
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, String email) {
        return registerUser(username, password, email, "Customer");
    }
    
    /**
     * Registers a new user account with specified role
     * @param username Username (must be unique)
     * @param password Password (should be hashed in production)
     * @param email Email address
     * @param role User role (Admin, Manager, or Customer)
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, String email, String role) {
        if (connection == null) return false;
        
        // Validate and normalize role
        if (role == null || role.isEmpty()) {
            role = "Customer";
        } else {
            role = role.trim();
            if (role.equalsIgnoreCase("Admin")) {
                role = "Admin";
            } else if (role.equalsIgnoreCase("Manager")) {
                role = "Manager";
            } else {
                role = "Customer";
            }
        }
        
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users(role, username, password, email, original_username) VALUES (?,?,?,?,?)")) {
            ps.setString(1, role);
            ps.setString(2, username);
            ps.setString(3, password); // In production, hash this password
            ps.setString(4, email);
            ps.setString(5, username); // Original username same as username initially
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error registering user: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Updates user role in database
     * @param username Username
     * @param role New role
     * @return true if update successful, false otherwise
     */
    public boolean updateUserRole(String username, String role) {
        if (connection == null) return false;
        
        // Validate and normalize role
        if (role == null || role.isEmpty()) {
            role = "Customer";
        } else {
            role = role.trim();
            if (role.equalsIgnoreCase("Admin")) {
                role = "Admin";
            } else if (role.equalsIgnoreCase("Manager")) {
                role = "Manager";
            } else {
                role = "Customer";
            }
        }
        
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET role = ? WHERE username = ?")) {
            ps.setString(1, role);
            ps.setString(2, username);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.err.println("Error updating user role: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Updates user password in database
     * @param username Username
     * @param password New password
     * @return true if update successful, false otherwise
     */
    public boolean updateUserPassword(String username, String password) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET password = ? WHERE username = ?")) {
            ps.setString(1, password != null ? password : "");
            ps.setString(2, username);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.err.println("Error updating user password: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Updates user email in database
     * @param username Username
     * @param email New email
     * @return true if update successful, false otherwise
     */
    public boolean updateUserEmail(String username, String email) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET email = ? WHERE username = ?")) {
            ps.setString(1, email != null ? email : "");
            ps.setString(2, username);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.err.println("Error updating user email: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Updates all user fields from accounts.txt (role, password, email)
     * @param username Username
     * @param role New role
     * @param password New password
     * @param email New email
     * @return true if update successful, false otherwise
     */
    public boolean updateUserFromFile(String username, String role, String password, String email) {
        if (connection == null) {
            System.err.println("  [updateUserFromFile] Connection is null!");
            return false;
        }
        
        // Validate and normalize role
        String originalRole = role;
        if (role == null || role.isEmpty()) {
            role = "Customer";
        } else {
            role = role.trim();
            if (role.equalsIgnoreCase("Admin")) {
                role = "Admin";
            } else if (role.equalsIgnoreCase("Manager")) {
                role = "Manager";
            } else {
                role = "Customer";
            }
        }
        
        System.out.println("  [updateUserFromFile] Updating " + username);
        System.out.println("  [updateUserFromFile] Role: '" + originalRole + "' → '" + role + "'");
        
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET role = ?, password = ?, email = ? WHERE username = ?")) {
            ps.setString(1, role);
            ps.setString(2, password != null ? password : "");
            ps.setString(3, email != null ? email : "");
            ps.setString(4, username);
            int rowsAffected = ps.executeUpdate();
            System.out.println("  [updateUserFromFile] Rows affected: " + rowsAffected);
            if (rowsAffected > 0) {
                System.out.println("  [updateUserFromFile] ✓ Update successful");
            } else {
                System.out.println("  [updateUserFromFile] ✗ No rows updated (user might not exist)");
            }
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.err.println("  [updateUserFromFile] Error updating user from file: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
}

