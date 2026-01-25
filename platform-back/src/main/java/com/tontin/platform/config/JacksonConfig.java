package com.tontin.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson Configuration.
 *
 * <p>This configuration class provides the ObjectMapper bean for JSON
 * serialization and deserialization throughout the application.</p>
 */
@Configuration
public class JacksonConfig {

    /**
     * Creates and configures the ObjectMapper bean.
     *
     * <p>The ObjectMapper is configured with:
     * <ul>
     *   <li>JavaTimeModule for Java 8 date/time support</li>
     *   <li>Disabled WRITE_DATES_AS_TIMESTAMPS for ISO-8601 format</li>
     *   <li>Pretty printing disabled for production efficiency</li>
     * </ul>
     *
     * @return configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
            .modules(new JavaTimeModule())
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }
}
