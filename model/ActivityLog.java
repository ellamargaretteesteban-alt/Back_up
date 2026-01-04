package model;

import java.sql.Timestamp;

/**
 * ActivityLog data class representing admin and manager activity.
 */
public class ActivityLog {
    public int id;
    public String actorUsername; // Can be Admin or Manager
    public String actorRole; // "Admin" or "Manager"
    public String action; // "ADD_MEDICINE", "EDIT_MEDICINE", "DELETE_MEDICINE", "EDIT_USER", "DELETE_USER", "CHANGE_ROLE", "RESET_PASSWORD", "APPROVE_REQUEST", "REJECT_REQUEST", "CREATE_REQUEST", "CANCEL_REQUEST"
    public String target; // What was affected (e.g., "username: john_doe", "medicine: Biogesic")
    public Timestamp timestamp;

    public ActivityLog(int id, String actorUsername, String actorRole, String action, String target, Timestamp timestamp) {
        this.id = id;
        this.actorUsername = actorUsername;
        this.actorRole = actorRole;
        this.action = action;
        this.target = target;
        this.timestamp = timestamp;
    }

    public ActivityLog(String actorUsername, String actorRole, String action, String target) {
        this(0, actorUsername, actorRole, action, target, new Timestamp(System.currentTimeMillis()));
    }
}
