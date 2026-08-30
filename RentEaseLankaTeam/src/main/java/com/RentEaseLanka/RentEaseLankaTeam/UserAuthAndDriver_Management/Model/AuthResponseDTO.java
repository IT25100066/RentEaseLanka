package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model;

/**
 * Data Transfer Object (DTO) for sent-back authentication responses.
 * When a user logs in successfully, this object wraps the generated
 * JWT access token, token type ("Bearer"), user email, role, and ID to send back to the client.
 */
public class AuthResponseDTO {
    private String token;
    private String type = "Bearer";
    private String email;
    private String role;
    private Long userId;

    public AuthResponseDTO(String token, String email, String role, Long userId) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public String getType() { return type; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Long getUserId() { return userId; }
}