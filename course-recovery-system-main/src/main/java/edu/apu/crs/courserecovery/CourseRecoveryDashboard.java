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
    
    // Eligibility Panel Components
    private DefaultTableModel eligibilityModel;
    private JComboBox<String> eligibilityFilterCombo;
    private JTextField searchField;

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

        // show button base on role

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
    // 🧩 FEATURE PANELS
    // =================================================================

    // 1. ELIGIBILITY PANEL
    private JPanel buildEligibilityPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        
        // 1. Control Panel (NORTH)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        
        // Filter Components (Existing)
        JLabel filterLabel = new JLabel("Filter by Status:");
        eligibilityFilterCombo = new JComboBox<>(new String[]{"All Students", "Eligible Only", "Needs Recovery Only"});
        
        // Search Components (NEW)
        JLabel searchLabel = new JLabel("Search by Student ID:");
        searchField = new JTextField(10); // 10 columns wide
        JButton searchBtn = new JButton("Search");

        // Add the control components
        controlPanel.add(filterLabel);
        controlPanel.add(eligibilityFilterCombo);
        controlPanel.add(searchLabel);
        controlPanel.add(searchField);
        controlPanel.add(searchBtn);

        // Refresh Button
        JButton refreshBtn = new JButton("Refresh Data");
        controlPanel.add(refreshBtn);

        panel.add(controlPanel, BorderLayout.NORTH);

        // 2. Table Setup (CENTER - Unchanged)
        eligibilityModel = new DefaultTableModel(
                new String[]{"Student ID", "Name", "CGPA", "Failed Courses", "Status"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        JTable table = new JTable(eligibilityModel);
        table.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. Action Listeners
        // --- Initial Load ---
        filterAndLoadData();

        // --- Search Action ---
        searchBtn.addActionListener(e -> searchStudentData(searchField.getText().trim()));
        
        // --- Filter/Refresh Actions ---
        eligibilityFilterCombo.addActionListener(e -> filterAndLoadData());
        refreshBtn.addActionListener(e -> {
            searchField.setText(""); // Clear search bar on refresh
            filterAndLoadData();
        });

        return panel;
    }
    
    private void filterAndLoadData() {
        eligibilityModel.setRowCount(0);
        List<Student> allStudents = masterDataService.getAllProcessedStudents();
        String selectedFilter = (String) eligibilityFilterCombo.getSelectedItem();
        
        // 1. Iterate and filter the master list
        for (Student s : allStudents) {
            // Calculate status string
            boolean isEligible = (s.getCurrentCGPA() >= 2.0 && s.getFailedCourseCount() <= 3);
            String statusText = isEligible ? "Eligible" : "Needs Recovery";
            
            boolean showStudent = false;
    
            // 2. Apply Filtering Logic
            switch (selectedFilter) {
                case "All Students":
                    showStudent = true;
                    break;
                case "Eligible Only":
                    if (isEligible) {
                        showStudent = true;
                    }
                    break;
                case "Needs Recovery Only":
                    if (!isEligible) {
                        showStudent = true;
                    }
                    break;
            }
    
            // 3. Add to table if the filter condition is met
            if (showStudent) {
                eligibilityModel.addRow(new Object[]{
                    s.getStudentId(),
                    s.getStudentName(),
                    String.format("%.2f", s.getCurrentCGPA()), // Format CGPA to 2 decimals
                    s.getFailedCourseCount(),
                    statusText
                });
            }
        }
    }
    
    // =====================================================================
    // 🔧 NEW SEARCH METHOD
    // =====================================================================
    private void searchStudentData(String studentId) {
        // If the search bar is empty, revert to the standard filter view
        if (studentId.isEmpty()) {
            filterAndLoadData();
            return;
        }

        // Clear the table before displaying search results
        eligibilityModel.setRowCount(0);

        // Find the student using the MasterService bridge
        Student s = masterDataService.findStudentById(studentId);

        if (s != null) {
            // Student found: display only this student's data
            boolean isEligible = (s.getCurrentCGPA() >= 2.0 && s.getFailedCourseCount() <= 3);
            String statusText = isEligible ? "Eligible" : "Needs Recovery";
            
            eligibilityModel.addRow(new Object[]{
                s.getStudentId(),
                s.getStudentName(),
                String.format("%.2f", s.getCurrentCGPA()),
                s.getFailedCourseCount(),
                statusText
            });
            // Reset the filter combo, as only the search result is shown
            eligibilityFilterCombo.setSelectedItem("All Students"); 
        } else {
            // Student not found: display an error row
            eligibilityModel.addRow(new Object[]{
                studentId,
                "Student Not Found",
                "N/A",
                "N/A",
                "N/A"
            });
            JOptionPane.showMessageDialog(this, "Student ID '" + studentId + "' was not found.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
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