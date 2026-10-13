package org.example.oopproject1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a recruiter profile in the Job Portal system.
 * <p>
 * Stores recruiter details such as name, email, company, position, and contact phone.
 * </p>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recruiters")
public class Recruiter {

    /**
     * Unique identifier for the recruiter.
     */
    @Id
    private String id;

    /**
     * Full name of the recruiter.
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * Email address of the recruiter.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Company name where the recruiter works.
     */
    @NotBlank(message = "Company is required")
    private String company;

    /**
     * Position or job title of the recruiter.
     */
    private String position;

    /**
     * Contact phone number of the recruiter.
     */
    private String phone;
}