package org.example.oopproject1.controller;

import org.example.oopproject1.model.Application;
import org.example.oopproject1.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing job applications.
 * <p>
 * Provides endpoints to create, retrieve, update, delete,
 * and search for applications, with optional pagination and sorting.
 * </p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    /**
     * Retrieves all applications.
     *
     * @return ResponseEntity with list of all Application objects and HTTP 200
     */
    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    /**
     * Retrieves a paginated and sorted list of applications.
     *
     * @param page      zero-based page index (default 0)
     * @param size      page size (default 10)
     * @param sortBy    property name to sort by (default "applicationDate")
     * @param direction sort direction, either "asc" or "desc" (default "desc")
     * @return ResponseEntity with a Page of Application objects and HTTP 200
     */
    @GetMapping("/paged")
    public ResponseEntity<Page<Application>> getPagedApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(applicationService.getAllApplications(pageable));
    }

    /**
     * Retrieves a single application by its ID.
     *
     * @param id application ID
     * @return ResponseEntity with the Application and HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable String id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    /**
     * Creates a new job application.
     *
     * @param application the Application object to create
     * @return ResponseEntity with the created Application and HTTP 201
     */
    @PostMapping
    public ResponseEntity<Application> createApplication(
            @Valid @RequestBody Application application) {
        Application createdApplication = applicationService.createApplication(application);
        return new ResponseEntity<>(createdApplication, HttpStatus.CREATED);
    }

    /**
     * Updates an existing application by ID.
     *
     * @param id          the ID of the application to update
     * @param application the updated Application object
     * @return ResponseEntity with the updated Application and HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(
            @PathVariable String id,
            @Valid @RequestBody Application application) {
        return ResponseEntity.ok(applicationService.updateApplication(id, application));
    }

    /**
     * Deletes an application by ID. Requires ADMIN role.
     *
     * @param id the ID of the application to delete
     * @return ResponseEntity with HTTP 204 (No Content)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteApplication(@PathVariable String id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all applications for a specific job.
     *
     * @param jobId the ID of the job
     * @return ResponseEntity with list of Application objects and HTTP 200
     */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Application>> getApplicationsByJobId(@PathVariable String jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsByJobId(jobId));
    }

    /**
     * Retrieves a paginated list of applications for a specific job.
     *
     * @param jobId     the ID of the job
     * @param page      zero-based page index
     * @param size      page size
     * @param sortBy    field to sort by
     * @param direction sort direction ("asc" or "desc")
     * @return ResponseEntity with a Page of Application objects and HTTP 200
     */
    @GetMapping("/job/{jobId}/paged")
    public ResponseEntity<Page<Application>> getPagedApplicationsByJobId(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(applicationService.getApplicationsByJobId(jobId, pageable));
    }

    /**
     * Retrieves all applications submitted by a specific user email.
     *
     * @param email the email address of the applicant
     * @return ResponseEntity with list of Application objects and HTTP 200
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<List<Application>> getApplicationsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(applicationService.getApplicationsByEmail(email));
    }

    /**
     * Retrieves a paginated list of applications by user email.
     *
     * @param email     the email address of the applicant
     * @param page      zero-based page index
     * @param size      page size
     * @param sortBy    field to sort by
     * @param direction sort direction ("asc" or "desc")
     * @return ResponseEntity with a Page of Application objects and HTTP 200
     */
    @GetMapping("/email/{email}/paged")
    public ResponseEntity<Page<Application>> getPagedApplicationsByEmail(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(applicationService.getApplicationsByEmail(email, pageable));
    }

    /**
     * Retrieves all applications filtered by status.
     *
     * @param status the ApplicationStatus enum value to filter by
     * @return ResponseEntity with list of Application objects and HTTP 200
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Application>> getApplicationsByStatus(@PathVariable Application.ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.getApplicationsByStatus(status));
    }

    /**
     * Retrieves a paginated list of applications filtered by status.
     *
     * @param status    the ApplicationStatus enum value to filter by
     * @param page      zero-based page index
     * @param size      page size
     * @param sortBy    field to sort by
     * @param direction sort direction ("asc" or "desc")
     * @return ResponseEntity with a Page of Application objects and HTTP 200
     */
    @GetMapping("/status/{status}/paged")
    public ResponseEntity<Page<Application>> getPagedApplicationsByStatus(
            @PathVariable Application.ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(applicationService.getApplicationsByStatus(status, pageable));
    }
}