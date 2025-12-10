package edu.apu.crs.models;

public class CourseRecoveryPlan {

    private String planId;
    private String studentId;
    private String courseId;
    private int studyWeek;
    private String status;
    private String recommendation;

    public CourseRecoveryPlan(String planId, String studentId, String courseId, int studyWeek, String status, String recommendation) {
        this.planId = planId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.studyWeek = studyWeek;
        this.status = status;
        this.recommendation = recommendation;
    }

    // Getters
    public String getPlanId() {
        return planId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public int getStudyWeek() {
        return studyWeek;
    }

    public String getStatus() {
        return status;
    }

    public String getRecommendation() {
        return recommendation;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

}
