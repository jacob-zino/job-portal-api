package org.example.oopproject1.controller;

import jakarta.validation.Valid;
import org.example.oopproject1.model.Job;
import org.example.oopproject1.model.User;
import org.example.oopproject1.service.JobService;
import org.example.oopproject1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing job postings.
 * <p>
 * Provides endpoints to create, retrieve, update, delete, and search jobs,
 * with optional pagination and role-based access control.
 * </p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves all jobs in the system.
     *
     * @return ResponseEntity containing a list of all Job objects and HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    /**
     * Retrieves a paginated and sorted list of jobs.
     *
     * @param page      zero-based page index (default 0)
     * @param size      the size of the page to be returned (default 10)
     * @param sortBy    the field by which to sort results (default "postDate")
     * @param direction sort direction, either "asc" or "desc" (default "desc")
     * @return ResponseEntity containing a Page of Job objects and HTTP 200 status
     */
    @GetMapping("/paged")
    public ResponseEntity<Page<Job>> getPagedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(jobService.getAllJobs(pageable));
    }

    /**
     * Retrieves a specific job by its ID.
     *
     * @param id the unique identifier of the job
     * @return ResponseEntity containing the Job object and HTTP 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable String id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    /**
     * Creates a new job posting. Accessible by RECRUITER or ADMIN.
     *
     * @param job             the Job object to create
     * @param authentication  the security context authentication
     * @return ResponseEntity containing the created Job and HTTP 201 status
     */
    @PostMapping
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<Job> createJob(@Valid @RequestBody Job job, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Job created;
        if (currentUser.getRoles().contains("ADMIN")) {
            created = jobService.createJob(job);
        } else {
            created = jobService.createJob(job, currentUser.getEmail());
        }
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Updates an existing job posting by ID.
     *
     * @param id  the ID of the job to update
     * @param job the updated Job object
     * @return ResponseEntity containing the updated Job and HTTP 200 status
     */
    @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable String id, @Valid @RequestBody Job job) {
        return ResponseEntity.ok(jobService.updateJob(id, job));
    }

    /**
     * Deletes a job posting by ID. Accessible only by ADMIN.
     *
     * @param id the ID of the job to delete
     * @return ResponseEntity with HTTP 204 (No Content) status
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJob(@PathVariable String id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves jobs filtered by company name.
     *
     * @param company the company name to filter jobs by
     * @return ResponseEntity containing a list of Job objects and HTTP 200 status
     */
    @GetMapping("/company/{company}")
    public ResponseEntity<List<Job>> getJobsByCompany(@PathVariable String company) {
        return ResponseEntity.ok(jobService.findJobsByCompany(company));
    }

    /**
     * Retrieves a paginated list of jobs filtered by company name.
     *
     * @param company  the company name to filter jobs by
     * @param page     zero-based page index
     * @param size     page size
     * @param sortBy   field to sort by
     * @param direction sort direction ("asc" or "desc")
     * @return ResponseEntity containing a Page of Job objects and HTTP 200 status
     */
    @GetMapping("/company/{company}/paged")
    public ResponseEntity<Page<Job>> getPagedJobsByCompany(
            @PathVariable String company,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(jobService.findJobsByCompany(company, pageable));
    }

    /**
     * Searches jobs by a single keyword across title, company, description,
     * location and employmentType.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Job>> searchJobsByKeyword(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(jobService.searchJobs(keyword));
    }

    /**
     * Searches for jobs by a single keyword across title, company, description,
     * location and employmentType, with pagination.
     *
     * @param keyword   the term to search across multiple job fields
     * @param page      zero-based page index (default 0)
     * @param size      page size (default 10)
     * @param sortBy    field to sort by (default "postDate")
     * @param direction sort direction ("asc" or "desc", default "desc")
     */
    @GetMapping("/search/paged")
    public ResponseEntity<Page<Job>> searchPagedJobsByKeyword(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        // call the service’s keyword search (which in turn uses your regex-based repository query)
        return ResponseEntity.ok(jobService.searchJobs(keyword, pageable));
    }

    /**
     * Retrieves active jobs by location.
     *
     * @param location the location to filter active jobs
     * @return ResponseEntity containing a list of active Job objects and HTTP 200 status
     */
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Job>> getActiveJobsByLocation(@PathVariable String location) {
        return ResponseEntity.ok(jobService.findActiveJobsByLocation(location));
    }

    /**
     * Retrieves active jobs by location with pagination.
     *
     * @param location the location to filter active jobs
     * @param page     zero-based page index
     * @param size     page size
     * @param sortBy   field to sort by
     * @param direction sort direction ("asc" or "desc")
     * @return ResponseEntity containing a Page of active Job objects and HTTP 200 status
     */
    @GetMapping("/location/{location}/paged")
    public ResponseEntity<Page<Job>> getPagedActiveJobsByLocation(
            @PathVariable String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(jobService.findActiveJobsByLocation(location, pageable));
    }

    /**
     * Retrieves jobs posted by a specific recruiter.
     *
     * @param recruiterId the ID of the recruiter
     * @return ResponseEntity containing a list of Job objects and HTTP 200 status
     */
    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Job>> getJobsByRecruiter(@PathVariable String recruiterId) {
        return ResponseEntity.ok(jobService.findJobsByRecruiter(recruiterId));
    }

    /**
     * Retrieves jobs posted by a specific recruiter with pagination.
     *
     * @param recruiterId the ID of the recruiter
     * @param page        zero-based page index
     * @param size        page size
     * @param sortBy      field to sort by
     * @param direction   sort direction ("asc" or "desc")
     * @return ResponseEntity containing a Page of Job objects and HTTP 200 status
     */
    @GetMapping("/recruiter/{recruiterId}/paged")
    public ResponseEntity<Page<Job>> getPagedJobsByRecruiter(
            @PathVariable String recruiterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(jobService.findJobsByRecruiter(recruiterId, pageable));
    }
}