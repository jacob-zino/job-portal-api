package org.example.oopproject1.util;

import org.example.oopproject1.model.User;
import org.example.oopproject1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Initializes default user data on application startup.
 * <p>
 * Creates an admin user with username "admin" if one does not already exist,
 * using the password from the ADMIN_PASSWORD environment variable (skipped when unset).
 * </p>
 *
 * @since 1.0
 */
@Component
public class UserDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Executes on application startup to seed the admin account.
     * <p>
     * Checks if a user with username "admin" exists; if not,
     * creates a new User with ADMIN role and encoded password.
     * </p>
     *
     * @param args command-line arguments (ignored)
     * @throws Exception if an error occurs during initialization
     */
    @Override
    public void run(String... args) throws Exception {
        String adminPassword = System.getenv("ADMIN_PASSWORD");
        if (adminPassword == null || adminPassword.isBlank()) {
            System.out.println("ADMIN_PASSWORD not set: skipping default admin creation");
            return;
        }
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(Arrays.asList("ADMIN"));
            admin.setEnabled(true);

            userRepository.save(admin);
            System.out.println("Admin user created");
        }
    }
}