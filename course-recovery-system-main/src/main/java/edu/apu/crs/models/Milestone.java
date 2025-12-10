package edu.apu.crs.models;

public class Milestone {

    private String courseRecoveryPlanId;
    private String courseId;
    private int studyWeek;
    private String task;

    public Milestone(String planTemplateId, String courseId, int studyWeek, String task) {
        this.courseRecoveryPlanId = planTemplateId;
        this.courseId = courseId;
        this.studyWeek = studyWeek;
        this.task = task;
    }

    // Getters
    public String getcourseRecoveryPlanId() {
        return courseRecoveryPlanId;
    }

    public String getCourseId() {
        return courseId;
    }

    public int getStudyWeek() {
        return studyWeek;
    }

    public String getTask() {
        return task;
    }

    // Setters
    public void setTask(String task) {
        this.task = task;
    }

    

}
