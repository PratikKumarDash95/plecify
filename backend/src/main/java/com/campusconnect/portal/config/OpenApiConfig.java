package com.campusconnect.portal.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger/OpenAPI metadata with a global Bearer-JWT security scheme. */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI campusRecruitmentOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Campus Recruitment Portal API")
                        .description("""
                                Multi-university campus recruitment platform. Roles: STUDENT, COMPANY,
                                PLACEMENT_CELL, ADMIN. Authenticate via /api/v1/auth/login, then send
                                the access token as a Bearer header. Job visibility for students is
                                pre-computed by the eligibility engine.""")
                        .version("1.0.0")
                        .contact(new Contact().name("CampusConnect").email("api@campusconnect.io"))
                        .license(new License().name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the access token returned by the login endpoint.")));
    }
}
