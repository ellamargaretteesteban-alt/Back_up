package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.HealthTip;

/**
 * HealthTipHandler handles health tip database operations.
 */
public class HealthTipHandler {
    
    private Connection connection;
    
    public HealthTipHandler(Connection connection) {
        this.connection = connection;
    }
    
    public void ensureHealthTipsTable() {
        if (connection == null) return;
        
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS health_tips ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "title VARCHAR(255) NOT NULL,"
                    + "content TEXT,"
                    + "link_source VARCHAR(500) NOT NULL)");
        } catch (Exception ex) {
            System.err.println("Error creating health_tips table: " + ex.getMessage());
        }
    }
    
    public boolean addHealthTip(HealthTip tip) {
        if (connection == null) return false;
        ensureHealthTipsTable();
        
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO health_tips(title, content, link_source) VALUES (?,?,?)")) {
            ps.setString(1, tip.title);
            ps.setString(2, tip.content);
            ps.setString(3, tip.linkSource);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error adding health tip: " + ex.getMessage());
            return false;
        }
    }
    
    public boolean updateHealthTip(int id, HealthTip tip) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE health_tips SET title = ?, content = ?, link_source = ? WHERE id = ?")) {
            ps.setString(1, tip.title);
            ps.setString(2, tip.content);
            ps.setString(3, tip.linkSource);
            ps.setInt(4, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error updating health tip: " + ex.getMessage());
            return false;
        }
    }
    
    public boolean deleteHealthTip(int id) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM health_tips WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error deleting health tip: " + ex.getMessage());
            return false;
        }
    }
    
    public List<HealthTip> getAllHealthTips() {
        List<HealthTip> tips = new ArrayList<>();
        if (connection == null) return tips;
        ensureHealthTipsTable();
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM health_tips ORDER BY id DESC")) {
            while (rs.next()) {
                tips.add(new HealthTip(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("link_source")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error getting health tips: " + ex.getMessage());
        }
        return tips;
    }
    
    public HealthTip getHealthTipById(int id) {
        if (connection == null) return null;
        ensureHealthTipsTable();
        
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM health_tips WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new HealthTip(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("link_source")
                );
            }
        } catch (SQLException ex) {
            System.err.println("Error getting health tip: " + ex.getMessage());
        }
        return null;
    }
}













