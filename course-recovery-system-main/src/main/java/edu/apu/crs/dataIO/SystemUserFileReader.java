package edu.apu.crs.dataIO;

import edu.apu.crs.usermanagement.Data.AcademicOfficer;
import edu.apu.crs.usermanagement.Data.CourseAdministrator;
import edu.apu.crs.usermanagement.Data.systemUser; // Assuming SystemUser is the class name

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SystemUserFileReader extends baseDataReader{


    private static final String FILE_NAME = "systemUserList.txt";

    // The method reads the file and returns a list of SystemUser objects (Polymorphism)
    public static List<systemUser> readAllUsers() {
        List<systemUser> users = new ArrayList<>();
        
        // Use an instance of the class to access the protected getReader() method
        try (BufferedReader br = new SystemUserFileReader().getReader(FILE_NAME)) {
            if (br == null) return users; // File not found or error occurred

            String line;
            

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip empty line
                
                String[] parts = line.split(",");

                if (parts.length < 5) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                String username = parts[1].trim();
                String password = parts[3].trim();
                String role = parts[4].trim();

                systemUser user = null; // Use SystemUser casing (Java standard)
                
                // Instantiation based on Role (Polymorphism at work)
                switch (role) {

                    case "Academic Officer":
                        user = new AcademicOfficer(username, password, role, true); 
                        break;

                    case "Course Admin":
                        user = new CourseAdministrator(username, password, role, true); 
                        break;
                        
                    default:
                        System.err.println("Skipping user with unknown role: " + role);
                        continue;
                }
                
                if (user != null) {
                    users.add(user);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading SystemUser data file: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }
}
