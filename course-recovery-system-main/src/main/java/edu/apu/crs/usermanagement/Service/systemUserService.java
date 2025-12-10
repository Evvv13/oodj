package edu.apu.crs.usermanagement.Service;

import edu.apu.crs.usermanagement.Data.AcademicOfficer;
import edu.apu.crs.usermanagement.Data.CourseAdministrator;
import edu.apu.crs.usermanagement.Data.systemUser;
import edu.apu.crs.dataIO.SystemUserFileReader; // Imports the new I/O class
import java.io.DataOutputStream; // For binary logging (Phase III, Step 10)
import java.io.FileOutputStream; // For binary logging (Phase III, Step 10)
import java.io.IOException;
//import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class systemUserService {

    // List holds the abstract class SystemUser (Polymorphism)
    private List<systemUser> users;

    // Path for binary logging file (Phase III, Step 10)
    private static final String LOG_FILE = "logs/login_timestamps.dat";

    public systemUserService() {
        // REFACTOR: Remove ALL hardcoded users and load from file instead
        System.out.println("Loading System Users from file...");
        this.users = SystemUserFileReader.readAllUsers();
        System.out.println("Loaded " + this.users.size() + " system users.");
    }

    
    
    public systemUser login(String input, String password) {
        // Polymorphism: iterating over the abstract SystemUser list to find the
        // concrete subclass
        for (systemUser u : users) {
            boolean nameMatch = u.getUsername().equalsIgnoreCase(input);
            boolean idMatch = u.getUserID().equalsIgnoreCase(input);

            if ((nameMatch || idMatch) && u.getPassword().equals(password) && u.isActive()) {
                return u;
            }
        }
        return null;
    }

    /**
     * Logs the logout timestamp to a binary file. (Phase III, Step 10)
     *
     * @param username The username logging out.
     */
    public void logout(String username) {
        logTimestamp(username, false); // Log logout
    }

    /**
     * Private helper method to log timestamps to a binary file (Requirement 1.1.2)
     */
    private void logTimestamp(String username, boolean isLogin) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(LOG_FILE, true))) {
            long timestamp = new Date().getTime();
            String action = isLogin ? "LOGIN" : "LOGOUT";

            // Write to binary file: [timestamp] | [action] | [username]
            dos.writeLong(timestamp); // Log the timestamp in binary form
            dos.writeUTF("|" + action + "|" + username + "\n");
        } catch (IOException e) {
            System.err.println("Error logging timestamp: " + e.getMessage());
        }
    }

    /**
     * Adds a new SystemUser to the system, instantiating the correct subclass.
     * @param username New user's username.
     * @param password New user's password.
     * @param role User's role (e.g., "Academic Officer" or "Course Admin").
     * @param active Initial active status.
     */
    public void addUser(String userID ,String username,String email, String password, String role, boolean active) {
        // Use the role string to determine which subclass to instantiate (Inheritance & Polymorphism)
        systemUser newUser = null;
        String normalizedRole = role.toLowerCase().replace(" ", "");
        switch (normalizedRole) {
            // Role assignment based on assignment requirements
            case "courseadmin":
            case "courseadministrator":
                newUser = new CourseAdministrator(userID ,username, email,password, role, active);
                break;
            case "academicofficer":
                newUser = new AcademicOfficer(userID ,username,email, password, role, active);
                break;
            default:
                System.err.println("Attempted to add user with unsupported role: " + role);
                newUser = null; 
                break;
        }
        if (newUser != null) {
            users.add(newUser);
        }
    }

    public boolean removeUser(String username) {
        return users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
    }

    /**
     * Finds a SystemUser by username.
     * 
     * @param username The username to search for.
     * @return The SystemUser object, or null if not found.
     */
    public systemUser findUser(String username) {
        for (systemUser u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    
    public boolean updatePassword(String username, String newPassword) {
        systemUser u = findUser(username);
        if (u != null) {
            u.setPassword(newPassword); // Encapsulation: The SystemUser object changes its own state
            return true;
        }
        return false;
    }

    // The rest of the User Management methods...
    public boolean deactivateUser(String username) {
        systemUser u = findUser(username);
        if (u != null) {
            u.setActive(false);
            return true;
        }
        return false;
    }

    public boolean activateUser(String username) {
        systemUser u = findUser(username);
        if (u != null) {
            u.setActive(true);
            return true;
        }
        return false;
    }

    public List<systemUser> getAllUsers() {
        return users;
    }

    public void printAllUsers() {
        System.out.println("---- All System Users ----");
        for (systemUser u : users) {
            // Polymorphism: calling getRoleTitle() to get the specific role name
            System.out.println(u.getUsername() + " | " + u.getRoleTitle() + " | Active: " + u.isActive());
        }
        System.out.println("-------------------");
    }

    // find user email ( for password recovery )
    public systemUser findUserByEmail(String email) {
        for (systemUser u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

}