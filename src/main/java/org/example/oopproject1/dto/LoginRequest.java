package org.example.oopproject1.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for user login credentials.
 * <p>
 * Contains the username and password required for authentication.
 * </p>
 *
 * @since 1.0
 */
@Data
public class LoginRequest {

    /**
     * The username of the user attempting to log in.
     */
    @NotBlank
    private String username;

    /**
     * The user's password for authentication.
     */
    @NotBlank
    private String password;

}
