package com.project.tickets.config;

import com.project.tickets.filters.UserProvisioningFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

//to centrally manage authorization,authentication and filters
//Steps : 1.authorizeHttpRequests() : to authoize every requests
// 2.disbale csrf
//3.make sessionManagement() : to stateless
// 4. oauth2ResourceServer() : jwt validation using default setings
//5.add user providion filter before berertoen : to auhtenticate user befoe user provisioning


@Configuration
@Profile("!render") // real Keycloak-backed security; disabled on Render (see SecurityConfigRender)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserProvisioningFilter userProvisioningFilter,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {


        http.
                authorizeHttpRequests(authorize ->
                           //http rules
                        authorize
                                .requestMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                ).permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/published-events/**").permitAll()
                                .requestMatchers("/api/v1/events").hasRole("ORGANIZER")
                                .requestMatchers("/api/v1/ticket-validations").hasRole("STAFF")

                                //api end points is acceses if role matches or permitall is set

                                .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter) // custom jwt converter
                        ))

                .addFilterAfter(userProvisioningFilter, BearerTokenAuthenticationFilter.class);

      return http.build();
    }
}
