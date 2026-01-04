package database;

import java.sql.*;

/**
 * ProfileManager handles user profile operations (save, update, delete).
 */
public class ProfileManager {
    
    private Connection connection;
    
    /**
     * Constructor
     * @param connection Database connection
     */
    public ProfileManager(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Saves/updates user profile information
     * @param username Username
     * @param name User's name
     * @param email User's email
     * @param age User's age
     * @return true if save successful, false otherwise
     */
    public boolean saveUserProfile(String username, String name, String email, int age) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET name = ?, email = ?, age = ? WHERE username = ?")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, age);
            ps.setString(4, username);
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            System.err.println("Error saving user profile: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a user account
     * @param username Username to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteUser(String username) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM users WHERE username = ?")) {
            ps.setString(1, username);
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            System.err.println("Error deleting user: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes all users from the database
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteAllUsers() {
        if (connection == null) return false;
        
        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM users");
            return true;
        } catch (Exception ex) {
            System.err.println("Error deleting users: " + ex.getMessage());
            return false;
        }
    }
}

