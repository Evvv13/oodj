package edu.apu.crs.models;

public class Course {

    private String courseId;
    private String courseName;
    private int credits;
    private int semester;
    

    // Constructor
    public Course(String courseId, String courseName, int credits, int semester) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.semester = semester;
    }

    // Getters
    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCredits() {
        return credits;
    }

    public int getSemester() {
        return semester;
    }

    // Setters
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }



}
