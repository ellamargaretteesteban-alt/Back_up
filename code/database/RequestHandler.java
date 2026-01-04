package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Request;

/**
 * RequestHandler handles all request-related database operations.
 */
public class RequestHandler {
    
    private Connection connection;
    
    public RequestHandler(Connection connection) {
        this.connection = connection;
    }
    
    public void ensureRequestsTable() {
        if (connection == null) return;
        
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS requests ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "type VARCHAR(50) NOT NULL,"
                    + "requester_username VARCHAR(255) NOT NULL,"
                    + "status VARCHAR(20) DEFAULT 'PENDING',"
                    + "details TEXT,"
                    + "created_date DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "processed_date DATETIME,"
                    + "processed_by VARCHAR(255))");
        } catch (Exception ex) {
            System.err.println("Error creating requests table: " + ex.getMessage());
        }
    }
    
    public boolean createRequest(Request request) {
        if (connection == null) return false;
        ensureRequestsTable();
        
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO requests(type, requester_username, status, details) VALUES (?,?,?,?)")) {
            ps.setString(1, request.type);
            ps.setString(2, request.requesterUsername);
            ps.setString(3, request.status);
            ps.setString(4, request.details);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error creating request: " + ex.getMessage());
            return false;
        }
    }
    
    public List<Request> getAllRequests() {
        List<Request> requests = new ArrayList<>();
        if (connection == null) return requests;
        ensureRequestsTable();
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM requests ORDER BY created_date DESC")) {
            while (rs.next()) {
                requests.add(new Request(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getString("requester_username"),
                    rs.getString("status"),
                    rs.getString("details"),
                    rs.getTimestamp("created_date"),
                    rs.getTimestamp("processed_date"),
                    rs.getString("processed_by")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error getting requests: " + ex.getMessage());
        }
        return requests;
    }
    
    public List<Request> getPendingRequests() {
        List<Request> requests = new ArrayList<>();
        if (connection == null) return requests;
        ensureRequestsTable();
        
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM requests WHERE status = 'PENDING' ORDER BY created_date DESC")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                requests.add(new Request(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getString("requester_username"),
                    rs.getString("status"),
                    rs.getString("details"),
                    rs.getTimestamp("created_date"),
                    rs.getTimestamp("processed_date"),
                    rs.getString("processed_by")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error getting pending requests: " + ex.getMessage());
        }
        return requests;
    }
    
    public boolean updateRequestStatus(int requestId, String status, String processedBy) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE requests SET status = ?, processed_date = CURRENT_TIMESTAMP, processed_by = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setString(2, processedBy);
            ps.setInt(3, requestId);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error updating request status: " + ex.getMessage());
            return false;
        }
    }
    
    public boolean deleteRequest(int requestId) {
        if (connection == null) return false;
        
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM requests WHERE id = ?")) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Error deleting request: " + ex.getMessage());
            return false;
        }
    }
}













