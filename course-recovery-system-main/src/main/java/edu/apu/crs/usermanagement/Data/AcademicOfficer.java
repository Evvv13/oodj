package edu.apu.crs.usermanagement.Data;

public class AcademicOfficer extends systemUser{

    // Constructor chains up to the SystemUser constructor
    public AcademicOfficer(String username, String password, String role, boolean isActive) {

        // Calls SystemUser constructor to initialize fields (username, password, role, isActive)
        super(username, password, role, isActive);
    }

    // Polymorphism: Must implement the abstract method from SystemUser
    @Override
    public String getRoleTitle() {
        return "Academic Officer";
    }


    // can add methods specific to an Academic Officer here (e.g., manageReports())

}
