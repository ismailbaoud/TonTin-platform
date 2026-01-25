package com.tontin.platform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration for the TonTin Platform API.
 *
 * <p>This configuration provides API documentation through Swagger UI,
 * including JWT authentication setup.</p>
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configures the OpenAPI specification for the application.
     *
     * @return the OpenAPI configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // Define the security scheme name
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
            .info(
                new Info()
                    .title("TonTin Platform API")
                    .description(
                        "REST API documentation for the TonTin Platform - Dart Management System"
                    )
                    .version("1.0.0")
                    .contact(
                        new Contact()
                            .name("TonTin Platform Team")
                            .email("support@tontin.com")
                    )
                    .license(
                        new License()
                            .name("Apache 2.0")
                            .url(
                                "https://www.apache.org/licenses/LICENSE-2.0.html"
                            )
                    )
            )
            .servers(
                List.of(
                    new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server"),
                    new Server()
                        .url("https://api.tontin.com")
                        .description("Production Server")
                )
            )
            .addSecurityItem(
                new SecurityRequirement().addList(securitySchemeName)
            )
            .components(
                new Components().addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Enter JWT token obtained from the /api/v1/auth/login endpoint"
                        )
                )
            );
    }
}
