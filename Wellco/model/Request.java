package model;

import java.sql.Timestamp;

/**
 * Request model class representing manager requests to admin
 */
public class Request {
    public int id;
    public String type; // ADD_MEDICINE, EDIT_MEDICINE, REMOVE_MEDICINE, ADD_USER, EDIT_USER, REMOVE_USER, ADD_TIP, EDIT_TIP, REMOVE_TIP
    public String requesterUsername;
    public String status; // PENDING, APPROVED, REJECTED
    public String details; // JSON or text details of the request
    public Timestamp createdDate;
    public Timestamp processedDate;
    public String processedBy; // Admin username who processed it

    public Request(int id, String type, String requesterUsername, String status, String details,
                  Timestamp createdDate, Timestamp processedDate, String processedBy) {
        this.id = id;
        this.type = type;
        this.requesterUsername = requesterUsername;
        this.status = status;
        this.details = details;
        this.createdDate = createdDate;
        this.processedDate = processedDate;
        this.processedBy = processedBy;
    }

    public Request(String type, String requesterUsername, String details) {
        this(0, type, requesterUsername, "PENDING", details, new Timestamp(System.currentTimeMillis()), null, null);
    }
}
