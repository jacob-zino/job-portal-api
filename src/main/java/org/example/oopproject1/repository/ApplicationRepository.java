package org.example.oopproject1.repository;

import org.example.oopproject1.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for accessing Application entities in MongoDB.
 * <p>
 * Extends MongoRepository to provide CRUD operations, as well as custom
 * methods for pagination and filtering by job ID, email, and application status.
 * </p>
 *
 * @since 1.0
 */
@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {

    /**
     * Retrieves a paginated list of all applications.
     *
     * @param pageable pagination and sorting information
     * @return a Page of Application objects
     */
    Page<Application> findAll(Pageable pageable);

    /**
     * Retrieves a paginated list of applications filtered by job ID.
     *
     * @param jobId    the ID of the job to filter applications
     * @param pageable pagination and sorting information
     * @return a Page of Application objects matching the job ID
     */
    Page<Application> findByJobId(String jobId, Pageable pageable);

    /**
     * Retrieves a paginated list of applications filtered by candidate email.
     *
     * @param email    the email address of the candidate
     * @param pageable pagination and sorting information
     * @return a Page of Application objects matching the email
     */
    Page<Application> findByEmail(String email, Pageable pageable);

    /**
     * Retrieves a paginated list of applications filtered by status.
     *
     * @param status   the application status to filter by
     * @param pageable pagination and sorting information
     * @return a Page of Application objects matching the status
     */
    Page<Application> findByStatus(Application.ApplicationStatus status, Pageable pageable);

    Page<Application> findByJobIdIn(List<String> jobIds, Pageable pageable);

    /**
     * Retrieves all applications filtered by job ID (non-paginated).
     *
     * @param jobId the ID of the job to filter applications
     * @return a List of Application objects matching the job ID
     */
    List<Application> findByJobId(String jobId);

    List<Application> findByJobIdIn(List<String> jobIds);

    /**
     * Retrieves all applications filtered by candidate email (non-paginated).
     *
     * @param email the email address of the candidate
     * @return a List of Application objects matching the email
     */
    List<Application> findByEmail(String email);

    /**
     * Retrieves all applications filtered by status (non-paginated).
     *
     * @param status the application status to filter by
     * @return a List of Application objects matching the status
     */
    List<Application> findByStatus(Application.ApplicationStatus status);
}
