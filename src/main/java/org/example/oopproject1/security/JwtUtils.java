package org.example.oopproject1.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utility class for generating and validating JSON Web Tokens (JWT).
 * <p>
 * Provides methods to create tokens with expiration, parse claims,
 * and validate token integrity and expiration.
 * </p>
 *
 * @since 1.0
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /**
     * Secret key used to sign the JWT, base64-encoded.
     */
    @Value("${app.jwtSecret:mySecretKey}")
    private String jwtSecret;

    /**
     * JWT expiration time in milliseconds.
     */
    @Value("${app.jwtExpirationMs:86400000}")
    private int jwtExpirationMs;

    /**
     * Generates a JWT token for the authenticated user.
     *
     * @param authentication the Authentication object containing user principal
     * @return a signed JWT as a String
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parses and retrieves the username (subject) from the JWT token.
     *
     * @param token the JWT string to parse
     * @return the username extracted from the token
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validates a JWT token's signature and expiration.
     *
     * @param authToken the JWT string to validate
     * @return true if the token is valid; false otherwise
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Constructs the signing key from the base64-encoded secret.
     *
     * @return a Key object for HS256 signing
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
