package edu.apu.crs.usermanagement;

import edu.apu.crs.courserecovery.CourseRecoveryDashboard;
import edu.apu.crs.usermanagement.Data.systemUser; // Use your lowercase naming
import edu.apu.crs.usermanagement.Service.systemUserService; // Use your lowercase naming

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPage extends JFrame {
    
    // Create an instance of the service to handle login logic
    private systemUserService userService;

    public LoginPage() {
        // Initialize the user service (loads users from file)
        this.userService = new systemUserService();

        setTitle("Login Page");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel userLabel = new JLabel("Username:");
        JTextField userText = new JTextField(15);

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passText = new JPasswordField(15);

        JButton loginButton = new JButton("Login");

        JPanel panel = new JPanel();
        panel.add(userLabel);
        panel.add(userText);
        panel.add(passLabel);
        panel.add(passText);
        panel.add(loginButton);

        add(panel);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passText.getPassword());

                // REPLACED HARDCODED CHECK WITH SERVICE CALL
                systemUser user = userService.login(username, password);

                if (user != null) {
                    JOptionPane.showMessageDialog(null, "Login successful!\nWelcome, " + user.getRoleTitle());
                    
                    // Log the logout event before closing? 
                    // Usually logout is logged when they click 'Logout', but login is logged inside service.login()
                    
                    dispose(); // Close login window
                    
                    // Pass the authenticated user object to the dashboard
                    // (We will update Dashboard next to accept this)
                    new CourseRecoveryDashboard(user).setVisible(true);
                    
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}