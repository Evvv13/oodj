package edu.apu.crs.usermanagement;

import edu.apu.crs.courserecovery.CourseRecoveryDashboard;
import edu.apu.crs.usermanagement.Data.systemUser;
import edu.apu.crs.usermanagement.Service.systemUserService;
import javax.swing.*;
import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;

public class LoginPage extends JFrame {

    private systemUserService userService;

    public LoginPage() {
        this.userService = new systemUserService();

        setTitle("Login to Course Recovery System");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //  Use GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        
        // panel.setBackground(new Color(240, 248, 255));
        // set background calor

        panel.setBorder(new EmptyBorder(20, 20, 20, 20)); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make components stretch

        // --- Row 1: Username/UserID ---
        JLabel userLabel = new JLabel("Username/User ID :");
        JTextField userText = new JTextField(15);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(userLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(userText, gbc);

        // --- Row 2: Password ---
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passText = new JPasswordField(15);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; // Reset weight
        panel.add(passLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(passText, gbc);

        // --- Row 3: Buttons
        JButton loginButton = new JButton("Login");
        JButton forgotPassButton = new JButton("Forgot Password");

        // Use a sub-panel for horizontal button alignment
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); 
        buttonPanel.add(loginButton);
        buttonPanel.add(forgotPassButton);
        

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; // Span across 2 columns
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);
        
        add(panel);

        // --- Login Action ---
        loginButton.addActionListener(e -> {
            String input = userText.getText().trim();
            String password = new String(passText.getPassword());

            if (input.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username/UserID and Password cannot be empty.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            systemUser user = userService.login(input, password);

            if (user != null) {
                JOptionPane.showMessageDialog(null, "Login successful!\nWelcome, " + user.getRoleTitle());
                dispose();
                new CourseRecoveryDashboard(user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username/UserID or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Forgot Password Action ---
        forgotPassButton.addActionListener(e -> {
            String email = JOptionPane.showInputDialog(null, "Enter your registered email to reset password:");
            
            
            if (email == null) {
                return;
            }

            // 2. Check if user submitted an empty email (returns empty string)
            if (email.trim().isEmpty()) {
                
                JOptionPane.showMessageDialog(null, "Email field cannot be empty. Please enter your email.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 3. Find user and process
            systemUser user = userService.findUserByEmail(email.trim());
            if (user != null) {

                // NotificationService.sendPasswordResetRequest here (Requirement 5.0)
                JOptionPane.showMessageDialog(null,
                    "Password recovery link sent to: " + user.getEmail(),
                    "Email Sent", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "The entered email was not found in the system.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}