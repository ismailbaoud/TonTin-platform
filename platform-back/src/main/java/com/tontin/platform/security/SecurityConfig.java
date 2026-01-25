package com.tontin.platform.security;

import com.tontin.platform.config.JwtAuthenticationEntryPoint;
import com.tontin.platform.config.JwtAuthenticationFilter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security Configuration for the TonTin Platform.
 *
 * <p>This configuration sets up JWT-based authentication, role-based authorization,
 * CORS policies, and security filters for the application.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(AbstractHttpConfigurer::disable)
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Stateless session management (JWT-based)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Configure authorization rules
            .authorizeHttpRequests(auth ->
                auth
                    // Public endpoints - no authentication required
                    .requestMatchers(
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/auth/verify",
                        "/api/v1/auth/refresh-token"
                    )
                    .permitAll()
                    // Swagger/OpenAPI documentation - public access
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/api-docs/**"
                    )
                    .permitAll()
                    // Health check endpoints - public access
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()
                    // Auth logout and profile - requires authentication (any role)
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout")
                    .hasAnyRole("CLIENT", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/auth/me")
                    .hasAnyRole("CLIENT", "ADMIN")
                    // Dart endpoints - requires CLIENT or ADMIN role
                    .requestMatchers(HttpMethod.GET, "/api/v1/dart/**")
                    .permitAll()
                    // .hasAnyRole("CLIENT", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/dart/**")
                    // .permitAll()
                    .hasAnyRole("CLIENT", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/dart/**")
                    .permitAll()
                    // .hasAnyRole("CLIENT", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/dart/**")
                    .permitAll()
                    // .hasAnyRole("CLIENT", "ADMIN")
                    // Member endpoints - requires CLIENT or ADMIN role
                    .requestMatchers(HttpMethod.GET, "/api/v1/member/**")
                    .permitAll()
                    // .hasAnyRole("CLIENT", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/member/**")
                    .permitAll()
                    // .hasAnyRole("CLIENT", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/member/**")
                    .permitAll()
                    // .hasAnyRole("CLIENT", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/member/**")
                    .permitAll()
                    // .hasAnyRole("CLIENT", "ADMIN")
                    // Admin-only endpoints (if any in the future)
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    // All other requests require authentication
                    .anyRequest()
                    .authenticated()
            )
            // Set authentication provider
            .authenticationProvider(authenticationProvider)
            // Add JWT filter before username/password authentication filter
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            )
            // Configure exception handling
            .exceptionHandling(exception ->
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            );

        return http.build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing).
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins (configure based on your frontend)
        configuration.setAllowedOrigins(
            Arrays.asList(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:8080",
                "https://tontin.example.com" // Replace with your production domain
            )
        );

        // Allow specific HTTP methods
        configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        );

        // Allow specific headers
        configuration.setAllowedHeaders(
            Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"
            )
        );

        // Expose specific headers
        configuration.setExposedHeaders(
            Arrays.asList("Authorization", "Content-Disposition")
        );

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
