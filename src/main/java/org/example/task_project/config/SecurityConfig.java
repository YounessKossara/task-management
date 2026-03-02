package org.example.task_project.config;

import org.example.task_project.entity.User;
import org.example.task_project.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new CustomJwtAuthenticationConverter(userRepository);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Convertit les rôles Keycloak (realm_access.roles) en format Spring Security
    // (ROLE_ADMIN)
    static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

            if (realmAccess == null || realmAccess.get("roles") == null) {
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");

            return roles.stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role)) // Explicit cast here
                    .toList();
        }
    }

    // Evaluate JWT and sync missing users into local DB
    static class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
        private final UserRepository userRepository;
        private final KeycloakRoleConverter roleConverter = new KeycloakRoleConverter();

        public CustomJwtAuthenticationConverter(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Override
        public AbstractAuthenticationToken convert(Jwt jwt) {
            Collection<String> authoritiesMapping = Collections.emptyList();
            Collection<GrantedAuthority> authorities = roleConverter.convert(jwt);

            // JIT Provisioning (Just in Time Creation)
            String keycloakId = jwt.getSubject();
            if (!userRepository.existsById(keycloakId)) {
                User newUser = new User();
                newUser.setKeycloakId(keycloakId);
                newUser.setEmail(jwt.getClaimAsString("email"));
                newUser.setPrenom(jwt.getClaimAsString("given_name"));
                newUser.setNom(jwt.getClaimAsString("family_name"));
                newUser.setCreatedAt(LocalDateTime.now());
                newUser.setUpdatedAt(LocalDateTime.now());

                // Extract role
                if (authorities != null) {
                    authorities.stream()
                            .map(GrantedAuthority::getAuthority)
                            .filter(role -> role.startsWith("ROLE_"))
                            .map(role -> role.replace("ROLE_", ""))
                            .filter(role -> !role.equalsIgnoreCase("offline_access")
                                    && !role.equalsIgnoreCase("uma_authorization")
                                    && !role.equalsIgnoreCase("default-roles-task-manager"))
                            .findFirst()
                            .ifPresent(newUser::setRole);
                }

                userRepository.save(newUser);
            }

            return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username"));
        }
    }
}
