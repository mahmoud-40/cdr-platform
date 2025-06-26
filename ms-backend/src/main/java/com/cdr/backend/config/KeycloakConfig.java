package com.cdr.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class KeycloakConfig {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakConfig.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    public KeycloakConfig() {
        System.out.println("DEBUG: KeycloakConfig loaded!");
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        logger.info("Initializing JWT decoder with JWK set URI: {}", jwkSetUri);
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring security filter chain");
        http
            .cors(cors -> {
                logger.info("Configuring CORS");
                cors.configurationSource(corsConfigurationSource());
            })
            .csrf(csrf -> {
                logger.info("Disabling CSRF");
                csrf.disable();
            })
            .sessionManagement(session -> {
                logger.info("Configuring stateless session");
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            })
            .authorizeHttpRequests(auth -> {
                logger.info("Configuring authorization rules");
                auth
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated();
            })
            .oauth2ResourceServer(oauth2 -> {
                logger.info("Configuring OAuth2 resource server");
                oauth2.jwt(jwt -> {
                    logger.info("Configuring JWT authentication");
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter());
                    jwt.decoder(jwtDecoder());
                });
            });
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            System.out.println("DEBUG: JwtGrantedAuthoritiesConverter called. Claims: " + jwt.getClaims());
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            Object realmAccessObj = jwt.getClaims().get("realm_access");
            if (realmAccessObj instanceof Map) {
                Map<?, ?> realmAccess = (Map<?, ?>) realmAccessObj;
                Object rolesObj = realmAccess.get("roles");
                if (rolesObj instanceof Collection<?>) {
                    Collection<?> roles = (Collection<?>) rolesObj;
                    for (Object roleObj : roles) {
                        if (roleObj instanceof String) {
                            String role = (String) roleObj;
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                        }
                    }
                }
            }
            System.out.println("DEBUG: Extracted authorities from JWT: " + authorities);
            return authorities;
        });
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("Configuring CORS configuration");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:8083",
            "http://cdr-frontend:8083"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 