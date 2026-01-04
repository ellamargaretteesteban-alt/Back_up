package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AccountsFileReader reads account data from accounts.txt file.
 * Supports loading accounts into both SQL and ArrayList storage.
 */
public class AccountsFileReader {
    
    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Reads all accounts from accounts.txt file
     * Format: ROLE, USERNAME, Password, Email, CreatedDate, OriginalUsername
     * @return List of UserData objects parsed from file
     */
    public List<UserData> readAccountsFromFile() {
        List<UserData> accounts = new ArrayList<>();
        File file = new File(ACCOUNTS_FILE);
        
        if (!file.exists()) {
            System.out.println("→ accounts.txt not found. Starting with empty accounts.");
            return accounts;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines, headers, and separators
                if (line.isEmpty() || 
                    line.startsWith("═") || 
                    line.startsWith("─") || 
                    line.startsWith("ACCOUNT LIST") ||
                    line.startsWith("ROLE") ||
                    line.startsWith("Example:") ||
                    line.startsWith("Total Accounts") ||
                    line.startsWith("Last Updated") ||
                    line.startsWith("This file is automatically") ||
                    line.startsWith("PURPOSE:") ||
                    line.startsWith("FILE FORMAT:") ||
                    line.startsWith("ALTERNATIVE FORMAT:") ||
                    line.startsWith("IMPORTANT NOTES:") ||
                    line.startsWith("ACCOUNT DATA:") ||
                    line.startsWith("AUTOMATIC UPDATES:")) {
                    continue;
                }
                
                // Parse account line
                // Format: ROLE | USERNAME | PASSWORD | EMAIL | CREATED DATE | ORIGINAL USERNAME
                // Or CSV format: ROLE, USERNAME, Password, Email, CreatedDate, OriginalUsername
                UserData account = parseAccountLine(line);
                if (account != null) {
                    // Normalize role before adding
                    String role = account.role != null ? account.role.trim() : "Customer";
                    if (role.equalsIgnoreCase("Admin")) {
                        role = "Admin";
                    } else if (role.equalsIgnoreCase("Manager")) {
                        role = "Manager";
                    } else {
                        role = "Customer";
                    }
                    
                    // Create new UserData with normalized role if needed
                    if (!role.equals(account.role)) {
                        String originalPassword = account.password;
                        account = new UserData(
                            role,
                            account.username,
                            account.email,
                            account.createdDate,
                            account.originalUsername,
                            account.name,
                            account.age
                        );
                        account.password = originalPassword;
                    }
                    
                    accounts.add(account);
                    // Debug output for Admin/Manager accounts
                    if ("Admin".equals(account.role) || "Manager".equals(account.role)) {
                        System.out.println("  → Parsed " + account.role + " account from file: " + account.username);
                    }
                } else if ((line.contains("|") || line.contains(",")) && !line.startsWith("Example:")) {
                    // Log if a line looks like it should be parsed but failed
                    String[] testParts = line.contains("|") ? line.split("\\|") : line.split(",");
                    if (testParts.length >= 4) { // At least has some data
                        System.out.println("  ⚠ Failed to parse line (might be invalid format): " + line.substring(0, Math.min(80, line.length())));
                        System.out.println("     Parts found: " + testParts.length + " (expected 6)");
                    }
                }
            }
            
            System.out.println("✓ Loaded " + accounts.size() + " accounts from accounts.txt");
            return accounts;
            
        } catch (IOException ex) {
            System.err.println("Error reading accounts.txt: " + ex.getMessage());
            return accounts;
        }
    }
    
    /**
     * Parses a single line from accounts.txt
     * Supports both pipe-separated (|) and comma-separated (,) formats
     * @param line Line to parse
     * @return UserData object or null if parsing fails
     */
    private UserData parseAccountLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        try {
            String[] parts;
            
            // Check if it's pipe-separated format (from NotepadWriter)
            if (line.contains("|")) {
                parts = line.split("\\|");
                if (parts.length < 6) {
                    // Try to handle cases where there might be extra separators
                    return null;
                }
                
                // Trim each part
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                }
            } 
            // Check if it's comma-separated format
            else if (line.contains(",")) {
                parts = line.split(",");
                if (parts.length < 6) {
                    return null;
                }
                
                // Trim each part
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                }
            } 
            else {
                return null; // Unknown format
            }
            
            // Parse fields: ROLE, USERNAME, Password, Email, CreatedDate, OriginalUsername
            String role = parts[0] != null ? parts[0].trim() : "";
            String username = parts[1] != null ? parts[1].trim() : "";
            String password = parts[2] != null ? parts[2].trim() : "";
            String email = parts[3] != null ? parts[3].trim() : "";
            String createdDateStr = parts[4] != null ? parts[4].trim() : "";
            String originalUsername = parts.length > 5 && parts[5] != null ? parts[5].trim() : username;
            
            // Skip if username is empty (check early)
            if (username == null || username.isEmpty()) {
                return null;
            }
            
            // Normalize and validate role
            if (role == null || role.isEmpty()) {
                role = "Customer"; // Default to Customer if role is empty
            } else {
                role = role.trim();
                // Normalize role to proper case
                if (role.equalsIgnoreCase("Admin")) {
                    role = "Admin";
                } else if (role.equalsIgnoreCase("Manager")) {
                    role = "Manager";
                } else if (role.equalsIgnoreCase("Customer")) {
                    role = "Customer";
                } else {
                    // Invalid role, default to Customer but log warning
                    System.out.println("Warning: Invalid role '" + role + "' for user '" + username + "'. Defaulting to Customer.");
                    role = "Customer";
                }
            }
            
            // Parse created date
            Timestamp createdDate = null;
            if (createdDateStr != null && !createdDateStr.isEmpty() && !createdDateStr.equals("N/A")) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(createdDateStr, DATE_FORMATTER);
                    createdDate = Timestamp.valueOf(dateTime);
                } catch (Exception e) {
                    // If parsing fails, use current timestamp
                    createdDate = Timestamp.valueOf(LocalDateTime.now());
                }
            } else {
                createdDate = Timestamp.valueOf(LocalDateTime.now());
            }
            
            // Create UserData object
            UserData user = new UserData(
                role,
                username,
                email,
                createdDate,
                originalUsername.isEmpty() ? username : originalUsername,
                null, // name
                0      // age
            );
            user.password = password;
            
            return user;
            
        } catch (Exception ex) {
            System.err.println("Error parsing account line: " + line);
            System.err.println("Error: " + ex.getMessage());
            return null;
        }
    }
    
    /**
     * Checks if accounts.txt file exists
     * @return true if file exists, false otherwise
     */
    public boolean fileExists() {
        return new File(ACCOUNTS_FILE).exists();
    }
}

