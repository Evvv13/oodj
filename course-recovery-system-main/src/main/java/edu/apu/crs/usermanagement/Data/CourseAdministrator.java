package edu.apu.crs.usermanagement.Data;

public class CourseAdministrator extends systemUser{

    public CourseAdministrator(String username, String password, String role, boolean isActive) {
        super(username, password, role, isActive);
    }

    // Polymorphism: Implements the abstract method
    @Override
    public String getRoleTitle() {
        return "Course Administrator";
    }


    //can add methods specific to a Course Administrator here (e.g., manageRecoveryPlans())
}
