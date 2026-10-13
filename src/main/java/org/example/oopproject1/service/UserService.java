package org.example.oopproject1.service;

import org.example.oopproject1.exception.ResourceNotFoundException;
import org.example.oopproject1.model.User;
import org.example.oopproject1.repository.ApplicationRepository;
import org.example.oopproject1.repository.JobRepository;
import org.example.oopproject1.repository.RecruiterRepository;
import org.example.oopproject1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for managing user accounts and authentication.
 * <p>
 * Implements {@link UserDetailsService} for Spring Security,
 * and provides methods to register users, update profile/status,
 * retrieve lists and stats, and delete users.
 * </p>
 *
 * @since 1.0
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final RecruiterRepository recruiterRepository;

    /**
     * Constructs the UserService with all required repositories and encoder.
     *
     * @param userRepository        the UserRepository for CRUD on users
     * @param passwordEncoder       the PasswordEncoder bean to hash passwords
     * @param jobRepository         the JobRepository for reading job counts
     * @param applicationRepository the ApplicationRepository for reading application counts
     * @param recruiterRepository   the RecruiterRepository for reading recruiter counts
     */
    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JobRepository jobRepository,
            ApplicationRepository applicationRepository,
            RecruiterRepository recruiterRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.recruiterRepository = recruiterRepository;
    }

    /**
     * {@inheritDoc}
     *
     * @param username the username to look up
     * @return the {@link UserDetails} for Spring Security
     * @throws UsernameNotFoundException if no user exists with that username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + username)
                );
    }

    /**
     * Find a user by their username.
     *
     * @param username the lookup key
     * @return Optional containing the User if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find a user by their email.
     *
     * @param email the lookup key
     * @return Optional containing the User if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Update username and email for an existing user.
     *
     * @param user        the user entity to modify
     * @param newUsername new username (unchanged if same)
     * @param newEmail    new email (unchanged if same)
     * @return the saved User
     */
    public User updateUserProfile(User user, String newUsername, String newEmail) {
        if (!user.getUsername().equals(newUsername)) {
            user.setUsername(newUsername);
        }
        if (!user.getEmail().equals(newEmail)) {
            user.setEmail(newEmail);
        }
        return userRepository.save(user);
    }

    /**
     * Register a brand-new user account.
     *
     * @param username desired unique username
     * @param email    desired unique email
     * @param password raw password (will be hashed)
     * @param role     initial role (e.g. "USER")
     * @return the created User
     * @throws RuntimeException         if username or email already exists
     */
    public User registerUser(String username, String email, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already in use");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Retrieve all users (legacy).
     *
     * @return list of all User entities
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieve all users (alias for admin).
     *
     * @return list of all User entities
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Enable or disable a user account.
     *
     * @param id      the ID of the user to toggle
     * @param enabled true = enable, false = disable
     * @return the updated User
     * @throws ResourceNotFoundException if the user does not exist
     */
    public User updateUserStatus(String id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    /**
     * Gather high-level system stats for the admin dashboard.
     *
     * @return map with keys "totalUsers", "totalJobs", "totalApplications", "totalRecruiters"
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalJobs", jobRepository.count());
        stats.put("totalApplications", applicationRepository.count());
        stats.put("totalRecruiters", recruiterRepository.count());
        return stats;
    }

    /**
     * Fetch one user by ID.
     *
     * @param id the User’s ID
     * @return the User
     * @throws ResourceNotFoundException if the user is missing
     */
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id)
                );
    }

    /**
     * Update email and/or password on an existing user.
     *
     * @param id          the User’s ID
     * @param newEmail    new email (optional)
     * @param newPassword new raw password (optional)
     * @return the updated User
     * @throws ResourceNotFoundException if the user is missing
     */
    public User updateUserWithPassword(String id, String newEmail, String newPassword) {
        User user = getUserById(id);
        if (newEmail != null && !newEmail.isEmpty()) {
            user.setEmail(newEmail);
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        return userRepository.save(user);
    }

    /**
     * Delete a user by their ID.
     *
     * @param id the User’s ID
     * @throws ResourceNotFoundException if the user is missing
     */
    public void deleteUserById(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
