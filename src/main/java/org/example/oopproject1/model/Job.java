package org.example.oopproject1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents a job posting within the Job Portal system.
 * <p>
 * Contains details such as title, company, description, skill requirements,
 * salary range, location, employment type, posting and deadline dates,
 * active status, and the recruiter who posted it.
 * </p>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jobs")
public class Job {

    /**
     * Unique identifier for the job posting.
     */
    @Id
    private String id;

    /**
     * Title of the job position.
     */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * Name of the company offering the job.
     */
    @NotBlank(message = "Company is required")
    private String company;

    /**
     * Detailed description of the job responsibilities and qualifications.
     */
    @NotBlank(message = "Description is required")
    private String description;

    /**
     * List of skills required for the job (e.g., Java, Spring Boot).
     */
    private List<String> requiredSkills;

    /**
     * Minimum salary offered for this position.
     */
    @NotNull(message = "Salary range is required")
    private Double minSalary;

    /**
     * Maximum salary offered for this position.
     */
    private Double maxSalary;

    /**
     * Location where the job is based.
     */
    @NotBlank(message = "Location is required")
    private String location;

    /**
     * Employment type (e.g., Full-time, Part-time, Contract).
     */
    @NotBlank(message = "Employment type is required")
    private String employmentType;

    /**
     * Date when the job was posted.
     */
    private LocalDate postDate;

    /**
     * Application deadline date for the job.
     */
    private LocalDate deadlineDate;

    /**
     * Indicates whether the job posting is currently active.
     */
    private boolean isActive;

    /**
     * Identifier of the recruiter who created this job posting.
     */
    private String recruiterId;
}