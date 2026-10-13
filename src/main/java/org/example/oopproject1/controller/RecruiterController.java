package org.example.oopproject1.controller;

import jakarta.validation.Valid;
import org.example.oopproject1.model.Application;
import org.example.oopproject1.model.Job;
import org.example.oopproject1.model.Recruiter;
import org.example.oopproject1.model.User;
import org.example.oopproject1.service.ApplicationService;
import org.example.oopproject1.service.JobService;
import org.example.oopproject1.service.RecruiterService;
import org.example.oopproject1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing recruiter profiles.
 * <p>
 * Provides endpoints to create, retrieve, update, delete, and search recruiters,
 * with optional pagination and role-based access control.
 * Also exposes “/me” endpoints for the authenticated recruiter to manage
 * their own profile, list their jobs, and view applications to their jobs.
 * </p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/recruiters")
public class RecruiterController {

    private final RecruiterService recruiterService;
    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    @Autowired
    public RecruiterController(
            RecruiterService recruiterService,
            UserService userService,
            JobService jobService,
            ApplicationService applicationService
    ) {
        this.recruiterService   = recruiterService;
        this.userService        = userService;
        this.jobService         = jobService;
        this.applicationService = applicationService;
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Admin & public endpoints
    // ───────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves all recruiters.
     *
     * @return HTTP 200 with list of all recruiters
     */
    @GetMapping
    public ResponseEntity<List<Recruiter>> getAllRecruiters() {
        return ResponseEntity.ok(recruiterService.getAllRecruiters());
    }

    /**
     * Retrieves a recruiter by email.
     *
     * @param email email address
     * @return HTTP 200 with recruiter or 404 if not found
     */
    @GetMapping("/byEmail")
    public ResponseEntity<Recruiter> getRecruiterByEmail(@RequestParam String email) {
        Optional<Recruiter> rec = recruiterService.getRecruiterByEmail(email);
        return rec.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a paginated list of recruiters.
     *
     * @param page      zero-based page index (default 0)
     * @param size      page size (default 10)
     * @param sortBy    field to sort by (default "name")
     * @param direction "asc" or "desc" (default "asc")
     * @return HTTP 200 with a Page of recruiters
     */
    @GetMapping("/paged")
    public ResponseEntity<Page<Recruiter>> getPagedRecruiters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction dir = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        return ResponseEntity.ok(recruiterService.getAllRecruiters(pageable));
    }

    /**
     * Retrieves a recruiter by ID.
     *
     * @param id recruiter ID
     * @return HTTP 200 with recruiter
     */
    @GetMapping("/{id}")
    public ResponseEntity<Recruiter> getRecruiterById(@PathVariable String id) {
        return ResponseEntity.ok(recruiterService.getRecruiterById(id));
    }

    /**
     * Creates a new recruiter and associated user.
     *
     * @param recruiter payload; ID will be ignored
     * @return HTTP 201 with created recruiter
     */
    @PostMapping
    public ResponseEntity<Recruiter> createRecruiter(@Valid @RequestBody Recruiter recruiter) {
        recruiter.setId(null);
        try {
            userService.registerUser(
                    recruiter.getName(),
                    recruiter.getEmail(),
                    "recruiter123",
                    "RECRUITER"
            );
        } catch (Exception e) {
            // user may already exist
        }
        Recruiter created = recruiterService.createRecruiter(recruiter);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Updates any recruiter by ID.
     *
     * @param id        recruiter ID
     * @param recruiter updated data
     * @return HTTP 200 with updated recruiter
     */
    @PutMapping("/{id}")
    public ResponseEntity<Recruiter> updateRecruiter(
            @PathVariable String id,
            @Valid @RequestBody Recruiter recruiter
    ) {
        Recruiter existing = recruiterService.getRecruiterById(id);
        String oldEmail = existing.getEmail();
        String oldName  = existing.getName();

        Recruiter updated = recruiterService.updateRecruiter(id, recruiter);

        if (!oldEmail.equals(updated.getEmail()) || !oldName.equals(updated.getName())) {
            userService.findByEmail(oldEmail)
                    .ifPresent(u -> userService.updateUserProfile(u, updated.getName(), updated.getEmail()));
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a recruiter by ID (ADMIN only).
     *
     * @param id recruiter ID
     * @return HTTP 204
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRecruiter(@PathVariable String id) {
        Recruiter rec = recruiterService.getRecruiterById(id);
        String email = rec.getEmail();
        recruiterService.deleteRecruiter(id);
        userService.findByEmail(email).ifPresent(u -> userService.deleteUserById(u.getId()));
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves recruiters by company.
     *
     * @param company company name
     * @return HTTP 200 with list
     */
    @GetMapping("/company/{company}")
    public ResponseEntity<List<Recruiter>> getRecruitersByCompany(@PathVariable String company) {
        return ResponseEntity.ok(recruiterService.getRecruitersByCompany(company));
    }

    /**
     * Retrieves a paginated list of recruiters filtered by company.
     *
     * @param company   company name
     * @param page      zero-based page index
     * @param size      page size
     * @param sortBy    sort field
     * @param direction "asc" or "desc"
     * @return HTTP 200 with Page
     */
    @GetMapping("/company/{company}/paged")
    public ResponseEntity<Page<Recruiter>> getPagedRecruitersByCompany(
            @PathVariable String company,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction dir = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        return ResponseEntity.ok(recruiterService.getRecruitersByCompany(company, pageable));
    }

    // ───────────────────────────────────────────────────────────────────────────
    // “Me” endpoints for the authenticated recruiter
    // ───────────────────────────────────────────────────────────────────────────

    /**
     * Lookup the Recruiter entity for the currently authenticated user.
     *
     * @param auth Spring Security Authentication
     * @return the Recruiter
     */
    private Recruiter lookupCurrentRecruiter(Authentication auth) {
        String username = auth.getName(); // this is the User.username
        User u = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return recruiterService.getRecruiterByEmail(u.getEmail())
                .orElseThrow(() -> new RuntimeException("Recruiter profile not found for " + u.getEmail()));
    }

    /**
     * View your own recruiter profile.
     *
     * @param auth Spring Security Authentication
     * @return HTTP 200 with your profile
     */
    @GetMapping("/me")
    public ResponseEntity<Recruiter> getMyProfile(Authentication auth) {
        return ResponseEntity.ok(lookupCurrentRecruiter(auth));
    }

    /**
     * Update your own recruiter profile.
     *
     * @param auth    Spring Security Authentication
     * @param updates payload
     * @return HTTP 200 with updated profile
     */
    @PutMapping("/me")
    public ResponseEntity<Recruiter> updateMyProfile(
            Authentication auth,
            @Valid @RequestBody Recruiter updates
    ) {
        Recruiter me = lookupCurrentRecruiter(auth);
        String oldEmail = me.getEmail();
        String oldName  = me.getName();

        Recruiter updated = recruiterService.updateRecruiter(me.getId(), updates);

        if (!oldEmail.equals(updated.getEmail()) || !oldName.equals(updated.getName())) {
            userService.findByEmail(oldEmail)
                    .ifPresent(u -> userService.updateUserProfile(u, updated.getName(), updated.getEmail()));
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * List your own jobs (paginated).
     *
     * @param auth     Spring Security Authentication
     * @param pageable pagination info
     * @return HTTP 200 with Page of Job
     */
    @GetMapping("/me/jobs")
    public ResponseEntity<Page<Job>> myJobs(Authentication auth, Pageable pageable) {
        Recruiter me = lookupCurrentRecruiter(auth);
        Page<Job> jobs = jobService.findJobsByRecruiter(me.getId(), pageable);
        return ResponseEntity.ok(jobs);
    }

    /**
     * List all applications received for your jobs (paginated).
     *
     * @param auth     Spring Security Authentication
     * @param pageable pagination info
     * @return HTTP 200 with Page of Application
     */
    @GetMapping("/me/applications")
    public ResponseEntity<Page<Application>> myApplications(Authentication auth, Pageable pageable) {
        Recruiter me = lookupCurrentRecruiter(auth);
        List<String> jobIds = jobService.findJobsByRecruiter(me.getId())
                .stream()
                .map(Job::getId)
                .collect(Collectors.toList());
        Page<Application> page = applicationService.findByJobIds(jobIds, pageable);
        return ResponseEntity.ok(page);
    }
}
