package org.example.oopproject1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an authenticated user in the Job Portal system.
 * <p>
 * Implements Spring Security's UserDetails to integrate with authentication,
 * storing credentials, roles, and account status.
 * </p>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User implements UserDetails {

    /**
     * Unique identifier for the user.
     */
    @Id
    private String id;

    /**
     * Username used for login and display.
     */
    private String username;

    /**
     * Encrypted password for authentication.
     */
    private String password;

    /**
     * Email address of the user.
     */
    private String email;

    /**
     * List of roles assigned to the user (e.g., "USER", "ADMIN").
     */
    private List<String> roles;

    /**
     * Indicates whether the user account is enabled (true) or disabled (false).
     */
    private boolean enabled = true;

    /**
     * Returns the authorities granted to the user based on their roles.
     *
     * @return collection of GrantedAuthority objects for Spring Security
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    /**
     * Indicates whether the user's account is non-expired.
     *
     * @return true (accounts never expire in this implementation)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is not locked.
     *
     * @return true (accounts are never locked in this implementation)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user’s credentials (password) are non-expired.
     *
     * @return true (credentials never expire in this implementation)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     *
     * @return true if enabled; false otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
