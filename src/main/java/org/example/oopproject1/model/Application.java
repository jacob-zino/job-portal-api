package org.example.oopproject1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Represents a job application submitted by a candidate.
 * <p>
 * Stores details such as the associated job ID, candidate information,
 * resume URL, cover letter text, application date, and current status.
 * </p>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "applications")
public class Application {

    /**
     * Unique identifier for this application.
     */
    @Id
    private String id;

    /**
     * Identifier of the job to which the application was submitted.
     */
    @NotBlank(message = "Job ID is required")
    private String jobId;

    /**
     * Full name of the candidate submitting the application.
     */
    @NotBlank(message = "Candidate name is required")
    private String candidateName;

    /**
     * Email address of the candidate.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Phone number of the candidate.
     */
    @NotBlank(message = "Phone number is required")
    private String phone;

    /**
     * URL pointing to the candidate's uploaded resume document.
     */
    private String resumeUrl;

    /**
     * Cover letter text provided by the candidate.
     */
    private String coverLetterText;

    /**
     * Date when the application was submitted.
     */
    private LocalDate applicationDate;

    /**
     * Current status of the application in the review process.
     * @see ApplicationStatus
     */
    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    /**
     * Enumeration of possible application statuses.
     */
    public enum ApplicationStatus {
        /** Application has been submitted and is awaiting review. */
        SUBMITTED,
        /** Application is currently under review. */
        REVIEWING,
        /** Candidate has been interviewed. */
        INTERVIEWED,
        /** Application was rejected. */
        REJECTED,
        /** Application was accepted. */
        ACCEPTED
    }
}
