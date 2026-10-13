package org.example.oopproject1.dto;

import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object representing a JWT authentication response.
 * <p>
 * Contains the token, token type, user details, and granted roles.
 * </p>
 *
 * @since 1.0
 */
@Data
public class JwtResponse {

    /**
     * The generated JWT token string.
     */
    private String token;

    /**
     * The token type, defaults to "Bearer".
     */
    private String type = "Bearer";

    /**
     * The unique identifier of the authenticated user.
     */
    private String id;

    /**
     * The username of the authenticated user.
     */
    private String username;

    /**
     * The email address of the authenticated user.
     */
    private String email;

    /**
     * The list of roles granted to the authenticated user.
     */
    private List<String> roles;

    /**
     * Constructs a JwtResponse with all required fields.
     *
     * @param accessToken the JWT token string
     * @param id          the ID of the authenticated user
     * @param username    the username of the authenticated user
     * @param email       the email of the authenticated user
     * @param roles       the list of granted roles
     */
    public JwtResponse(String accessToken, String id, String username, String email, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}