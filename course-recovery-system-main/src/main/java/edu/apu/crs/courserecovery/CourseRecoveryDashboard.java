package edu.apu.crs.courserecovery;

import edu.apu.crs.models.Student;
import edu.apu.crs.models.Course;
import edu.apu.crs.models.Milestone;
import edu.apu.crs.models.CourseRecoveryPlan;
import edu.apu.crs.service.MasterDataService;
import edu.apu.crs.service.CourseRecoveryService;
import edu.apu.crs.usermanagement.Data.systemUser;

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

    // 2. RECOVERY PANEL（用你那版 + CourseRecoveryService）
    private JPanel buildRecoveryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel("Course Recovery Management"), BorderLayout.NORTH);

        JPanel top = new JPanel();
        top.add(new JLabel("Student:"));

        studentCombo = new JComboBox<>();
        for (Student s : masterDataService.getStudentsNeedingRecovery()) {
            studentCombo.addItem(s);
        }
        // 显示 StudentID
        studentCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student) {
                    setText(((Student) value).getStudentId());
                }
                return this;
            }
        });
        top.add(studentCombo);

        top.add(new JLabel("Failed Course:"));
        courseCombo = new JComboBox<>();
        courseCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) {
                    setText(((Course) value).getCourseId());
                }
                return this;
            }
        });
        top.add(courseCombo);

        JButton loadBtn = new JButton("Load Plan");
        top.add(loadBtn);

        panel.add(top, BorderLayout.NORTH);

        planInfoLabel = new JLabel("No plan selected yet");
        milestoneTableModel = new DefaultTableModel(
                new String[]{"Week", "Task", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        milestoneTable = new JTable(milestoneTableModel);

        JPanel center = new JPanel(new BorderLayout());
        center.add(planInfoLabel, BorderLayout.NORTH);
        center.add(new JScrollPane(milestoneTable), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        studentCombo.addActionListener(e -> reloadCoursesForSelectedStudent());
        loadBtn.addActionListener(e -> loadPlanForSelectedCourse());

        if (studentCombo.getItemCount() > 0) {
            studentCombo.setSelectedIndex(0);
        }

        return panel;
    }

    private void reloadCoursesForSelectedStudent() {
        if (courseCombo == null) return;

        courseCombo.removeAllItems();
        milestoneTableModel.setRowCount(0);
        planInfoLabel.setText("No plan selected yet");

        Student selected = (Student) studentCombo.getSelectedItem();
        if (selected == null) return;

        // 用 CourseRecoveryService 取得 fail 的 course
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
            JOptionPane.showMessageDialog(this,
                    "Please select both a student and a course.");
            return;
        }

        CourseRecoveryPlan plan =
                courseRecoveryService.getOrCreateRecoveryPlan(
                        s.getStudentId(), c.getCourseId());

        planInfoLabel.setText("Plan " + plan.getPlanId()
                + " | Status: " + plan.getStatus());

        List<Milestone> ms =
                courseRecoveryService.getMilestonesForCourse(c.getCourseId());
        milestoneTableModel.setRowCount(0);
        for (Milestone m : ms) {
            milestoneTableModel.addRow(new Object[]{
                    m.getStudyWeek(),
                    m.getTask(),
                    plan.getStatus()
            });
        }
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
}
