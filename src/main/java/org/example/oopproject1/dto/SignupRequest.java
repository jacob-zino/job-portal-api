package org.example.oopproject1.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user signup requests.
 * <p>
 * Encapsulates user registration details including optional recruiter fields.
 * </p>
 *
 * @since 1.0
 */
@Data
public class SignupRequest {

    /**
     * Desired username for the new account.
     * <p>
     * Must be between 3 and 20 characters, and not blank.
     * </p>
     */
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    /**
     * Email address for the new account.
     * <p>
     * Must be a valid email format and not exceed 50 characters.
     * </p>
     */
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    /**
     * Password for the new account.
     * <p>
     * Must be between 6 and 40 characters.
     * </p>
     */
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    /**
     * Role for the new account (e.g., "USER" or "RECRUITER").
     */
    private String role;

    /**
     * The company name for recruiter registration (optional).
     */
    private String company;

    /**
     * Phone number for recruiter registration (optional).
     */
    private String phone;

    /**
     * Position/title for recruiter registration (optional).
     */
    private String position;
}
