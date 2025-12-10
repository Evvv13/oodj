package edu.apu.crs.notification;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import io.github.cdimascio.dotenv.Dotenv;

public class NotificationService {

    private final String fromEmail;
    private final String password;
    private final String host;
    private final String port;

    public NotificationService() {
        Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load(); // loads .env from project root, but won't fail if missing
        this.fromEmail = dotenv.get("EMAIL_USERNAME", "your-email@gmail.com");
        this.password = dotenv.get("EMAIL_PASSWORD", "your-app-password");
        this.host = dotenv.get("EMAIL_HOST", "smtp.gmail.com");
        this.port = dotenv.get("EMAIL_PORT", "587");
    }

    // Core function: send an email
    private void sendEmail(String toEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("✅ Email sent to: " + toEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to send email to: " + toEmail);
        }
    }

    // ===============================
    // 🔹 User Account Management
    // ===============================
    public void sendNewUserCreated(String email, String username) {
        sendEmail(email, "Welcome to CRS",
            "Hi " + username + ",\n\nYour CRS account has been created successfully.\nWelcome aboard!");
    }

    public void sendProfileUpdated(String email) {
        sendEmail(email, "Profile Updated",
            "Your CRS profile has been updated successfully.");
    }

    public void sendUserDeactivated(String email) {
        sendEmail(email, "Account Deactivated",
            "Your CRS account has been deactivated.");
    }

    public void sendLoginNotification(String email) {
        sendEmail(email, "Login Alert",
            "You have logged in to CRS.");
    }

    public void sendLogoutNotification(String email) {
        sendEmail(email, "Logout Alert",
            "You have logged out of CRS.");
    }

    // ===============================
    // 🔹 Password & Recovery
    // ===============================
    public void sendPasswordResetRequest(String email, String resetLink) {
        sendEmail(email, "Password Reset Request",
            "Click the following link to reset your password:\n" + resetLink + "\n\nIf you didn’t request this, please ignore.");
    }

    public void sendPasswordChanged(String email) {
        sendEmail(email, "Password Changed",
            "Your CRS password has been successfully changed.");
    }

    // ===============================
    // 🔹 Course Recovery (Action Plan)
    // ===============================
    public void sendFailedStudentEnrolled(String email, String courseName) {
        sendEmail(email, "Enrolled in Recovery Course",
            "You have been enrolled in the recovery course: " + courseName + 
            ".\nPlease check your dashboard for further details.");
    }

    public void sendRecoveryReminder(String email, String courseName, String dueDate) {
        sendEmail(email, "Recovery Course Reminder",
            "Reminder: Your recovery course (" + courseName + ") is due on " + dueDate + ".\nStay on track!");
    }

    public void sendRecoveryProgressUpdate(String email, String courseName, String milestone, String progressDetails) {
        sendEmail(email, "Recovery Progress Update",
            "Course: " + courseName + 
            "\nMilestone: " + milestone +
            "\nProgress: " + progressDetails);
    }

    // ===============================
    // 🔹 Academic Performance Report
    // ===============================
    public void sendSemesterReport(String email, String studentName, String reportSummary) {
        sendEmail(email, "Semester Report for " + studentName,
            "Hello " + studentName + ",\n\nHere is your semester performance summary:\n" 
            + reportSummary + 
            "\n\nKeep up the good work!");
    }
}
