package com.project.tickets.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * DEMO-ONLY security, active only when SPRING_PROFILES_ACTIVE=render (deployment without Keycloak).
 *
 * It does two things:
 *   1. permits every request (no login/token required), and
 *   2. injects a fixed fake JWT into each request, so controllers that read
 *      @AuthenticationPrincipal Jwt (e.g. jwt.getSubject()) do NOT throw a NullPointerException.
 *
 * The real, Keycloak-backed security in {@link SecurityConfig} runs for every other profile
 * (it is annotated @Profile("!render")). Nothing here affects local development.
 */
@Configuration
@Profile("render")
public class SecurityConfigRender {

    // Fixed demo user id used as the JWT "subject" (jwt.getSubject()).
    private static final String DEMO_USER_ID = "00000000-0000-0000-0000-000000000000";

    @Bean
    public SecurityFilterChain renderSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // add AFTER the context filter so our fake authentication is not wiped out
                .addFilterAfter(new DemoJwtInjectionFilter(), SecurityContextHolderFilter.class);

        return http.build();
    }

    /**
     * Puts a fixed demo user into the SecurityContext for every request so downstream
     * controllers behave as if a Keycloak user were logged in.
     */
    static class DemoJwtInjectionFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            Jwt jwt = Jwt.withTokenValue("demo-token")
                    .header("alg", "none")
                    .subject(DEMO_USER_ID)
                    .claim("preferred_username", "demo-user")
                    .claim("email", "demo@example.com")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_ORGANIZER"),
                    new SimpleGrantedAuthority("ROLE_STAFF"),
                    new SimpleGrantedAuthority("ROLE_USER"));

            SecurityContextHolder.getContext()
                    .setAuthentication(new JwtAuthenticationToken(jwt, authorities));

            filterChain.doFilter(request, response);
        }
    }
}
