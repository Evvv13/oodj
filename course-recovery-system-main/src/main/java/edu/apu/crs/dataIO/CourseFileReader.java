package edu.apu.crs.dataIO;

import edu.apu.crs.models.Course;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CourseFileReader extends baseDataReader{

    private static final String FILE_NAME = "courseList.txt";

    public static List<Course> readCourses() {
        List<Course> courses = new ArrayList<>();
        try (BufferedReader br = new CourseFileReader().getReader(FILE_NAME)) {
            if (br == null) return courses;

            String line;
            br.readLine(); // Skip header (CourseID ,CourseName,Credit,Semester)
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                // Expects 4 parts: C001,Introduction to Databases,3,1
                if (parts.length >= 4) {
                    try {
                        String courseId = parts[0].trim();
                        String courseName = parts[1].trim();
                        int credits = Integer.parseInt(parts[2].trim());
                        int semester = Integer.parseInt(parts[3].trim());
                        courses.add(new Course(courseId, courseName, credits, semester));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping line with invalid number format: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return courses;
    }



}
