package org.example.oopproject1.service;

import org.example.oopproject1.exception.ResourceNotFoundException;
import org.example.oopproject1.model.Application;
import org.example.oopproject1.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing job applications.
 * <p>
 * Provides methods to create, retrieve, update, and delete applications,
 * with support for pagination, default value initialization,
 * and multi‐job ID lookups.
 * </p>
 *
 * @since 1.0
 */
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    /**
     * Constructor injection of the repository.
     *
     * @param applicationRepository repository for Application entities
     */
    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * Retrieves all applications (non-paginated).
     *
     * @return list of all Application objects
     */
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    /**
     * Retrieves a paginated list of applications.
     *
     * @param pageable pagination information
     * @return page of Application objects
     */
    public Page<Application> getAllApplications(Pageable pageable) {
        return applicationRepository.findAll(pageable);
    }

    /**
     * Retrieves an application by its unique identifier.
     *
     * @param id the ID of the application to retrieve
     * @return the Application object
     * @throws ResourceNotFoundException if no application is found with the given ID
     */
    public Application getApplicationById(String id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    /**
     * Creates a new application, initializing default values if needed, and saves it.
     *
     * @param application the Application object to create
     * @return the saved Application object
     */
    public Application createApplication(Application application) {
        application.setId(null);
        if (application.getApplicationDate() == null) {
            application.setApplicationDate(LocalDate.now());
        }
        if (application.getStatus() == null) {
            application.setStatus(Application.ApplicationStatus.SUBMITTED);
        }
        return applicationRepository.save(application);
    }

    /**
     * Updates an existing application identified by its ID.
     *
     * @param id                 the ID of the application to update
     * @param applicationDetails the Application object containing updated details
     * @return the updated Application object
     */
    public Application updateApplication(String id, Application applicationDetails) {
        Application application = getApplicationById(id);
        application.setCandidateName(applicationDetails.getCandidateName());
        application.setEmail(applicationDetails.getEmail());
        application.setPhone(applicationDetails.getPhone());
        application.setResumeUrl(applicationDetails.getResumeUrl());
        application.setCoverLetterText(applicationDetails.getCoverLetterText());
        application.setStatus(applicationDetails.getStatus());
        return applicationRepository.save(application);
    }

    /**
     * Deletes the application with the specified ID.
     *
     * @param id the ID of the application to delete
     */
    public void deleteApplication(String id) {
        Application application = getApplicationById(id);
        applicationRepository.delete(application);
    }

    /**
     * Retrieves all applications for a specific job (non-paginated).
     *
     * @param jobId the job ID to filter applications by
     * @return list of Application objects for the given job ID
     */
    public List<Application> getApplicationsByJobId(String jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    /**
     * Retrieves a paginated list of applications for a specific job.
     *
     * @param jobId    the job ID to filter applications by
     * @param pageable pagination information
     * @return page of Application objects for the given job ID
     */
    public Page<Application> getApplicationsByJobId(String jobId, Pageable pageable) {
        return applicationRepository.findByJobId(jobId, pageable);
    }

    /**
     * Retrieves all applications submitted by a specific email (non-paginated).
     *
     * @param email the email address of the candidate
     * @return list of Application objects for the given email
     */
    public List<Application> getApplicationsByEmail(String email) {
        return applicationRepository.findByEmail(email);
    }

    /**
     * Retrieves a paginated list of applications submitted by a specific email.
     *
     * @param email    the email address of the candidate
     * @param pageable pagination information
     * @return page of Application objects for the given email
     */
    public Page<Application> getApplicationsByEmail(String email, Pageable pageable) {
        return applicationRepository.findByEmail(email, pageable);
    }

    /**
     * Retrieves all applications with a specific status (non-paginated).
     *
     * @param status the ApplicationStatus to filter by
     * @return list of Application objects with the given status
     */
    public List<Application> getApplicationsByStatus(Application.ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    /**
     * Retrieves a paginated list of applications with a specific status.
     *
     * @param status   the ApplicationStatus to filter by
     * @param pageable pagination information
     * @return page of Application objects with the given status
     */
    public Page<Application> getApplicationsByStatus(Application.ApplicationStatus status, Pageable pageable) {
        return applicationRepository.findByStatus(status, pageable);
    }

    /**
     * Fetches applications across multiple job IDs (paginated).
     *
     * @param jobIds   list of job IDs
     * @param pageable pagination information
     * @return page of Application objects matching any of the job IDs
     */
    public Page<Application> findByJobIds(List<String> jobIds, Pageable pageable) {
        return applicationRepository.findByJobIdIn(jobIds, pageable);
    }

    /**
     * Fetches applications across multiple job IDs (non-paginated).
     *
     * @param jobIds list of job IDs
     * @return list of Application objects matching any of the job IDs
     */
    public List<Application> findByJobIds(List<String> jobIds) {
        return applicationRepository.findByJobIdIn(jobIds);
    }
}
