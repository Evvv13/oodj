package edu.apu.crs.models;

public class Score {
    private String studentId;
    private String courseId;
    private int attempt;
    private int semester;
    private int assignmentScore;
    private int examScore;
    private String grade;
    private double gradePoint;
    private String status;

    public Score(String studentId, String courseId, int attempt, int semester,
        int assignmentScore, int examScore, String grade, double gradePoint, String status) {

        this.studentId = studentId;
        this.courseId = courseId;
        this.attempt = attempt;
        this.semester = semester;
        this.assignmentScore = assignmentScore;
        this.examScore = examScore;
        this.grade = grade;
        this.gradePoint = gradePoint;
        this.status = status;
        }

    // getters
    public String getstudentId() {
        return this.studentId;
    }
    public String getcourseId () {
        return this.courseId;
    }
    public int getattempt() {
        return this.attempt;
    }
    public int getsemester() {
        return this.semester;
    }
    public int getassignmentScore() {
        return this.assignmentScore;
    }
    public int getexamScore() {
        return this.examScore;
    }
    public String getgrade() {
        return this.grade;
    }
    public double getgradePoint() {
        return this.gradePoint;
    }
    public String getstatus() {
        return this.status;
    }

    // setters
    public void setstudentId(String studentId) {
        this.studentId = studentId;
    }
    public void setcourseId(String courseId) {
        this.courseId = courseId;
    }
    public void setattempt(int attempt) {
        this.attempt = attempt;
    }
    public void setsemester(int semester) {
        this.semester = semester;
    }
    public void setassignmentScore(int assignmentScore) {
        this.assignmentScore = assignmentScore;
    }
    public void setexamScore(int examScore) {
        this.examScore = examScore;
    }
    public void setgrade(String grade) {
        this.grade = grade;
    }
    public void setgradePoint(double gradePoint) {
        this.gradePoint = gradePoint;
    }
    public void setstatus(String status) {
        this.status = status;
    }


    public String updateStatus() {

            if (this.gradePoint >= 2.0) {
                this.status = "PASS";
            } else {
                this.status = "FAIL";
            }
            return this.status;
    }
    
    

    public double calGradePoint() {
        
        double totalscore = this.assignmentScore * 0.4 + this.examScore * 0.6;
        if (totalscore >= 80) {
            this.grade= "A+";
            this.gradePoint = 4.0;

        } else if (79 <= totalscore && totalscore >= 75) {
            this.grade= "A";
            this.gradePoint = 3.7;

        } else if (74 <= totalscore && totalscore >= 70) {
            this.grade= "B+";
            this.gradePoint = 3.3;

        } else if (69 <= totalscore && totalscore >= 65) {
            this.grade= "B";
            this.gradePoint = 3.0;

        } else if (64 <= totalscore && totalscore >= 60) {
            this.grade= "C+";
            this.gradePoint = 2.7;

        } else if (59 <= totalscore && totalscore >= 55) {
            this.grade= "C";
            this.gradePoint = 2.3;

        } else if (54 <= totalscore && totalscore >= 50) {
            this.grade= "C-";
            this.gradePoint = 2.0;

        
        } else {
            this.grade= "F";
            this.gradePoint = 0.0;
        }
        return this.gradePoint ;
    }



}
