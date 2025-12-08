package com.ankurshala.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String apiDescription = """
                ## Ankurshala Education Platform API
                
                Complete REST API for the Ankurshala educational platform supporting:
                
                ### Core Modules
                - **Authentication**: JWT-based registration, login, role-based access (Admin/Teacher/Student)
                - **Content Management**: Hierarchical taxonomy (Board → Grade → Subject → Chapter → Topic)
                - **Teacher Management**: Search, profiles, availability, ratings & reviews
                - **Booking System**: Class scheduling, session management, attendance tracking
                - **Payment Integration**: Razorpay integration, transaction tracking, refunds
                - **Notification System**: In-app notifications, settings management
                - **Admin Operations**: User management, content CRUD, reports, analytics
                
                ### Authentication
                Most endpoints require JWT authentication via `Authorization: Bearer <token>` header.
                Obtain tokens via `/auth/login` or `/auth/register` endpoints.
                
                ### Rate Limiting
                API implements rate limiting: 100 requests/minute per user, 1000 requests/hour per IP.
                """;
        
        return new OpenAPI()
                .info(new Info()
                        .title("Ankurshala Education Platform API")
                        .version("1.0.0")
                        .description(apiDescription)
                        .contact(new Contact()
                                .name("Ankurshala Development Team")
                                .email("support@ankurshala.com")
                                .url("https://ankurshala.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api-staging.ankurshala.com")
                                .description("Staging Environment"),
                        new Server()
                                .url("https://api.ankurshala.com")
                                .description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authentication - Obtain token via /auth/login"))
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("SESSION")
                                .description("Session cookie for authenticated requests")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
