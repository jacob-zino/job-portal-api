package org.example.oopproject1.config;

import org.example.oopproject1.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the Job Portal application.
 * <p>
 * Defines password encoding, authentication provider, authentication manager,
 * and the security filter chain which enforces stateless JWT-based security.
 * </p>
 *
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructs the SecurityConfig with required dependencies.
     *
     * @param userDetailsService     service to load user-specific data.
     * @param jwtAuthenticationFilter filter to validate and process JWT tokens.
     */
    public SecurityConfig(@Lazy UserDetailsService userDetailsService,
                          @Lazy JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * DaoAuthenticationProvider configured with the custom UserDetailsService and password encoder.
     *
     * @return a configured DaoAuthenticationProvider.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager bean for use in authentication flows.
     *
     * @param authConfig the authentication configuration provided by Spring.
     * @return the AuthenticationManager instance.
     * @throws Exception if unable to retrieve the authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configures HTTP security rules, including CSRF disabling, stateless session management,
     * public and secured endpoint permissions, and JWT filter placement.
     *
     * @param http the HttpSecurity to customize.
     * @return the configured SecurityFilterChain.
     * @throws Exception if an error occurs building the filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // static resources
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/*.html").permitAll()
                        .requestMatchers("/apidocs/**").permitAll()
                        // authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // allow clients to fetch their own profile
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        // everything else requires authentication
                        .requestMatchers(
                                "/api/jobs/**",
                                "/api/applications/**",
                                "/api/recruiters/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean for password encoding using the BCrypt hashing algorithm.
     *
     * @return a BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
