package com.tontin.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password Encoder Configuration.
 *
 * <p>This configuration class provides the password encoder bean used throughout
 * the application for hashing and validating passwords. It uses BCrypt, which is
 * a strong adaptive hash function designed for password hashing.</p>
 *
 * <p>BCrypt automatically incorporates a salt to protect against rainbow table
 * attacks and is computationally expensive to prevent brute-force attacks.</p>
 */
@Configuration
public class PasswordConfig {

    /**
     * The strength/cost factor for BCrypt hashing (4-31).
     * Higher values mean more secure but slower hashing.
     * Default is 10, which provides a good balance between security and performance.
     */
    @Value("${security.password.bcrypt.strength:10}")
    private int bcryptStrength;

    /**
     * Creates and configures the BCrypt password encoder.
     *
     * <p>BCrypt features:</p>
     * <ul>
     *   <li>Automatic salt generation and storage</li>
     *   <li>Adaptive cost factor (configurable rounds of hashing)</li>
     *   <li>One-way hash function (cannot be reversed)</li>
     *   <li>Resistant to rainbow table attacks</li>
     * </ul>
     *
     * <p>The strength parameter determines the log2 number of rounds of hashing.
     * For example, strength=10 means 2^10 = 1024 rounds. Each increment doubles
     * the computation time.</p>
     *
     * <p>Recommended values:</p>
     * <ul>
     *   <li>10 - Good default (moderate security, fast enough for most apps)</li>
     *   <li>12 - Better security (slower, recommended for production)</li>
     *   <li>14-16 - High security (noticeably slower, for sensitive systems)</li>
     * </ul>
     *
     * @return the configured BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }
}
