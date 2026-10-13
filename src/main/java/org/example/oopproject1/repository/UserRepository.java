package org.example.oopproject1.repository;

import org.example.oopproject1.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for accessing User entities in MongoDB.
 * <p>
 * Extends MongoRepository to provide CRUD operations,
 * and includes methods to lookup users by username and check
 * for existence by username or email.
 * </p>
 *
 * @since 1.0
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the User if found, or empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search for
     * @return an Optional containing the email if found, or empty otherwise
     */
    Optional<User> findByEmail(String email);
    /**
     * Checks if a user exists by their username.
     *
     * @param username the username to check for existence
     * @return true if a user with the given username exists, false otherwise
     */
    Boolean existsByUsername(String username);

    /**
     * Checks if a user exists by their email address.
     *
     * @param email the email to check for existence
     * @return true if a user with the given email exists, false otherwise
     */
    Boolean existsByEmail(String email);
}