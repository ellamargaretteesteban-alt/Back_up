package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.ManagerPermission;

/**
 * ManagerPermissionHandler handles manager permission database operations.
 */
public class ManagerPermissionHandler {
    
    private Connection connection;
    
    public ManagerPermissionHandler(Connection connection) {
        this.connection = connection;
    }
    
    public void ensureManagerPermissionsTable() {
        if (connection == null) return;
        
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS manager_permissions ("
                    + "manager_username VARCHAR(255) PRIMARY KEY,"
                    + "can_request_medicine BOOLEAN DEFAULT TRUE,"
                    + "can_request_user BOOLEAN DEFAULT TRUE,"
                    + "can_request_health_tip BOOLEAN DEFAULT TRUE)");
        } catch (Exception ex) {
            System.err.println("Error creating manager_permissions table: " + ex.getMessage());
        }
    }
    
    public boolean savePermission(ManagerPermission permission) {
        if (connection == null) return false;
        ensureManagerPermissionsTable();
        
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO manager_permissions(manager_username, can_request_medicine, can_request_user, can_request_health_tip) "
                + "VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE "
                + "can_request_medicine = ?, can_request_user = ?, can_request_health_tip = ?")) {
            ps.setString(1, permission.managerUsername);
            ps.setBoolean(2, permission.canRequestMedicine);
            ps.setBoolean(3, permission.canRequestUser);
            ps.setBoolean(4, permission.canRequestHealthTip);
            ps.setBoolean(5, permission.canRequestMedicine);
            ps.setBoolean(6, permission.canRequestUser);
            ps.setBoolean(7, permission.canRequestHealthTip);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error saving permission: " + ex.getMessage());
            return false;
        }
    }
    
    public ManagerPermission getPermission(String managerUsername) {
        if (connection == null) return new ManagerPermission(managerUsername);
        ensureManagerPermissionsTable();
        
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM manager_permissions WHERE manager_username = ?")) {
            ps.setString(1, managerUsername);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ManagerPermission(
                    rs.getString("manager_username"),
                    rs.getBoolean("can_request_medicine"),
                    rs.getBoolean("can_request_user"),
                    rs.getBoolean("can_request_health_tip")
                );
            }
        } catch (SQLException ex) {
            System.err.println("Error getting permission: " + ex.getMessage());
        }
        return new ManagerPermission(managerUsername); // Default: all enabled
    }
    
    public List<ManagerPermission> getAllPermissions() {
        List<ManagerPermission> permissions = new ArrayList<>();
        if (connection == null) return permissions;
        ensureManagerPermissionsTable();
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM manager_permissions")) {
            while (rs.next()) {
                permissions.add(new ManagerPermission(
                    rs.getString("manager_username"),
                    rs.getBoolean("can_request_medicine"),
                    rs.getBoolean("can_request_user"),
                    rs.getBoolean("can_request_health_tip")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error getting permissions: " + ex.getMessage());
        }
        return permissions;
    }
}













