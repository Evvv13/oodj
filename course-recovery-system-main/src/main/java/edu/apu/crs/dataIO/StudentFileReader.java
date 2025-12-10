package edu.apu.crs.dataIO;

import edu.apu.crs.models.Student;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudentFileReader extends baseDataReader{

    private static final String FILE_NAME = "stuList.txt";

    public static List<Student> readStudents() {
        List<Student> students = new ArrayList<>();
        try (BufferedReader br = new StudentFileReader().getReader(FILE_NAME)) {
            if (br == null) return students;

            String line;
            br.readLine(); // Skip header (StudentID,Student Name,Email,ProgramID,Program,currentSemester)
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                // Expects at least 6 parts
                if (parts.length >= 6) {
                    try {
                        String studentId = parts[0].trim();
                        String studentName = parts[1].trim();
                        String email = parts[2].trim();
                        String programId = parts[3].trim();
                        // String programName = parts[4].trim(); // Program name can be looked up later
                        int currentSemester = Integer.parseInt(parts[5].trim());
                        
                        students.add(new Student(studentId, studentName, email, programId, currentSemester));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping line with invalid semester format: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return students;
    }

}
