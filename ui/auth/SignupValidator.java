package ui.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SignupValidator handles validation for user registration.
 */
public class SignupValidator {
    
    /**
     * Validates signup form inputs and returns a list of error messages
     * @param username Username
     * @param email Email
     * @param password Password
     * @param confirmPassword Confirm password
     * @return List of error messages (empty if valid)
     */
    public static List<String> validate(String username, String email, String password, String confirmPassword) {
        List<String> errors = new ArrayList<>();
        
        // Username validation: no spaces, 4-25 characters
        if (username.isEmpty()) {
            errors.add("Username: Required");
        } else {
            if (username.contains(" ")) {
                errors.add("Username: No spaces allowed");
            }
            if (username.length() < 4) {
                errors.add("Username: Must be more than 4 characters");
            }
            if (username.length() > 25) {
                errors.add("Username: Must be less than 25 characters");
            }
        }

        // Email validation: (name)@(name).com format
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\\.com$");
        if (email.isEmpty()) {
            errors.add("Email: Required");
        } else if (!emailPattern.matcher(email).matches()) {
            errors.add("Email: Must be in format (name)@(name).com");
        }

        // Password validation: 8-50 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char
        if (password.isEmpty()) {
            errors.add("Password: Required");
        } else {
            if (password.length() < 8) {
                errors.add("Password: Must be at least 8 characters");
            }
            if (password.length() > 50) {
                errors.add("Password: Must be at most 50 characters");
            }
            boolean hasNumber = password.matches(".*[0-9].*");
            boolean hasUpper = password.matches(".*[A-Z].*");
            boolean hasLower = password.matches(".*[a-z].*");
            boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
            
            if (!hasNumber) {
                errors.add("Password: Must contain at least 1 numerical character");
            }
            if (!hasUpper) {
                errors.add("Password: Must contain at least 1 uppercase character");
            }
            if (!hasLower) {
                errors.add("Password: Must contain at least 1 lowercase character");
            }
            if (!hasSpecial) {
                errors.add("Password: Must contain at least 1 special character");
            }
        }

        // Confirm password
        if (!password.equals(confirmPassword)) {
            errors.add("Confirm Password: Passwords do not match");
        }

        return errors;
    }
}

