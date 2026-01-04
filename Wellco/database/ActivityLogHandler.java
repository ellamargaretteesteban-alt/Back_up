package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.ActivityLog;

/**
 * ActivityLogHandler handles all activity log database operations.
 */
public class ActivityLogHandler {
    
    private Connection connection;
    
    public ActivityLogHandler(Connection connection) {
        this.connection = connection;
    }
    
    public void ensureActivityLogTable() {
        if (connection == null) return;
        
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS activity_logs ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "admin_username VARCHAR(255) NOT NULL,"
                    + "action VARCHAR(50) NOT NULL,"
                    + "target VARCHAR(255),"
                    + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        } catch (Exception ex) {
            System.err.println("Error creating activity_logs table: " + ex.getMessage());
        }
    }
    
    public boolean logActivity(ActivityLog log) {
        if (connection == null) return false;
        ensureActivityLogTable();
        
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO activity_logs(admin_username, action, target) VALUES (?,?,?)")) {
            ps.setString(1, log.adminUsername);
            ps.setString(2, log.action);
            ps.setString(3, log.target);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error logging activity: " + ex.getMessage());
            return false;
        }
    }
    
    public List<ActivityLog> getAllActivityLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        if (connection == null) return logs;
        ensureActivityLogTable();
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 100")) {
            while (rs.next()) {
                logs.add(new ActivityLog(
                    rs.getInt("id"),
                    rs.getString("admin_username"),
                    rs.getString("action"),
                    rs.getString("target"),
                    rs.getTimestamp("timestamp")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error getting activity logs: " + ex.getMessage());
        }
        return logs;
    }
    
    public List<ActivityLog> getActivityLogsByAdmin(String adminUsername) {
        List<ActivityLog> logs = new ArrayList<>();
        if (connection == null) return logs;
        ensureActivityLogTable();
        
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM activity_logs WHERE admin_username = ? ORDER BY timestamp DESC LIMIT 100")) {
            ps.setString(1, adminUsername);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                logs.add(new ActivityLog(
                    rs.getInt("id"),
                    rs.getString("admin_username"),
                    rs.getString("action"),
                    rs.getString("target"),
                    rs.getTimestamp("timestamp")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error getting activity logs: " + ex.getMessage());
        }
        return logs;
    }
}













