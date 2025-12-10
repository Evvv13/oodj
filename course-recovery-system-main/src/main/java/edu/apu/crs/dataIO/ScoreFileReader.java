package edu.apu.crs.dataIO;

import edu.apu.crs.models.Score;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ScoreFileReader extends baseDataReader {

    
    private static final String FILE_NAME = "stuScore.txt";

    public static List<Score> readScores() {
        List<Score> scores = new ArrayList<>();
        try (BufferedReader br = new ScoreFileReader().getReader(FILE_NAME)) {
            if (br == null) return scores;

            String line;
            br.readLine(); // Skip header (StudentID,CourseID,Attempt,Semester,AssignmentScore,FinalExamScore,GradePoint,Grade,Status)
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                // Expects 9 parts
                if (parts.length >= 9) {
                    try {
                        String studentId = parts[0].trim();
                        String courseId = parts[1].trim();
                        int attempt = Integer.parseInt(parts[2].trim());
                        int semester = Integer.parseInt(parts[3].trim());
                        int assignmentScore = Integer.parseInt(parts[4].trim());
                        int examScore = Integer.parseInt(parts[5].trim());
                        double gradePoint = Double.parseDouble(parts[6].trim());
                        String grade = parts[7].trim();
                        String status = parts[8].trim(); 
                        
                        // NOTE: The Score class's constructor currently accepts GradePoint/Grade/Status as input.
                        // It also has calGradePoint() which calculates/overwrites them.
                        // We will rely on the data file for now, but the Service layer will re-calculate/validate this.
                        scores.add(new Score(
                            studentId, courseId, attempt, semester,
                            assignmentScore, examScore, grade, gradePoint, status
                        ));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping line with invalid numeric format: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scores;
    }
}