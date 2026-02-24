package com.tontin.platform.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * JWT Service for token generation, validation, and extraction.
 *
 * <p>This service handles all JWT-related operations including:</p>
 * <ul>
 *   <li>Generating access tokens and refresh tokens</li>
 *   <li>Validating token signatures and expiration</li>
 *   <li>Extracting claims (username, expiration, custom claims)</li>
 *   <li>Differentiating between access and refresh tokens</li>
 * </ul>
 *
 * <p>Security considerations:</p>
 * <ul>
 *   <li>Uses HS256 algorithm with a strong secret key (minimum 256 bits)</li>
 *   <li>Access tokens expire after 15 minutes</li>
 *   <li>Refresh tokens expire after 30 days</li>
 *   <li>Tokens are signed to prevent tampering</li>
 *   <li>Secret key should be externalized via environment variables in production</li>
 * </ul>
 */
@Service
@Slf4j
public class JwtService {

    /**
     * Secret key for signing JWT tokens.
     *
     * <p><strong>Security Warning:</strong> In production, this MUST be externalized
     * to environment variables or a secure vault service. Never commit real secrets
     * to version control.</p>
     *
     * <p>The key must be at least 256 bits (32 bytes) for HS256 algorithm.</p>
     */
    @Value(
        "${security.jwt.secret-key:72ccaf3126509f5ad2973e3f1c81fcbf24b3cac93114eba0754b94c4c02f10d9}"
    )
    private String secretKey;

    /**
     * Access token expiration time in milliseconds.
     * Default: 15 minutes (900,000 ms)
     */
    @Value("${security.jwt.access-token.expiration:900000}")
    private long accessTokenExpiration;

    /**
     * Refresh token expiration time in milliseconds.
     * Default: 30 days (2,592,000,000 ms)
     */
    @Value("${security.jwt.refresh-token.expiration:2592000000}")
    private long refreshTokenExpiration;

    /**
     * JWT issuer identifier.
     */
    @Value("${security.jwt.issuer:tontin-platform}")
    private String issuer;

    // Token type constants
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    /**
     * Generates an access token for the given user.
     *
     * <p>Access tokens are short-lived (15 minutes) and used for API authentication.
     * They contain the user's email as the subject.</p>
     *
     * @param userDetails the authenticated user's details
     * @return the generated JWT access token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS);
        return generateToken(extraClaims, userDetails, accessTokenExpiration);
    }

    /**
     * Generates an access token with additional custom claims.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the authenticated user's details
     * @return the generated JWT access token
     */
    public String generateToken(
        Map<String, Object> extraClaims,
        UserDetails userDetails
    ) {
        extraClaims.put(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS);
        return generateToken(extraClaims, userDetails, accessTokenExpiration);
    }

    /**
     * Generates a refresh token for the given user.
     *
     * <p>Refresh tokens are long-lived (30 days) and used to obtain new access tokens
     * without requiring the user to log in again. They contain a "type: refresh" claim
     * to differentiate them from access tokens.</p>
     *
     * @param userDetails the authenticated user's details
     * @return the generated JWT refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH);
        return generateToken(claims, userDetails, refreshTokenExpiration);
    }

    /**
     * Generates a JWT token with specified claims, user details, and expiration time.
     *
     * <p>This is the core token generation method that constructs the JWT with:</p>
     * <ul>
     *   <li>Custom claims (e.g., type, roles)</li>
     *   <li>Subject (username/email)</li>
     *   <li>Issuer identifier</li>
     *   <li>Issued at timestamp</li>
     *   <li>Expiration timestamp</li>
     *   <li>Digital signature using HS256</li>
     * </ul>
     *
     * @param extraClaims additional claims to include
     * @param userDetails the user's details
     * @param expirationTime token lifetime in milliseconds
     * @return the generated JWT token string
     */
    private String generateToken(
        Map<String, Object> extraClaims,
        UserDetails userDetails,
        long expirationTime
    ) {
        long currentTimeMillis = System.currentTimeMillis();
        Date issuedAt = new Date(currentTimeMillis);
        Date expiresAt = new Date(currentTimeMillis + expirationTime);

        log.debug(
            "Generating JWT token for user: {} with expiration: {}",
            userDetails.getUsername(),
            expiresAt
        );

        return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuer(issuer)
            .setIssuedAt(issuedAt)
            .setExpiration(expiresAt)
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Validates if a token is valid for the given user.
     *
     * <p>A token is valid if:</p>
     * <ul>
     *   <li>The username in the token matches the user's username</li>
     *   <li>The token has not expired</li>
     *   <li>The signature is valid</li>
     * </ul>
     *
     * @param token the JWT token to validate
     * @param userDetails the user details to validate against
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid =
                username.equals(userDetails.getUsername()) &&
                !isTokenExpired(token);

            if (isValid) {
                log.debug("Token validation successful for user: {}", username);
            } else {
                log.warn("Token validation failed for user: {}", username);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates if a refresh token is valid for the given user.
     *
     * <p>A refresh token is valid if it meets all the criteria of a regular token
     * AND has the "type: refresh" claim set correctly.</p>
     *
     * @param token the refresh token to validate
     * @param userDetails the user details to validate against
     * @return true if the refresh token is valid, false otherwise
     */
    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final String type = extractClaim(token, claims ->
                claims.get(TOKEN_TYPE_CLAIM, String.class)
            );

            boolean isValid =
                username.equals(userDetails.getUsername()) &&
                TOKEN_TYPE_REFRESH.equals(type) &&
                !isTokenExpired(token);

            if (isValid) {
                log.debug(
                    "Refresh token validation successful for user: {}",
                    username
                );
            } else {
                log.warn(
                    "Refresh token validation failed for user: {}",
                    username
                );
            }

            return isValid;
        } catch (Exception e) {
            log.error("Refresh token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the username (subject) from the token.
     *
     * @param token the JWT token
     * @return the username extracted from the token
     * @throws ExpiredJwtException if the token is expired
     * @throws MalformedJwtException if the token is malformed
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the token using a claims resolver function.
     *
     * @param token the JWT token
     * @param claimsResolver function to extract the desired claim
     * @param <T> the type of the claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(
        String token,
        Function<Claims, T> claimsResolver
    ) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the token.
     *
     * <p>This method parses and validates the token signature, then extracts
     * all claims from the payload.</p>
     *
     * @param token the JWT token
     * @return all claims from the token
     * @throws ExpiredJwtException if the token is expired
     * @throws UnsupportedJwtException if the token format is not supported
     * @throws MalformedJwtException if the token is malformed
     * @throws SignatureException if the signature validation fails
     * @throws IllegalArgumentException if the token is null or empty
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT token is null or empty: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if the token has expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Gets the signing key used for JWT signature generation and validation.
     *
     * <p>The key is derived from the base64-encoded secret key configured in
     * application properties. The key must be at least 256 bits for HS256.</p>
     *
     * @return the signing key
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
