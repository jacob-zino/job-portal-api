package org.example.oopproject1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Entry point for the OOP Project 1 Spring Boot application.
 * <p>
 * Bootstraps the Spring context and starts the embedded servlet container.
 * Use this class to launch the application.
 * </p>
 *
 * @author Jacob
 * @since 1.0
 */
@SpringBootApplication
public class Oopproject1Application {

    /**
     * Main method, used to launch the Spring Boot application.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        SpringApplication.run(Oopproject1Application.class, args);
    }
}
