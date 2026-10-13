package org.example.oopproject1.service;

import org.example.oopproject1.exception.ResourceNotFoundException;
import org.example.oopproject1.model.Job;
import org.example.oopproject1.model.Recruiter;
import org.example.oopproject1.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing job postings.
 * <p>
 * Provides methods to create, retrieve, update, and delete jobs,
 * with support for pagination, recruiter company validation,
 * and keyword-based search.
 * </p>
 *
 * @since 1.0
 */
@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private RecruiterService recruiterService;

    /**
     * Retrieves a paginated list of all jobs.
     *
     * @param pageable pagination and sorting information
     * @return Page of Job objects
     */
    public Page<Job> getAllJobs(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }

    /**
     * Retrieves all jobs without pagination.
     *
     * @return list of all Job objects
     */
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    /**
     * Retrieves a job by its unique identifier.
     *
     * @param id the ID of the job to retrieve
     * @return the Job object
     * @throws ResourceNotFoundException if no job is found with the given ID
     */
    public Job getJobById(String id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    /**
     * Creates a new job posting on behalf of a recruiter, validating company match.
     *
     * @param job           the Job object to create
     * @param recruiterEmail the email of the recruiter creating the job
     * @return the created Job object
     * @throws RuntimeException if recruiter profile not found or company mismatch
     */
    public Job createJob(Job job, String recruiterEmail) {
        Optional<Recruiter> recruiterOptional = recruiterService.getRecruiterByEmail(recruiterEmail);
        if (!recruiterOptional.isPresent()) {
            throw new RuntimeException("Recruiter profile not found");
        }

        Recruiter recruiter = recruiterOptional.get();
        if (!recruiter.getCompany().equals(job.getCompany())) {
            throw new RuntimeException(
                    "Recruiters can only post jobs for their own company: " + recruiter.getCompany());
        }

        job.setRecruiterId(recruiter.getId());
        job.setId(null);
        job.setPostDate(LocalDate.now());
        if (job.getDeadlineDate() == null) {
            job.setDeadlineDate(LocalDate.now().plusMonths(1));
        }
        job.setActive(true);
        return jobRepository.save(job);
    }

    /**
     * Creates a new job posting without recruiter validation (admin use).
     *
     * @param job the Job object to create
     * @return the created Job object
     */
    public Job createJob(Job job) {
        job.setId(null);
        job.setPostDate(LocalDate.now());
        if (job.getDeadlineDate() == null) {
            job.setDeadlineDate(LocalDate.now().plusMonths(1));
        }
        job.setActive(true);
        return jobRepository.save(job);
    }

    /**
     * Updates an existing job posting by its ID.
     *
     * @param id         the ID of the job to update
     * @param jobDetails the Job object containing updated fields
     * @return the updated Job object
     */
    public Job updateJob(String id, Job jobDetails) {
        Job job = getJobById(id);
        job.setTitle(jobDetails.getTitle());
        job.setCompany(jobDetails.getCompany());
        job.setDescription(jobDetails.getDescription());
        job.setRequiredSkills(jobDetails.getRequiredSkills());
        job.setMinSalary(jobDetails.getMinSalary());
        job.setMaxSalary(jobDetails.getMaxSalary());
        job.setLocation(jobDetails.getLocation());
        job.setEmploymentType(jobDetails.getEmploymentType());
        job.setDeadlineDate(jobDetails.getDeadlineDate());
        job.setActive(jobDetails.isActive());
        return jobRepository.save(job);
    }

    /**
     * Deletes a job posting by its ID.
     *
     * @param id the ID of the job to delete
     */
    public void deleteJob(String id) {
        Job job = getJobById(id);
        jobRepository.delete(job);
    }

    /**
     * Retrieves a paginated list of jobs filtered by company name.
     *
     * @param company  the company name to filter by
     * @param pageable pagination and sorting information
     * @return Page of Job objects matching the company
     */
    public Page<Job> findJobsByCompany(String company, Pageable pageable) {
        return jobRepository.findByCompany(company, pageable);
    }

    /**
     * Retrieves a paginated list of jobs whose titles contain the given keyword.
     *
     * @param title    the keyword to search in job titles
     * @param pageable pagination and sorting information
     * @return Page of Job objects whose titles contain the keyword
     */
    public Page<Job> findJobsByTitle(String title, Pageable pageable) {
        return jobRepository.findByTitleContaining(title, pageable);
    }

    /**
     * Retrieves a paginated list of active jobs by location.
     *
     * @param location the location to filter active jobs by
     * @param pageable pagination and sorting information
     * @return Page of active Job objects in the specified location
     */
    public Page<Job> findActiveJobsByLocation(String location, Pageable pageable) {
        return jobRepository.findByLocationAndIsActiveTrue(location, pageable);
    }

    /**
     * Retrieves a paginated list of jobs posted by a specific recruiter.
     *
     * @param recruiterId the ID of the recruiter
     * @param pageable    pagination and sorting information
     * @return Page of Job objects associated with the recruiter
     */
    public Page<Job> findJobsByRecruiter(String recruiterId, Pageable pageable) {
        return jobRepository.findByRecruiterId(recruiterId, pageable);
    }

    /**
     * Retrieves all jobs filtered by company name without pagination.
     *
     * @param company the company name to filter by
     * @return list of Job objects matching the company
     */
    public List<Job> findJobsByCompany(String company) {
        return jobRepository.findByCompany(company);
    }

    /**
     * Retrieves all jobs whose titles contain the given keyword without pagination.
     *
     * @param title the keyword to search in job titles
     * @return list of Job objects whose titles contain the keyword
     */
    public List<Job> findJobsByTitle(String title) {
        return jobRepository.findByTitleContaining(title);
    }

    /**
     * Retrieves all active jobs by location without pagination.
     *
     * @param location the location to filter active jobs by
     * @return list of active Job objects in the specified location
     */
    public List<Job> findActiveJobsByLocation(String location) {
        return jobRepository.findByLocationAndIsActiveTrue(location);
    }

    /**
     * Retrieves all jobs posted by a specific recruiter without pagination.
     *
     * @param recruiterId the ID of the recruiter
     * @return list of Job objects associated with the recruiter
     */
    public List<Job> findJobsByRecruiter(String recruiterId) {
        return jobRepository.findByRecruiterId(recruiterId);
    }

    /**
     * Performs a keyword-based search on jobs without pagination.
     *
     * @param keyword the search term for title, company, or description
     * @return list of Job objects matching the keyword
     */
    public List<Job> searchJobs(String keyword) {
        return jobRepository.searchByKeyword(keyword);
    }

    /**
     * Performs a keyword-based search on jobs with pagination.
     *
     * @param keyword  the search term for title, company, or description
     * @param pageable pagination and sorting information
     * @return Page of Job objects matching the keyword
     */
    public Page<Job> searchJobs(String keyword, Pageable pageable) {
        return jobRepository.searchByKeyword(keyword, pageable);
    }
}
