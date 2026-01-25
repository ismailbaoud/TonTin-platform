package com.tontin.platform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application Security Configuration.
 *
 * <p>This configuration class provides beans for authentication and authorization
 * including the authentication provider, authentication manager, and password encoder
 * integration.</p>
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates and configures the DAO authentication provider.
     *
     * <p>This provider uses the custom UserDetailsService to load user information
     * from the database and the configured PasswordEncoder to validate passwords.</p>
     *
     * @return configured DaoAuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(
            customUserDetailsService
        );
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager bean.
     *
     * <p>The AuthenticationManager is used to authenticate users during login.
     * This bean retrieves the authentication manager from the Spring Security
     * authentication configuration.</p>
     *
     * @param config the authentication configuration
     * @return the authentication manager
     * @throws Exception if unable to get the authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Provides the UserDetailsService bean for dependency injection.
     *
     * <p>This allows other components to use the UserDetailsService without
     * directly depending on the CustomUserDetailsService implementation.</p>
     *
     * @return the custom user details service
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return customUserDetailsService;
    }
}
