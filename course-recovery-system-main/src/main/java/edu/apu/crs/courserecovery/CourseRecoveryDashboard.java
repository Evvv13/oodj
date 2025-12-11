package edu.apu.crs.courserecovery;

import edu.apu.crs.models.Student;
import edu.apu.crs.service.MasterDataService;
import edu.apu.crs.usermanagement.Data.systemUser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseRecoveryDashboard extends JFrame {

    private final systemUser currentUser;
    private final MasterDataService masterDataService;
    
    // Layout Manager
    private CardLayout cardLayout;
    private JPanel mainContainer;

    // Eligibility Components
    private DefaultTableModel eligibilityModel;
    private JComboBox<String> eligibilityFilterCombo;
    private JTextField searchField;

    public CourseRecoveryDashboard(systemUser user) {
        this.currentUser = user;
        this.masterDataService = new MasterDataService();

        setTitle("CRS Dashboard - " + user.getRoleTitle());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Setup CardLayout
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // 2. Add Screens
        mainContainer.add(createMenuPanel(), "MENU");
        mainContainer.add(buildEligibilityPanel(), "ELIGIBILITY");
        mainContainer.add(buildRecoveryPanel(), "RECOVERY");
        mainContainer.add(buildReportPanel(), "REPORT");
        mainContainer.add(buildUserManagementPanel(), "USER_MANAGE");

        add(mainContainer);
        
        // Show Menu first
        cardLayout.show(mainContainer, "MENU");
    }

    // =================================================================
    // 🏠 MENU PANEL (Landing Page)
    // =================================================================
    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        JLabel welcome = new JLabel("Welcome, " + currentUser.getUsername(), SwingConstants.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 22));
        menuPanel.add(welcome);
        
        JLabel roleLabel = new JLabel("Role: " + currentUser.getRoleTitle(), SwingConstants.CENTER);
        roleLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        menuPanel.add(roleLabel);

        String role = currentUser.getRoleTitle();

        // ACADEMIC OFFICER Features
        if (role.equalsIgnoreCase("Academic Officer")) {
            addButton(menuPanel, "Check Student Eligibility", "ELIGIBILITY");
            addButton(menuPanel, "Manage Recovery Plans", "RECOVERY");
            addButton(menuPanel, "Generate Academic Reports", "REPORT");
        }

        // COURSE ADMIN Features
        if (role.equalsIgnoreCase("Course Administrator") || role.equalsIgnoreCase("Course Admin")) {
            addButton(menuPanel, "User Management", "USER_MANAGE");
        }

        // Logout
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(255, 200, 200));
        logoutBtn.addActionListener(e -> {
            dispose();
            new edu.apu.crs.usermanagement.LoginPage().setVisible(true);
        });
        menuPanel.add(logoutBtn);

        return menuPanel;
    }

    private void addButton(JPanel panel, String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        btn.addActionListener(e -> cardLayout.show(mainContainer, cardName));
        panel.add(btn);
    }

    // =================================================================
    // 1️⃣ ELIGIBILITY PANEL (Fixed Layout)
    // =================================================================
    private JPanel buildEligibilityPanel() {
        // Main Panel for this tab
        JPanel mainPanel = new JPanel(new BorderLayout());

        // A. Header (TOP) - Contains "Back" button and Title
        mainPanel.add(createHeaderPanel("Student Eligibility Check"), BorderLayout.NORTH);

        // B. Content Panel (CENTER) - Contains Toolbar and Table
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Toolbar (Filter & Search) ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        // Filter
        toolbar.add(new JLabel("Filter:"));
        eligibilityFilterCombo = new JComboBox<>(new String[]{"All Students", "Eligible Only", "Needs Recovery Only"});
        toolbar.add(eligibilityFilterCombo);

        // Search
        toolbar.add(new JSeparator(SwingConstants.VERTICAL)); // Visual separator
        toolbar.add(new JLabel("Search ID:"));
        searchField = new JTextField(10);
        toolbar.add(searchField);
        
        JButton searchBtn = new JButton("Search");
        JButton resetBtn = new JButton("Reset");
        toolbar.add(searchBtn);
        toolbar.add(resetBtn);

        contentPanel.add(toolbar, BorderLayout.NORTH);

        // --- Table ---
        eligibilityModel = new DefaultTableModel(
            new String[]{"Student ID", "Name", "CGPA", "Failed Courses", "Status"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        JTable table = new JTable(eligibilityModel);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Add Content to Main
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // --- Logic & Listeners ---
        filterAndLoadData(); // Initial load

        // Filter Action
        eligibilityFilterCombo.addActionListener(e -> filterAndLoadData());

        // Search Action
        searchBtn.addActionListener(e -> {
            String term = searchField.getText().trim();
            if (!term.isEmpty()) {
                searchStudentData(term);
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a Student ID to search.");
            }
        });

        // Reset Action
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            eligibilityFilterCombo.setSelectedIndex(0); // Reset filter to All
            filterAndLoadData(); // Reload full list
        });

        return mainPanel;
    }

    // --- Data Logic: Filter ---
    private void filterAndLoadData() {
        eligibilityModel.setRowCount(0);
        List<Student> allStudents = masterDataService.getAllProcessedStudents();
        String selectedFilter = (String) eligibilityFilterCombo.getSelectedItem();
        
        for (Student s : allStudents) {
            boolean isEligible = (s.getCurrentCGPA() >= 2.0 && s.getFailedCourseCount() <= 3);
            String statusText = isEligible ? "Eligible" : "Needs Recovery";
            boolean show = false;

            if (selectedFilter.equals("All Students")) show = true;
            else if (selectedFilter.equals("Eligible Only") && isEligible) show = true;
            else if (selectedFilter.equals("Needs Recovery Only") && !isEligible) show = true;

            if (show) {
                eligibilityModel.addRow(new Object[]{
                    s.getStudentId(), s.getStudentName(), String.format("%.2f", s.getCurrentCGPA()),
                    s.getFailedCourseCount(), statusText
                });
            }
        }
    }

    // --- Data Logic: Search ---
    private void searchStudentData(String studentId) {
        // 1. Clear Table
        eligibilityModel.setRowCount(0);

        // 2. Find Student using MasterService
        Student s = masterDataService.findStudentById(studentId);

        if (s != null) {
            boolean isEligible = (s.getCurrentCGPA() >= 2.0 && s.getFailedCourseCount() <= 3);
            String statusText = isEligible ? "Eligible" : "Needs Recovery";
            
            eligibilityModel.addRow(new Object[]{
                s.getStudentId(), s.getStudentName(), String.format("%.2f", s.getCurrentCGPA()),
                s.getFailedCourseCount(), statusText
            });
        } else {
            JOptionPane.showMessageDialog(this, "Student ID '" + studentId + "' not found.", "Search Result", JOptionPane.WARNING_MESSAGE);
            // Optionally reload data so table isn't empty
            filterAndLoadData();
        }
    }

    // =================================================================
    // 2️⃣ RECOVERY PANEL (Placeholder)
    // =================================================================
    private JPanel buildRecoveryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel("Course Recovery Management"), BorderLayout.NORTH);
        
        JPanel content = new JPanel();
        content.add(new JLabel("Select Student for Recovery: "));
        JComboBox<String> combo = new JComboBox<>();
        
        // Load only failing students
        List<Student> list = masterDataService.getStudentsNeedingRecovery();
        for(Student s : list) combo.addItem(s.getStudentId());
        
        content.add(combo);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // =================================================================
    // 3️⃣ REPORT PANEL (Placeholder)
    // =================================================================
    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel("Academic Performance Reports"), BorderLayout.NORTH);
        panel.add(new JLabel("Report UI goes here", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    // =================================================================
    // 4️⃣ USER MANAGEMENT (Placeholder)
    // =================================================================
    private JPanel buildUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel("User Management"), BorderLayout.NORTH);
        panel.add(new JLabel("User Management UI goes here", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    // --- Helper: Consistent Header with Back Button ---
    private JPanel createHeaderPanel(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.setBackground(new Color(240, 240, 240));

        JButton backBtn = new JButton("<< Back to Menu");
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        header.add(backBtn, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);
        
        // Add dummy panel to EAST to balance the center title
        JPanel dummy = new JPanel();
        dummy.setPreferredSize(backBtn.getPreferredSize());
        dummy.setOpaque(false);
        header.add(dummy, BorderLayout.EAST);

        return header;
    }
}
