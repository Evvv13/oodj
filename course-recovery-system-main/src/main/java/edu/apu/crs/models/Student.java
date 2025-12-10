
package edu.apu.crs.models;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Student {
    private String studentId;
    private String studentName;
    private String email;
    private String ProgramID;
    private int currentSemester;

    // AGGREGATION/COMPOSITION
    // each Student object holds multiple of their Score objects.

    private List<Score> scores = new ArrayList<>();

    
    // temporary field for Eligibility check
    private double currentCGPA = 0.0;
    private int failedCourseCount = 0;

    public Student(String studentId, String studentName, String email, String ProgramID, int currentSemester) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.ProgramID = ProgramID;
        this.currentSemester = currentSemester;
    }


    // getter
    public String getStudentId() {
        return studentId;
    }
    public String getStudentName() {
        return studentName;
    }
    public String getEmail() {
        return email;
    }
    public String getProgramID() {
        return ProgramID;
    }
    public int getCurrentSemester() {
        return currentSemester;
    }


    public List<Score> getScores() {
        return scores;
    }
    public double getCurrentCGPA() {
        return currentCGPA;
    }
    public int getFailedCourseCount() {
        return failedCourseCount;
    }


    // setter
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setProgramID(String ProgramID) {
        this.ProgramID = ProgramID;
    }
    public void setCurrentSemester(int currentSemester) {
        this.currentSemester = currentSemester;
    }


    // method
    public void addScore(Score score) {
        this.scores.add(score);
    }

    // *NEW CALCULATED DATA SETTERS:*
    public void setCurrentCGPA(double currentCGPA) {
        this.currentCGPA = currentCGPA;
    }
    public void setFailedCourseCount(int failedCourseCount) {
        this.failedCourseCount = failedCourseCount;
    }


    /// wan teng look thisssssss
    
    /**
     * Retrieves a map of failed courses (CourseID -> CourseName) for this student.
     * This relies on having the Course Name data available elsewhere (e.g., in the GUI or another service).
     * For now, we return a map of CourseID to a dummy name.
     * @return Map of CourseID to status ("FAIL").
     */
    public Map<String, String> getFailedCourseCodes() {
        return this.scores.stream()
            .filter(score -> "FAIL".equalsIgnoreCase(score.getstatus()))
            // Map CourseID to the status for display (or integrate with Course model for Name lookup later)
            .collect(Collectors.toMap(Score::getcourseId, score -> score.getgrade() + " (" + score.getgradePoint() + ")"));
    }
}


