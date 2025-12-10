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
    
    // Layout Manager to switch screens
    private CardLayout cardLayout;
    private JPanel mainContainer;

    public CourseRecoveryDashboard(systemUser user) {
        this.currentUser = user;
        this.masterDataService = new MasterDataService();

        setTitle("CRS Dashboard - " + user.getRoleTitle());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Setup CardLayout (Holds different screens like a stack of cards)
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // 2. Add Screens to the Container
        mainContainer.add(createMenuPanel(), "MENU");       // The Main Menu
        mainContainer.add(buildEligibilityPanel(), "ELIGIBILITY");
        mainContainer.add(buildRecoveryPanel(), "RECOVERY");
        mainContainer.add(buildReportPanel(), "REPORT");
        mainContainer.add(buildUserManagementPanel(), "USER_MANAGE");

        add(mainContainer);
        
        // Show Menu first
        cardLayout.show(mainContainer, "MENU");
    }

    // =================================================================
    // 🔑 ROLE-BASED MENU LOGIC
    // =================================================================
    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 10, 10)); // Simple Grid Layout
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100)); // Padding

        // Welcome Message
        JLabel welcome = new JLabel("Welcome, " + currentUser.getUsername() + " (" + currentUser.getRoleTitle() + ")", SwingConstants.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 18));
        menuPanel.add(welcome);

        String role = currentUser.getRoleTitle();

        // --- LOGIC: Only add buttons allowed for the specific role ---

        // 1. ACADEMIC OFFICER Features
        if (role.equalsIgnoreCase("Academic Officer")) {
            addButton(menuPanel, "Check Eligibility", "ELIGIBILITY");
            addButton(menuPanel, "Manage Recovery Plans", "RECOVERY");
            addButton(menuPanel, "Academic Reports", "REPORT");
        }

        // 2. COURSE ADMIN Features
        if (role.equalsIgnoreCase("Course Administrator") || role.equalsIgnoreCase("Course Admin")) {
            addButton(menuPanel, "User Management", "USER_MANAGE");
        }

        // 3. COMMON Features (Logout)
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new edu.apu.crs.usermanagement.LoginPage().setVisible(true);
        });
        menuPanel.add(logoutBtn);

        return menuPanel;
    }

    // Helper to add standard navigation buttons
    private void addButton(JPanel panel, String label, String cardName) {
        JButton btn = new JButton(label);
        btn.addActionListener(e -> cardLayout.show(mainContainer, cardName));
        panel.add(btn);
    }

    // =================================================================
    // 🧩 FEATURE PANELS (Simple Versions)
    // =================================================================

    // 1. ELIGIBILITY PANEL
    private JPanel buildEligibilityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Header with Back Button
        panel.add(createHeaderPanel("Student Eligibility Check"), BorderLayout.NORTH);

        // Table
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "CGPA", "Failed", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; } // Read-only
        };
        
        // Load Data
        List<Student> students = masterDataService.getAllProcessedStudents();
        for (Student s : students) {
            boolean eligible = (s.getCurrentCGPA() >= 2.0 && s.getFailedCourseCount() <= 3);
            model.addRow(new Object[]{
                s.getStudentId(), s.getStudentName(), String.format("%.2f", s.getCurrentCGPA()),
                s.getFailedCourseCount(), eligible ? "Eligible" : "Needs Recovery"
            });
        }

        panel.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        return panel;
    }

    // 2. RECOVERY PANEL
    private JPanel buildRecoveryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel("Course Recovery Management"), BorderLayout.NORTH);
        
        // Placeholder Content
        JComboBox<String> combo = new JComboBox<>();
        List<Student> list = masterDataService.getStudentsNeedingRecovery();
        for(Student s : list) combo.addItem(s.getStudentId());
        
        JPanel content = new JPanel();
        content.add(new JLabel("Select Student:"));
        content.add(combo);
        panel.add(content, BorderLayout.CENTER);
        
        return panel;
    }

    // 3. REPORT PANEL
    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel("Academic Reports"), BorderLayout.NORTH);
        panel.add(new JLabel("Report Generation UI Placeholder", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    // 4. USER MANAGEMENT PANEL
    private JPanel buildUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel("User Management"), BorderLayout.NORTH);
        panel.add(new JLabel("Add/Edit System Users UI Placeholder", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    // --- Helper for consistent headers ---
    private JPanel createHeaderPanel(String title) {
        JPanel header = new JPanel(new BorderLayout());
        JButton backBtn = new JButton("<< Back");
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));
        
        header.add(backBtn, BorderLayout.WEST);
        header.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.CENTER);
        return header;
    }
}