package edu.apu.crs.service;

import edu.apu.crs.models.Course;
import edu.apu.crs.models.CourseRecoveryPlan;
import edu.apu.crs.models.Milestone;
import edu.apu.crs.models.Program;
import edu.apu.crs.models.Score;
import edu.apu.crs.models.Student;

import java.io.*;
import java.net.URL;
import java.util.*;

public class CourseRecoveryService {

    private static final String DATA_FOLDER        = "data/";
    private static final String COURSE_LIST_FILE   = DATA_FOLDER + "courseList.txt";
    private static final String STUDENT_LIST_FILE  = DATA_FOLDER + "stuList.txt";
    private static final String SCORE_FILE         = DATA_FOLDER + "stuScore.txt";
    private static final String PROGRAM_LIST_FILE  = DATA_FOLDER + "programList.txt";
    private static final String PROGRAM_FILE       = DATA_FOLDER + "program.txt";
    private static final String MILESTONE_FILE     = DATA_FOLDER + "milestoneList.txt";
    private static final String RECOVERY_PLAN_FILE = DATA_FOLDER + "courseRecoveryPlan.txt";

    private Map<String, Course> courses = new HashMap<>();
    private Map<String, Student> students = new HashMap<>();
    private List<Score> scores = new ArrayList<>();
    private List<Milestone> milestones = new ArrayList<>();
    private List<CourseRecoveryPlan> recoveryPlans = new ArrayList<>();
    private Map<String, Program> programs = new HashMap<>();
    private Map<String, List<String>> programCourses = new HashMap<>();

    public CourseRecoveryService() {
        loadCourses();
        loadPrograms();
        loadProgramCourses();
        loadStudents();
        loadScores();
        loadMilestones();
        loadRecoveryPlans();
    }

    /* ===================== 1. LOAD TXT FILES ===================== */

    private void loadCourses() {
        courses.clear();
        for (String[] parts : readCsv(COURSE_LIST_FILE)) {
            if (parts.length < 4) {
                continue;
            }
            String courseId   = parts[0].trim();
            String courseName = parts[1].trim();
            int credits       = parseIntSafe(parts[2]);
            int semester      = parseIntSafe(parts[3]);

            Course course = new Course(courseId, courseName, credits, semester);
            courses.put(courseId, course);
        }
    }

    private void loadPrograms() {
        programs.clear();
        for (String[] parts : readCsv(PROGRAM_FILE)) {
            if (parts.length < 2) {
                continue;
            }
            String programId   = parts[0].trim();
            String programName = parts[1].trim();

            Program program = new Program(programId, programName);
            programs.put(programId, program);
        }
    }

    private void loadProgramCourses() {
        programCourses.clear();
        for (String[] parts : readCsv(PROGRAM_LIST_FILE)) {
            if (parts.length < 2) {
                continue;
            }
            String programId = parts[0].trim();
            String courseId  = parts[1].trim();

            List<String> list = programCourses.get(programId);
            if (list == null) {
                list = new ArrayList<>();
                programCourses.put(programId, list);
            }
            list.add(courseId);
        }
    }

    private void loadStudents() {
        students.clear();
        for (String[] parts : readCsv(STUDENT_LIST_FILE)) {
            // stuList: studentId, name, email, programId, programName, currentSemester
            if (parts.length < 6) {
                continue;
            }
            String studentId       = parts[0].trim();
            String studentName     = parts[1].trim();
            String email           = parts[2].trim();
            String programId       = parts[3].trim();
            int currentSemester    = parseIntSafe(parts[5]);

            Student student = new Student(studentId, studentName, email, programId, currentSemester);
            students.put(studentId, student);
        }
    }

    private void loadScores() {
        scores.clear();
        for (String[] parts : readCsv(SCORE_FILE)) {
            // stuScore: S001,C003,0,1,70,72,3.3,B+,PASS
            if (parts.length < 9) {
                continue;
            }
            String studentId      = parts[0].trim();
            String courseId       = parts[1].trim();
            int attempt           = parseIntSafe(parts[2]);
            int semester          = parseIntSafe(parts[3]);
            int assignmentScore   = parseIntSafe(parts[4]);
            int examScore         = parseIntSafe(parts[5]);
            double gradePoint     = parseDoubleSafe(parts[6]);  // 3.3
            String grade          = parts[7].trim();            // B+
            String status         = parts[8].trim();            // PASS / FAIL

            Score score = new Score(
                    studentId,
                    courseId,
                    attempt,
                    semester,
                    assignmentScore,
                    examScore,
                    grade,
                    gradePoint,
                    status
            );

            scores.add(score);

            Student stu = students.get(studentId);
            if (stu != null) {
                stu.addScore(score);
            }
        }

        // 计算每个学生有多少门 FAIL
        for (Student s : students.values()) {
            int failed = (int) s.getScores().stream()
                    .filter(sc -> "FAIL".equalsIgnoreCase(sc.getstatus()))
                    .count();
            s.setFailedCourseCount(failed);
        }
    }

    private void loadMilestones() {
        milestones.clear();
        for (String[] parts : readCsv(MILESTONE_FILE)) {
            // CR001,C001,1,Review database notes
            if (parts.length < 4) {
                continue;
            }
            String templateId = parts[0].trim();
            String courseId   = parts[1].trim();
            int week          = parseIntSafe(parts[2]);
            String task       = parts[3].trim();

            Milestone m = new Milestone(templateId, courseId, week, task);
            milestones.add(m);
        }
    }

    private void loadRecoveryPlans() {
        recoveryPlans.clear();
        for (String[] parts : readCsv(RECOVERY_PLAN_FILE)) {
            // P001,S001,C014,1,In Progress,NA
            if (parts.length < 5) {
                continue;
            }
            String planId        = parts[0].trim();
            String studentId     = parts[1].trim();
            String courseId      = parts[2].trim();
            int studyWeek        = parseIntSafe(parts[3]);
            String status        = parts[4].trim();
            String recommendation = parts.length > 5 ? parts[5].trim() : "";

            CourseRecoveryPlan plan = new CourseRecoveryPlan(
                    planId,
                    studentId,
                    courseId,
                    studyWeek,
                    status,
                    recommendation
            );
            recoveryPlans.add(plan);
        }
    }

    /* ===================== 2. QUERY METHODS ===================== */

    public List<Student> getStudentsWithFailedCourses() {
        List<Student> result = new ArrayList<>();
        for (Student s : students.values()) {
            // 用 failedCourseCount 或 Map 都可以，这里用 count > 0
            if (s.getFailedCourseCount() > 0) {
                result.add(s);
            }
        }
        result.sort(Comparator.comparing(Student::getStudentId));
        return result;
    }

    public List<Course> getFailedCoursesForStudent(String studentId) {
        List<Course> result = new ArrayList<>();
        Student stu = students.get(studentId);
        if (stu == null) {
            return result;
        }

        // Student.getFailedCourseCodes() 返回的是 Map<CourseId, ...>
        Map<String, String> failedMap = stu.getFailedCourseCodes();
        for (String courseId : failedMap.keySet()) {
            Course c = courses.get(courseId);
            if (c != null) {
                result.add(c);
            }
        }
        return result;
    }

    public List<Milestone> getMilestonesForCourse(String courseId) {
        List<Milestone> result = new ArrayList<>();
        for (Milestone m : milestones) {
            if (courseId.equals(m.getCourseId())) {
                result.add(m);
            }
        }
        // 按 week 排序
        result.sort(Comparator.comparingInt(Milestone::getStudyWeek));
        return result;
    }

    public CourseRecoveryPlan getOrCreateRecoveryPlan(String studentId, String courseId) {
        for (CourseRecoveryPlan plan : recoveryPlans) {
            if (studentId.equals(plan.getStudentId())
                    && courseId.equals(plan.getCourseId())) {
                return plan;
            }
        }

        String newPlanId = generateNextPlanId();
        CourseRecoveryPlan newPlan = new CourseRecoveryPlan(
                newPlanId,
                studentId,
                courseId,
                1,
                "Not Started",
                ""
        );
        recoveryPlans.add(newPlan);
        return newPlan;
    }

    /* ===================== 3. UPDATE METHODS ===================== */

    public void updateMilestoneStatus(String planId, String courseId, int week, String newStatus) {
        // 目前 CourseRecoveryPlan 只有 overall status，这里就更新 plan 的 status
        for (CourseRecoveryPlan plan : recoveryPlans) {
            if (planId.equals(plan.getPlanId())
                    && courseId.equals(plan.getCourseId())) {
                plan.setStatus(newStatus);
            }
        }
    }

    public void addRecommendation(String planId, String text) {
        updateRecommendation(planId, text);
    }

    public void updateRecommendation(String planId, String newText) {
        for (CourseRecoveryPlan plan : recoveryPlans) {
            if (planId.equals(plan.getPlanId())) {
                plan.setRecommendation(newText);
                break;
            }
        }
    }

    public void removeRecommendation(String planId) {
        for (CourseRecoveryPlan plan : recoveryPlans) {
            if (planId.equals(plan.getPlanId())) {
                plan.setRecommendation("");
                break;
            }
        }
    }

    /* ===================== 4. SAVE BACK TO TXT ===================== */

    public void saveRecoveryPlans() {
        List<String> lines = new ArrayList<>();
        for (CourseRecoveryPlan plan : recoveryPlans) {
            StringBuilder sb = new StringBuilder();
            sb.append(plan.getPlanId()).append(",");
            sb.append(plan.getStudentId()).append(",");
            sb.append(plan.getCourseId()).append(",");
            sb.append(plan.getStudyWeek()).append(",");
            sb.append(plan.getStatus() == null ? "" : plan.getStatus()).append(",");
            sb.append(plan.getRecommendation() == null ? "" : plan.getRecommendation());
            lines.add(sb.toString());
        }
        writeLines(RECOVERY_PLAN_FILE, lines);
    }

    public void updateScoreForRecovery(String studentId, String courseId, double newGradePoint) {
        for (Score s : scores) {
            if (studentId.equals(s.getstudentId())
                    && courseId.equals(s.getcourseId())) {
                s.setgradePoint(newGradePoint);
                if (newGradePoint >= 2.0) {
                    s.setstatus("PASS");
                } else {
                    s.setstatus("FAIL");
                }
            }
        }
        saveScores();

        Student stu = students.get(studentId);
        if (stu != null) {
            int failed = (int) stu.getScores().stream()
                    .filter(sc -> "FAIL".equalsIgnoreCase(sc.getstatus()))
                    .count();
            stu.setFailedCourseCount(failed);
        }
    }

    private void saveScores() {
        List<String> lines = new ArrayList<>();
        for (Score s : scores) {
            StringBuilder sb = new StringBuilder();
            sb.append(s.getstudentId()).append(",");
            sb.append(s.getcourseId()).append(",");
            sb.append(s.getattempt()).append(",");
            sb.append(s.getsemester()).append(",");
            sb.append(s.getassignmentScore()).append(",");
            sb.append(s.getexamScore()).append(",");
            sb.append(s.getgradePoint()).append(",");
            sb.append(s.getgrade()).append(",");
            sb.append(s.getstatus());
            lines.add(sb.toString());
        }
        writeLines(SCORE_FILE, lines);
    }

    /* =====================  Helper Methods  ===================== */

    private List<String[]> readCsv(String resourcePath) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = openReader(resourcePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] parts = trimmed.split(",", -1);
                rows.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private BufferedReader openReader(String resourcePath) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        }
        return new BufferedReader(new InputStreamReader(is));
    }

    private void writeLines(String resourcePath, List<String> lines) {
        try {
            URL url = getClass().getClassLoader().getResource(resourcePath);
            if (url == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }
            File file = new File(url.getPath());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String generateNextPlanId() {
        int max = 0;
        for (CourseRecoveryPlan plan : recoveryPlans) {
            String id = plan.getPlanId();
            if (id == null || id.length() < 2) {
                continue;
            }
            if (!id.startsWith("P")) {
                continue;
            }
            try {
                int n = Integer.parseInt(id.substring(1));
                if (n > max) {
                    max = n;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        int next = max + 1;
        return String.format("P%03d", next);
    }
}
