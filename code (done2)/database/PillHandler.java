package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Pill;

/**
 * PillHandler handles medication/pill database operations.
 */
public class PillHandler {
    
    private Connection connection;
    
    public PillHandler(Connection connection) {
        this.connection = connection;
    }
    
    public void ensurePillsTable() {
        if (connection == null) return;
        
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS pills ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "name VARCHAR(255) NOT NULL UNIQUE,"
                    + "description TEXT,"
                    + "recommendation TEXT)");
        } catch (Exception ex) {
            System.err.println("Error creating pills table: " + ex.getMessage());
        }
    }
    
    public boolean addPill(Pill pill) {
        if (connection == null) return false;
        ensurePillsTable();
        
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO pills(name, description, recommendation) VALUES (?,?,?)")) {
            ps.setString(1, pill.name);
            ps.setString(2, pill.description);
            ps.setString(3, pill.recommendation);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error adding pill: " + ex.getMessage());
            return false;
        }
    }
    
    public boolean updatePill(String oldName, Pill pill) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE pills SET name = ?, description = ?, recommendation = ? WHERE name = ?")) {
            ps.setString(1, pill.name);
            ps.setString(2, pill.description);
            ps.setString(3, pill.recommendation);
            ps.setString(4, oldName);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error updating pill: " + ex.getMessage());
            return false;
        }
    }
    
    public boolean deletePill(String name) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM pills WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error deleting pill: " + ex.getMessage());
            return false;
        }
    }
    
    public List<Pill> getAllPills() {
        List<Pill> pills = new ArrayList<>();
        if (connection == null) return pills;
        ensurePillsTable();
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM pills ORDER BY name")) {
            while (rs.next()) {
                pills.add(new Pill(
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("recommendation")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error getting pills: " + ex.getMessage());
        }
        return pills;
    }
    
    public Pill getPillByName(String name) {
        if (connection == null) return null;
        ensurePillsTable();
        
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM pills WHERE name = ?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Pill(
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("recommendation")
                );
            }
        } catch (SQLException ex) {
            System.err.println("Error getting pill: " + ex.getMessage());
        }
        return null;
    }
}













