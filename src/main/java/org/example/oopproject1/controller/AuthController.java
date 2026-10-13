package org.example.oopproject1.controller;

import jakarta.validation.Valid;
import org.example.oopproject1.dto.JwtResponse;
import org.example.oopproject1.dto.LoginRequest;
import org.example.oopproject1.dto.SignupRequest;
import org.example.oopproject1.model.Recruiter;
import org.example.oopproject1.model.User;
import org.example.oopproject1.security.JwtUtils;
import org.example.oopproject1.service.RecruiterService;
import org.example.oopproject1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling authentication operations.
 * <p>
 * Provides login and user registration endpoints,
 * including separate logic for recruiter profile creation.
 * </p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private RecruiterService recruiterService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Authenticates a user using provided credentials and generates a JWT token.
     *
     * @param loginRequest the login request containing username and password
     * @return ResponseEntity containing JwtResponse with token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        User userDetails = (User) authentication.getPrincipal();
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRoles()));
    }

    /**
     * Registers a new user or recruiter.
     * <p>
     * If the role is RECRUITER, a Recruiter profile is also created.
     * </p>
     *
     * @param signupRequest the signup request containing user details and optional recruiter info
     * @return ResponseEntity containing MessageResponse with success or error message
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            String role = (signupRequest.getRole() == null || signupRequest.getRole().isEmpty())
                    ? "USER"
                    : signupRequest.getRole();
            User user = userService.registerUser(
                    signupRequest.getUsername(),
                    signupRequest.getEmail(),
                    signupRequest.getPassword(),
                    role);

            if ("RECRUITER".equals(role)) {
                Recruiter recruiter = new Recruiter();
                recruiter.setName(signupRequest.getUsername());
                recruiter.setEmail(signupRequest.getEmail());
                recruiter.setCompany(
                        signupRequest.getCompany() != null ? signupRequest.getCompany() : "Your Company");
                recruiter.setPosition(
                        signupRequest.getPosition() != null ? signupRequest.getPosition() : "Recruiter");
                recruiter.setPhone(signupRequest.getPhone() != null ? signupRequest.getPhone() : "");
                recruiterService.createRecruiter(recruiter);
            }

            // return 201 with no body
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (RuntimeException e) {
            // return 400 with the error message in the body
            return ResponseEntity
                    .badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .body(null);
        }
    }

}
