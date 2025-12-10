package edu.apu.crs.service;

import edu.apu.crs.models.Score;
import edu.apu.crs.models.Course;
import edu.apu.crs.models.Student;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EligibilityService {

    
    // using courseID to get the course credit hours for calculate cgpa
    private final Map<String, Integer> courseCredits;

    
    public EligibilityService(List<Course> allCourses) {
        
        this.courseCredits = allCourses.stream()
            .collect(Collectors.toMap(Course::getCourseId, Course::getCredits));
    }



    // --- Main Calculation Methods ---

    /**
     * Calculates the CGPA for a student based on their scores.
     *
     * @param scores List of all scores for a student.
     * @return Calculated CGPA.
     */
    public double calculateCGPA(List<Score> scores) {
        double totalGradePointsXCredits = 0.0;
        int totalCreditHours = 0;
        
        for (Score score : scores) {

            // get credit hour
            int credits = courseCredits.getOrDefault(score.getcourseId(), 0);
            
            // Check if status is FAIL (Grade Point is 0) to exclude failed courses from calculation if the assignment requires it.
            // NOTE: The standard CGPA calculation includes failed courses (Grade Point 0) unless retaken.
            // We use the recorded GradePoint regardless of status, as per CGPA definition.
            totalGradePointsXCredits += score.getgradePoint() * credits;
            totalCreditHours += credits;
        }
        
        if (totalCreditHours == 0) {
            return 0.0;
        }
        
        // Return rounded CGPA (in 2 decimal )
        return Math.round((totalGradePointsXCredits / totalCreditHours) * 100.0) / 100.0;
    }

    /**
     * Counts the number of distinct failed courses for a student.
     * @param scores List of all scores for a student.
     * @return Count of failed courses.
     */
    public int countFailedCourses(List<Score> scores) {
        // Filters scores where status is FAIL and counts them
        return (int) scores.stream()
            .filter(score -> "FAIL".equalsIgnoreCase(score.getstatus()))
            .count();
    }



    // --- Eligibility Decision Methods ---

    /**
     * @param cgpa Calculated CGPA.
     * @param failedCourseCount Count of courses marked as FAIL.
     * @return true if eligible, false otherwise.
     */
    public boolean isEligibleToProgress(double cgpa, int failedCourseCount) {

        // Criteria 1: At least CGPA 2.0
        boolean meetsCgpa = cgpa >= 2.0;
        
        // Criteria 2: Not more than three failed courses (<= 3)
        boolean meetsFailLimit = failedCourseCount <= 3;

        return meetsCgpa && meetsFailLimit;
    }

    /**
     * Processes all students, calculates their eligibility, and updates the student objects.
     * @param students List of all aggregated student objects.
     */
    public void processAllStudentEligibility(List<Student> students) {
        for (Student student : students) {

            // 1. Calculate CGPA
            double cgpa = calculateCGPA(student.getScores());
            student.setCurrentCGPA(cgpa);
            
            // 2. Calculate failed course count
            int failedCount = countFailedCourses(student.getScores());
            student.setFailedCourseCount(failedCount);
        }
    }

    /**
     * Gets a list of students who are ineligible to progress or have failed courses
     * and therefore need a recovery plan.
     * @param allStudents The master list of Student objects.
     * @return List of Student objects requiring intervention.
     */
    public List<Student> getStudentsNeedingRecovery(List<Student> allStudents) {
        return allStudents.stream()
            .filter(student -> !isEligibleToProgress(
                                    student.getCurrentCGPA(),
                                    student.getFailedCourseCount()) || student.getFailedCourseCount() > 0)
            .collect(Collectors.toList());
    }

}
