package edu.apu.crs.usermanagement.Data;

public abstract class systemUser {

    private String userID ;
    private String username;
    private String email;
    private String password;
    private String role;
    private boolean isActive;

    // Polymorphic contract that concrete subclasses must implement.
    public abstract String getRoleTitle();

    public systemUser(String userID, String username, String email, String password, String role, boolean isActive) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username must not be null or empty");
        }
        if (password == null) {
            throw new IllegalArgumentException("password must not be null");
        }

        this.userID = userID;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
    }

    // Getters
    public String getUserID (){
        return this.userID;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public String getRole() {
        return this.role;
    }

    public boolean isActive() {
        return this.isActive;
    }

    // Setters

    public void setUserID (String userID){

        this.userID =userID;
    }

    public void setUsername (String username){

        this.username =username;
    }

    public void setEmail (String email){

        this.email =email;
    }


    public void setPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("password must not be null");
        }
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", active=" + isActive +
                '}';
    }
}
