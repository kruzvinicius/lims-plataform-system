package com.kruzvinicius.limsbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) documentation configuration.
 * Configures JWT Bearer authentication and API metadata.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI limsOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("LIMS Platform API")
                        .description("""
                                Laboratory Information Management System — REST API.
                                
                                **Available modules:**
                                - 🔬 Samples — registration, traceability, status flow
                                - 📋 Service Orders — creation, assignment, SLA
                                - ✅ Test Results — recording, approval, rejection
                                - ⚠️ Non-Conformances — lifecycle OPEN→CLOSED
                                - 🔧 Equipment — calibration, maintenance, alerts
                                - 📊 Reports — operational and managerial dashboard
                                - 📤 Export — CSV download
                                - 🔐 Audit — change tracking via Hibernate Envers
                                
                                **Authentication:** Use `POST /api/auth/login` to obtain a JWT token, 
                                then click on the 🔒 Authorize button and enter: `Bearer {your_token}`
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("LIMS Team")
                                .email("admin@lims.local")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained via /api/auth/login")));
    }
}
