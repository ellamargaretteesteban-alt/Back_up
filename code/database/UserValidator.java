package database;

import java.sql.*;

/**
 * UserValidator handles validation checks for usernames and emails.
 */
public class UserValidator {
    
    private Connection connection;
    
    /**
     * Constructor
     * @param connection Database connection
     */
    public UserValidator(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Checks if username already exists
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE username = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception ex) {
            System.err.println("Error checking username: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Checks if email already exists
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE email = ?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception ex) {
            System.err.println("Error checking email: " + ex.getMessage());
        }
        return false;
    }
}

