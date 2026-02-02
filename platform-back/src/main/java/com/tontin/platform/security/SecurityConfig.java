package com.tontin.platform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig {
    

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
        .csrf(csrf-> csrf.disable())
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(req ->
            req.requestMatchers("/api/v1/client/*").permitAll()
            .requestMatchers("/api/v1/dart/**").permitAll()
            .requestMatchers("/api/v1/auth/**").permitAll()
        )
        .build();
    }
    
}
