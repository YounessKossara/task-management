package org.example.task_project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

        @Value("${keycloak.auth-server-url}")
        private String authServerUrl;

        @Value("${keycloak.realm}")
        private String realm;

        @Bean
        public OpenAPI openAPI() {
                String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

                return new OpenAPI()
                                .info(new Info()
                                                .title("Task Manager API")
                                                .description("API de gestion de projets et tâches")
                                                .version("1.0"))
                                .addSecurityItem(new SecurityRequirement().addList("Keycloak OAuth2"))
                                .components(new Components()
                                                .addSecuritySchemes("Keycloak OAuth2",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.OAUTH2)
                                                                                .description("Connecte-toi avec tes identifiants Keycloak")
                                                                                .flows(new OAuthFlows()
                                                                                                .password(new OAuthFlow()
                                                                                                                .tokenUrl(tokenUrl)))));
        }
}
