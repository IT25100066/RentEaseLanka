package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model;

public class UserRegisterDTO {
    private String fullName;
    private String email;
    private String password;
    private Role role;

    public UserRegisterDTO() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}