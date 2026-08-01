package com.project.tickets.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    //Custom JWT Converter : Role Base Access from JWT token
    //1.add its obkect in securityConfig filter and 2.modify its jwt processing with this custom converter 3.add
    //API end points which should be accesed thrugh roles


    //Flow
    //JWT received from Keycloak → Extract roles → Convert them into Spring Security Granted authorities →
    // Create an Authentication object (token).

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if(null == realmAccess || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>)realmAccess.get("roles");

        return roles.stream()
                .filter(role -> role.startsWith("ROLE_"))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
