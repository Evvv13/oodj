package edu.apu.crs;
import javax.swing.SwingUtilities;
import edu.apu.crs.usermanagement.LoginPage;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage login = new LoginPage();
            login.setVisible(true);
        });
    }
}