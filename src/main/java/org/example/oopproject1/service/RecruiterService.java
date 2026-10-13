package org.example.oopproject1.service;

import org.example.oopproject1.exception.ResourceNotFoundException;
import org.example.oopproject1.model.Recruiter;
import org.example.oopproject1.repository.RecruiterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing recruiter profiles.
 * <p>
 * Provides methods to create, retrieve, update, and delete recruiter entities,
 * with support for pagination and lookup by email.
 * </p>
 *
 * @since 1.0
 */
@Service
public class RecruiterService {

    @Autowired
    private RecruiterRepository recruiterRepository;

    /**
     * Retrieves all recruiters without pagination.
     *
     * @return list of all Recruiter objects
     */
    public List<Recruiter> getAllRecruiters() {
        return recruiterRepository.findAll();
    }

    /**
     * Retrieves a paginated list of all recruiters.
     *
     * @param pageable pagination and sorting information
     * @return Page of Recruiter objects
     */
    public Page<Recruiter> getAllRecruiters(Pageable pageable) {
        return recruiterRepository.findAll(pageable);
    }

    /**
     * Retrieves a recruiter by its unique identifier.
     *
     * @param id the ID of the recruiter to retrieve
     * @return the Recruiter object
     * @throws ResourceNotFoundException if no recruiter is found with the given ID
     */
    public Recruiter getRecruiterById(String id) {
        return recruiterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id: " + id));
    }

    /**
     * Retrieves a recruiter by email address.
     *
     * @param email the email of the recruiter to find
     * @return Optional containing the Recruiter if found, or empty otherwise
     */
    public Optional<Recruiter> getRecruiterByEmail(String email) {
        return recruiterRepository.findByEmail(email);
    }

    /**
     * Creates a new recruiter profile.
     *
     * @param recruiter the Recruiter object to create
     * @return the saved Recruiter object
     */
    public Recruiter createRecruiter(Recruiter recruiter) {
        recruiter.setId(null);
        return recruiterRepository.save(recruiter);
    }

    /**
     * Updates an existing recruiter profile by ID.
     *
     * @param id                the ID of the recruiter to update
     * @param recruiterDetails  the Recruiter object containing updated data
     * @return the updated Recruiter object
     * @throws ResourceNotFoundException if no recruiter is found with the given ID
     */
    public Recruiter updateRecruiter(String id, Recruiter recruiterDetails) {
        Recruiter recruiter = getRecruiterById(id);
        recruiter.setName(recruiterDetails.getName());
        recruiter.setEmail(recruiterDetails.getEmail());
        recruiter.setCompany(recruiterDetails.getCompany());
        recruiter.setPosition(recruiterDetails.getPosition());
        recruiter.setPhone(recruiterDetails.getPhone());
        return recruiterRepository.save(recruiter);
    }

    /**
     * Deletes a recruiter profile by ID.
     *
     * @param id the ID of the recruiter to delete
     * @throws ResourceNotFoundException if no recruiter is found with the given ID
     */
    public void deleteRecruiter(String id) {
        Recruiter recruiter = getRecruiterById(id);
        recruiterRepository.delete(recruiter);
    }

    /**
     * Retrieves all recruiters filtered by company name without pagination.
     *
     * @param company the company name to filter recruiters
     * @return list of Recruiter objects matching the company
     */
    public List<Recruiter> getRecruitersByCompany(String company) {
        return recruiterRepository.findByCompany(company);
    }

    /**
     * Retrieves a paginated list of recruiters filtered by company name.
     *
     * @param company the company name to filter recruiters
     * @param pageable pagination and sorting information
     * @return Page of Recruiter objects matching the company
     */
    public Page<Recruiter> getRecruitersByCompany(String company, Pageable pageable) {
        return recruiterRepository.findByCompany(company, pageable);
    }
}