package database;

import java.sql.Timestamp;

/**
 * UserData class to hold user information.
 */
public class UserData {
    public final String role;
    public final String username;
    public final String email;
    public final Timestamp createdDate;
    public final String originalUsername;
    public final String name;
    public final int age;
    public String password; // For notepad file generation
    
    // New fields
    public final String nickname; // Nickname separate from username
    public final String status; // "Active", "Inactive", "Suspended"

    // Constructor with all fields
    public UserData(String role, String username, String email, Timestamp createdDate,
                   String originalUsername, String name, int age, String nickname, String status) {
        this.role = role;
        this.username = username;
        this.email = email;
        this.createdDate = createdDate;
        this.originalUsername = originalUsername;
        this.name = name;
        this.age = age;
        this.nickname = nickname;
        this.status = status != null ? status : "Active"; // Default to Active
    }

    // Backward compatibility constructor (without nickname and status)
    public UserData(String role, String username, String email, Timestamp createdDate,
                   String originalUsername, String name, int age) {
        this(role, username, email, createdDate, originalUsername, name, age, null, "Active");
    }
}
