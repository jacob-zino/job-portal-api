package org.example.oopproject1.repository;

import org.example.oopproject1.model.Recruiter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing Recruiter entities in MongoDB.
 * <p>
 * Extends MongoRepository to provide CRUD operations, pagination,
 * filtering by company, and lookup by email.
 * </p>
 *
 * @since 1.0
 */
@Repository
public interface RecruiterRepository extends MongoRepository<Recruiter, String> {

    /**
     * Retrieves a paginated list of all recruiters.
     *
     * @param pageable pagination and sorting information
     * @return a Page of Recruiter objects
     */
    Page<Recruiter> findAll(Pageable pageable);

    /**
     * Retrieves a paginated list of recruiters filtered by company name.
     *
     * @param company  the company name to filter by
     * @param pageable pagination and sorting information
     * @return a Page of Recruiter objects matching the company
     */
    Page<Recruiter> findByCompany(String company, Pageable pageable);

    /**
     * Retrieves all recruiters filtered by company name (non-paginated).
     *
     * @param company the company name to filter by
     * @return a List of Recruiter objects matching the company
     */
    List<Recruiter> findByCompany(String company);

    /**
     * Retrieves a recruiter by their email address.
     *
     * @param email the email address to search by
     * @return an Optional containing the Recruiter if found, or empty otherwise
     */
    Optional<Recruiter> findByEmail(String email);
}