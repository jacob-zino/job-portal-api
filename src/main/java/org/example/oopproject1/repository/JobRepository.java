package org.example.oopproject1.repository;

import org.example.oopproject1.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for accessing Job entities in MongoDB.
 * <p>
 * Extends MongoRepository to provide CRUD operations, pagination,
 * filtering by company, title, location, deadline, recruiter,
 * and keyword-based search using regular expressions.
 * </p>
 *
 * @since 1.0
 */
@Repository
public interface JobRepository extends MongoRepository<Job, String> {

    /**
     * Retrieves a paginated list of all jobs.
     *
     * @param pageable pagination and sorting information
     * @return a Page of Job objects
     */
    Page<Job> findAll(Pageable pageable);

    /**
     * Retrieves a paginated list of jobs filtered by company name.
     *
     * @param company  the company name to filter by
     * @param pageable pagination and sorting information
     * @return a Page of Job objects matching the company
     */
    Page<Job> findByCompany(String company, Pageable pageable);

    /**
     * Retrieves a paginated list of jobs containing the title keyword.
     *
     * @param title    the keyword to search within job titles
     * @param pageable pagination and sorting information
     * @return a Page of Job objects whose titles contain the keyword
     */
    Page<Job> findByTitleContaining(String title, Pageable pageable);

    /**
     * Retrieves a paginated list of active jobs by location.
     *
     * @param location the location to filter active jobs by
     * @param pageable pagination and sorting information
     * @return a Page of active Job objects in the specified location
     */
    Page<Job> findByLocationAndIsActiveTrue(String location, Pageable pageable);

    /**
     * Retrieves a paginated list of jobs with a deadline after the given date.
     *
     * @param date     the cutoff date for deadlines
     * @param pageable pagination and sorting information
     * @return a Page of Job objects whose deadline is after the date
     */
    Page<Job> findByDeadlineDateAfter(LocalDate date, Pageable pageable);

    /**
     * Retrieves a paginated list of jobs posted by a specific recruiter.
     *
     * @param recruiterId the ID of the recruiter
     * @param pageable    pagination and sorting information
     * @return a Page of Job objects associated with the recruiter
     */
    Page<Job> findByRecruiterId(String recruiterId, Pageable pageable);

    /**
     * Retrieves all jobs filtered by company name (non-paginated).
     *
     * @param company the company name to filter by
     * @return a List of Job objects matching the company
     */
    List<Job> findByCompany(String company);

    /**
     * Retrieves all jobs whose titles contain the given keyword (non-paginated).
     *
     * @param title the keyword to search within job titles
     * @return a List of Job objects whose titles contain the keyword
     */
    List<Job> findByTitleContaining(String title);

    /**
     * Retrieves all active jobs by location (non-paginated).
     *
     * @param location the location to filter active jobs by
     * @return a List of active Job objects in the specified location
     */
    List<Job> findByLocationAndIsActiveTrue(String location);

    /**
     * Retrieves all jobs with a deadline after the given date (non-paginated).
     *
     * @param date the cutoff date for deadlines
     * @return a List of Job objects whose deadline is after the date
     */
    List<Job> findByDeadlineDateAfter(LocalDate date);

    /**
     * Retrieves all jobs posted by a specific recruiter (non-paginated).
     *
     * @param recruiterId the ID of the recruiter
     * @return a List of Job objects associated with the recruiter
     */
    List<Job> findByRecruiterId(String recruiterId);

    /**
     * Searches jobs by keyword in title, company, or description using case-insensitive regex.
     *
     * @param keyword the search term for matching title, company, or description
     * @return a List of Job objects matching the keyword
     */
    @Query("{ $or: [ " +
            "{ 'title': { $regex: ?0, $options: 'i' } }, " +
            "{ 'company': { $regex: ?0, $options: 'i' } }, " +
            "{ 'description': { $regex: ?0, $options: 'i' } }, " +
            "{ 'location': { $regex: ?0, $options: 'i' } }, " +
            "{ 'employmentType': { $regex: ?0, $options: 'i' } } ] }")
    List<Job> searchByKeyword(String keyword);


    /**
     * Searches jobs by keyword with pagination using case-insensitive regex.
     *
     * @param keyword  the search term for matching title, company, or description
     * @param pageable pagination and sorting information
     * @return a Page of Job objects matching the keyword
     */
    @Query("{ $or: [ " +
            "{ 'title': { $regex: ?0, $options: 'i' } }, " +
            "{ 'company': { $regex: ?0, $options: 'i' } }, " +
            "{ 'description': { $regex: ?0, $options: 'i' } }, " +
            "{ 'location': { $regex: ?0, $options: 'i' } }, " +
            "{ 'employmentType': { $regex: ?0, $options: 'i' } } ] }")
    Page<Job> searchByKeyword(String keyword, Pageable pageable);

}
