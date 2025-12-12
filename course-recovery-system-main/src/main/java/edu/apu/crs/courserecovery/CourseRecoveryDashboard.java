package edu.apu.crs.courserecovery;

import edu.apu.crs.models.Student;
import edu.apu.crs.models.Course;
import edu.apu.crs.models.Milestone;
import edu.apu.crs.models.CourseRecoveryPlan;
import edu.apu.crs.service.MasterDataService;
import edu.apu.crs.service.CourseRecoveryService;
import edu.apu.crs.usermanagement.Data.systemUser;
import edu.apu.crs.service.EligibilityService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseRecoveryDashboard extends JFrame {

    private final systemUser currentUser;
    private final MasterDataService masterDataService;
    private final CourseRecoveryService courseRecoveryService;

    private CardLayout cardLayout;
    private JPanel mainContainer;

    private DefaultTableModel eligibilityModel;
    private JComboBox<String> eligibilityFilterCombo;
    private JTextField searchField;

    private JComboBox<Student> studentCombo;
    private JComboBox<Course> courseCombo;
    private JLabel planInfoLabel;
    private JTable milestoneTable;
    private DefaultTableModel milestoneTableModel;
    private String currentPlanId = null;

    public CourseRecoveryDashboard(systemUser user) {
        this.currentUser = user;
        this.masterDataService = new MasterDataService();
        this.courseRecoveryService = new CourseRecoveryService();

        setTitle("CRS Dashboard - " + user.getRoleTitle());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(createMenuPanel(), "MENU");
        mainContainer.add(buildEligibilityPanel(), "ELIGIBILITY");
        mainContainer.add(buildRecoveryPanel(), "RECOVERY");
        mainContainer.add(buildReportPanel(), "REPORT");
        mainContainer.add(buildUserManagementPanel(), "USER_MANAGE");

        add(mainContainer);
        cardLayout.show(mainContainer, "MENU");
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        JLabel welcome = new JLabel(
                "Welcome, " + currentUser.getUsername() +
                        " (" + currentUser.getRoleTitle() + ")",
                SwingConstants.CENTER
        );
        welcome.setFont(new Font("Arial", Font.BOLD, 18));
        menuPanel.add(welcome);

        String role = currentUser.getRoleTitle();

        if (role.equalsIgnoreCase("Academic Officer")) {
            addButton(menuPanel, "Check Eligibility", "ELIGIBILITY");
            addButton(menuPanel, "Manage Recovery Plans", "RECOVERY");
            addButton(menuPanel, "Academic Reports", "REPORT");
        }

        if (role.equalsIgnoreCase("Course Administrator")
                || role.equalsIgnoreCase("Course Admin")) {
            addButton(menuPanel, "User Management", "USER_MANAGE");
        }

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new edu.apu.crs.usermanagement.LoginPage().setVisible(true);
        });
        menuPanel.add(logoutBtn);

        return menuPanel;
    }

    private void addButton(JPanel panel, String label, String cardName) {
        JButton btn = new JButton(label);
        btn.addActionListener(e -> cardLayout.show(mainContainer, cardName));
        panel.add(btn);
    }

    private JPanel createHeaderPanel(String title) {
        JPanel header = new JPanel(new BorderLayout());
        JButton backBtn = new JButton("<< Back");
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        header.add(backBtn, BorderLayout.WEST);
        header.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.CENTER);
        return header;
    }

    // 1. ELIGIBILITY PANEL
    private JPanel buildEligibilityPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));

        JLabel filterLabel = new JLabel("Filter by Status:");
        eligibilityFilterCombo = new JComboBox<>(
                new String[]{"All Students", "Eligible Only", "Needs Recovery Only"}
        );

        JLabel searchLabel = new JLabel("Search by Student ID:");
        searchField = new JTextField(10);
        JButton searchBtn = new JButton("Search");

        JButton refreshBtn = new JButton("Refresh Data");

        controlPanel.add(filterLabel);
        controlPanel.add(eligibilityFilterCombo);
        controlPanel.add(searchLabel);
        controlPanel.add(searchField);
        controlPanel.add(searchBtn);
        controlPanel.add(refreshBtn);

        panel.add(controlPanel, BorderLayout.NORTH);

        eligibilityModel = new DefaultTableModel(
                new String[]{"Student ID", "Name", "CGPA", "Failed Courses", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(eligibilityModel);
        table.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        filterAndLoadData();

        searchBtn.addActionListener(e -> searchStudentData(searchField.getText().trim()));

        eligibilityFilterCombo.addActionListener(e -> filterAndLoadData());

        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            filterAndLoadData();
        });

        return panel;
    }

    private void filterAndLoadData() {
        eligibilityModel.setRowCount(0);
        List<Student> allStudents = masterDataService.getAllProcessedStudents();
        String selectedFilter = (String) eligibilityFilterCombo.getSelectedItem();

        for (Student s : allStudents) {
            boolean isEligible =
                    (s.getCurrentCGPA() >= 2.0 && s.getFailedCourseCount() <= 3);
            String statusText = isEligible ? "Eligible" : "Needs Recovery";

            boolean showStudent = false;
            switch (selectedFilter) {
                case "All Students":
                    showStudent = true;
                    break;
                case "Eligible Only":
                    showStudent = isEligible;
                    break;
                case "Needs Recovery Only":
                    showStudent = !isEligible;
                    break;
            }

            if (showStudent) {
                eligibilityModel.addRow(new Object[]{
                        s.getStudentId(),
                        s.getStudentName(),
                        String.format("%.2f", s.getCurrentCGPA()),
                        s.getFailedCourseCount(),
                        statusText
                });
            }
        }
    }

    private void searchStudentData(String studentId) {
        if (studentId.isEmpty()) {
            filterAndLoadData();
            return;
        }

        eligibilityModel.setRowCount(0);
        Student s = masterDataService.findStudentById(studentId);

        if (s != null) {
            boolean isEligible =
                    (s.getCurrentCGPA() >= 2.0 && s.getFailedCourseCount() <= 3);
            String statusText = isEligible ? "Eligible" : "Needs Recovery";

            eligibilityModel.addRow(new Object[]{
                    s.getStudentId(),
                    s.getStudentName(),
                    String.format("%.2f", s.getCurrentCGPA()),
                    s.getFailedCourseCount(),
                    statusText
            });
            eligibilityFilterCombo.setSelectedItem("All Students");
        } else {
            eligibilityModel.addRow(new Object[]{
                    studentId,
                    "Student Not Found",
                    "N/A",
                    "N/A",
                    "N/A"
            });
            JOptionPane.showMessageDialog(
                    this,
                    "Student ID '" + studentId + "' was not found.",
                    "Search Result",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    // 2. RECOVERY PANEL
    private JPanel buildRecoveryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.add(createHeaderPanel("Course Recovery Management"), BorderLayout.NORTH);

        studentCombo = new JComboBox<>();
        courseCombo = new JComboBox<>();

        JButton loadBtn = new JButton("Load Plan");
        JButton saveBtn = new JButton("Save Changes");
        JButton removeRecBtn = new JButton("Remove Recommendation");
        JButton addMilestoneBtn = new JButton("Add Milestone");
        JButton updateMilestoneBtn = new JButton("Update Milestone");
        JButton removeMilestoneBtn = new JButton("Remove Milestone");

        List<Student> processed = masterDataService.getAllProcessedStudents();
        java.util.List<Student> needRecovery = new java.util.ArrayList<>();
        for (Student s : processed) {
            boolean isEligible = (s.getCurrentCGPA() >= 2.0 && s.getFailedCourseCount() <= 3);
            if (!isEligible) needRecovery.add(s);
        }
        needRecovery.sort(java.util.Comparator.comparing(Student::getStudentId));

        studentCombo.removeAllItems();
        for (Student s : needRecovery) studentCombo.addItem(s);

        studentCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                        int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student) setText(((Student) value).getStudentId());
                return this;
            }
        });

        courseCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                        int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) setText(((Course) value).getCourseId());
                return this;
            }
        });

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.add(new JLabel("Student:"));
        row1.add(studentCombo);
        row1.add(new JLabel("Failed Course:"));
        row1.add(courseCombo);
        row1.add(loadBtn);
        row1.add(saveBtn);
        row1.add(removeRecBtn);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2.add(addMilestoneBtn);
        row2.add(updateMilestoneBtn);
        row2.add(removeMilestoneBtn);

        controls.add(row1);
        controls.add(row2);

        northWrapper.add(controls, BorderLayout.SOUTH);
        panel.add(northWrapper, BorderLayout.NORTH);

        planInfoLabel = new JLabel("No plan selected yet");

        milestoneTableModel = new DefaultTableModel(
                new String[]{"Week", "Task", "Status", "Recommendation"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2 || col == 3;
            }
        };

        milestoneTable = new JTable(milestoneTableModel);
        milestoneTable.getTableHeader().setReorderingAllowed(false);

        JComboBox<String> statusEditor = new JComboBox<>(
            new String[]{"Not Started", "In Progress", "Completed", "PASS", "FAIL"}
    );
    
        milestoneTable.getColumnModel().getColumn(2)
                .setCellEditor(new DefaultCellEditor(statusEditor));

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(planInfoLabel, BorderLayout.NORTH);
        center.add(new JScrollPane(milestoneTable), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        studentCombo.addActionListener(e -> reloadCoursesForSelectedStudent());
        loadBtn.addActionListener(e -> loadPlanForSelectedCourse());
        addMilestoneBtn.addActionListener(e -> addMilestoneAction());
        updateMilestoneBtn.addActionListener(e -> updateMilestoneAction());
        removeMilestoneBtn.addActionListener(e -> removeMilestoneAction());

        removeRecBtn.addActionListener(e -> {
            int row = milestoneTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a row first.");
                return;
            }
            milestoneTableModel.setValueAt("", row, 3);
        });

        saveBtn.addActionListener(e -> {
            if (milestoneTable.isEditing()) milestoneTable.getCellEditor().stopCellEditing();
            saveRecoveryEdits();
        });

        if (studentCombo.getItemCount() > 0) {
            studentCombo.setSelectedIndex(0);
            reloadCoursesForSelectedStudent();
        }

        return panel;
    }

    // 3. REPORT PANEL（占位）
    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel("Academic Reports"), BorderLayout.NORTH);
        panel.add(new JLabel("Report Generation UI Placeholder",
                SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    // 4. USER MANAGEMENT PANEL（占位）
    private JPanel buildUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel("User Management"), BorderLayout.NORTH);
        panel.add(new JLabel("Add/Edit System Users UI Placeholder",
                SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private void reloadCoursesForSelectedStudent() {
        if (courseCombo == null) return;

        courseCombo.removeAllItems();
        milestoneTableModel.setRowCount(0);
        planInfoLabel.setText("No plan selected yet");
        currentPlanId = null;

        Student selected = (Student) studentCombo.getSelectedItem();
        if (selected == null) return;

        List<Course> failedCourses =
                courseRecoveryService.getFailedCoursesForStudent(selected.getStudentId());

        for (Course c : failedCourses) {
            courseCombo.addItem(c);
        }
    }

    private void loadPlanForSelectedCourse() {
        Student s = (Student) studentCombo.getSelectedItem();
        Course c = (Course) courseCombo.getSelectedItem();

        if (s == null || c == null) {
            JOptionPane.showMessageDialog(this, "Please select both a student and a course.");
            return;
        }

        CourseRecoveryPlan any =
                courseRecoveryService.getOrCreateRecoveryPlan(s.getStudentId(), c.getCourseId());
        currentPlanId = any.getPlanId();

        planInfoLabel.setText("Plan " + currentPlanId
                + " | Student: " + s.getStudentId()
                + " | Course: " + c.getCourseId());

        List<CourseRecoveryPlan> entries =
                courseRecoveryService.getRecoveryPlanEntries(s.getStudentId(), c.getCourseId());
        java.util.Map<Integer, CourseRecoveryPlan> weekMap = new java.util.HashMap<>();
        for (CourseRecoveryPlan p : entries) {
            weekMap.put(p.getStudyWeek(), p);
        }

        List<Milestone> ms = courseRecoveryService.getMilestonesForPlan(currentPlanId, c.getCourseId());
        
        milestoneTableModel.setRowCount(0);

        for (Milestone m : ms) {
            CourseRecoveryPlan p = weekMap.get(m.getStudyWeek());

            String status = (p == null || p.getStatus() == null || p.getStatus().isBlank())
                    ? "Not Started" : p.getStatus();

            String rec = (p == null || p.getRecommendation() == null)
                    ? "NA" : p.getRecommendation();

            if ("NA".equalsIgnoreCase(rec)) rec = "";

            milestoneTableModel.addRow(new Object[]{
                    m.getStudyWeek(),
                    m.getTask(),
                    status,
                    rec
            });
        }
    }

    private void saveRecoveryEdits() {
        Student s = (Student) studentCombo.getSelectedItem();
        Course c = (Course) courseCombo.getSelectedItem();

        if (s == null || c == null || currentPlanId == null) {
            JOptionPane.showMessageDialog(this, "Please load a plan first.");
            return;
        }

        if (milestoneTable.isEditing()) {
            milestoneTable.getCellEditor().stopCellEditing();
        }

        for (int r = 0; r < milestoneTableModel.getRowCount(); r++) {
            int week = Integer.parseInt(milestoneTableModel.getValueAt(r, 0).toString());

            String task = String.valueOf(milestoneTableModel.getValueAt(r, 1));
            boolean isExamRow = task != null && task.toLowerCase().contains("exam");

            String newStatus = String.valueOf(milestoneTableModel.getValueAt(r, 2));
            String newRec = String.valueOf(milestoneTableModel.getValueAt(r, 3));
            newRec = sanitizeForCsv(newRec);
            if (newRec.isBlank()) newRec = "NA";

            // ✅ 先拿旧的 planWeek（为了比较 oldStatus）
            CourseRecoveryPlan planWeek =
                    courseRecoveryService.getOrCreatePlanWeek(currentPlanId, s.getStudentId(), c.getCourseId(), week);

            String oldStatus = planWeek.getStatus();

            // ✅ 1) 写回 plan（status + recommendation）
            courseRecoveryService.updateMilestoneStatus(currentPlanId, c.getCourseId(), week, newStatus);
            courseRecoveryService.updateRecommendation(currentPlanId, c.getCourseId(), week, newRec);

            // ✅ 2) 只有 Exam 行 + status 有变化 + 且是 Pass/Failed 才更新成绩 & attempt+1
            boolean isPassFail = "PASS".equalsIgnoreCase(newStatus) || "FAIL".equalsIgnoreCase(newStatus);

            if (isExamRow && isPassFail) {
                boolean passed = "Pass".equalsIgnoreCase(newStatus);
                double gradePoint = passed ? 2.0 : 0.0;

                courseRecoveryService.recordRecoveryExamResult(
                        s.getStudentId(),
                        c.getCourseId(),
                        gradePoint
                );
            }
        }

        // ✅ loop 外面才 save（一次就好）
        courseRecoveryService.saveRecoveryPlans();
        JOptionPane.showMessageDialog(this, "Saved!");
    }


    private String sanitizeForCsv(String text) {
        if (text == null) return "";
        return text.replace(",", "，").trim();
    }

    private void addMilestoneAction() {
    if (currentPlanId == null) {
        JOptionPane.showMessageDialog(this, "Please load a plan first.");
        return;
    }
    Course c = (Course) courseCombo.getSelectedItem();
    if (c == null) return;

    String weekStr = JOptionPane.showInputDialog(this, "Enter Week (number):");
    if (weekStr == null) return;

    int week;
    try { week = Integer.parseInt(weekStr.trim()); }
    catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Invalid week number.");
        return;
    }

    String task = JOptionPane.showInputDialog(this, "Enter Milestone Task:");
    if (task == null || task.trim().isEmpty()) return;

    courseRecoveryService.upsertCustomMilestone(currentPlanId, c.getCourseId(), week, task.trim());
    courseRecoveryService.saveCustomMilestones();

    Student s = (Student) studentCombo.getSelectedItem();
    if (s != null) {
        courseRecoveryService.getOrCreatePlanWeek(currentPlanId, s.getStudentId(), c.getCourseId(), week);
        courseRecoveryService.saveRecoveryPlans();
    }

    loadPlanForSelectedCourse(); // reload table

    // TODO: Email notification hook
}

    private void updateMilestoneAction() {
        if (currentPlanId == null) {
            JOptionPane.showMessageDialog(this, "Please load a plan first.");
            return;
        }
        int row = milestoneTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        Course c = (Course) courseCombo.getSelectedItem();
        if (c == null) return;

        int week = Integer.parseInt(milestoneTableModel.getValueAt(row, 0).toString());
        String oldTask = String.valueOf(milestoneTableModel.getValueAt(row, 1));

        String newTask = JOptionPane.showInputDialog(this, "Update Task:", oldTask);
        if (newTask == null || newTask.trim().isEmpty()) return;

        courseRecoveryService.upsertCustomMilestone(currentPlanId, c.getCourseId(), week, newTask.trim());
        courseRecoveryService.saveCustomMilestones();

        loadPlanForSelectedCourse();

        // TODO: Email notification hook
    }

    private void removeMilestoneAction() {
        if (currentPlanId == null) {
            JOptionPane.showMessageDialog(this, "Please load a plan first.");
            return;
        }
        int row = milestoneTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        Course c = (Course) courseCombo.getSelectedItem();
        Student s = (Student) studentCombo.getSelectedItem();
        if (c == null || s == null) return;

        int week = Integer.parseInt(milestoneTableModel.getValueAt(row, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove milestone for Week " + week + "?\n(Will remove custom milestone and plan entry for that week)",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        courseRecoveryService.removeCustomMilestone(currentPlanId, c.getCourseId(), week);
        courseRecoveryService.saveCustomMilestones();

        courseRecoveryService.removePlanWeek(currentPlanId, s.getStudentId(), c.getCourseId(), week);
        courseRecoveryService.saveRecoveryPlans();

        loadPlanForSelectedCourse();

        // TODO: Email notification hook
    }

}
