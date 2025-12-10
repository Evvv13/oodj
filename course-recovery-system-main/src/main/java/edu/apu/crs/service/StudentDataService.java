package edu.apu.crs.service;

import edu.apu.crs.dataIO.StudentFileReader;
import edu.apu.crs.dataIO.ScoreFileReader;
import edu.apu.crs.models.Student;
import edu.apu.crs.models.Score;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// This service is responsible for aggregating raw data into usable Student objects 
// (Aggregation)

public class StudentDataService {

    private final List<Student> students; // List of all students

    public StudentDataService() {
        // 1. Load raw student and score data
        List<Student> rawStudents = StudentFileReader.readStudents();
        List<Score> rawScores = ScoreFileReader.readScores();
        
        // 2. Group scores by StudentID (Efficient lookup)
        Map<String, List<Score>> scoresByStudentId = rawScores.stream()
            .collect(Collectors.groupingBy(Score::getstudentId));
            
        // 3. Aggregate: Add scores to the corresponding student object
        for (Student student : rawStudents) {
            List<Score> studentScores = scoresByStudentId.getOrDefault(student.getStudentId(), List.of());
            for (Score score : studentScores) {
                student.addScore(score); // Aggregation: Student HAS-A List of Scores
            }
        }
        
        this.students = rawStudents;
    }

    // Getter to provide the complete list of aggregated students to other services (like EligibilityService)
    public List<Student> getAllStudents() {
        return students;
    }

    // Helper method to find a single student by ID
    public Student findStudentById(String studentId) {
        return students.stream()
            .filter(s -> s.getStudentId().equalsIgnoreCase(studentId))
            .findFirst()
            .orElse(null);
    }



}
