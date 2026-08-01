package com.project.tickets.filters;

import com.project.tickets.domain.entities.User;
import com.project.tickets.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserProvisioningFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //TODO : extract info from jwt ->check if exists in db -> if not , store in db
        //  (every login should be stored in db)


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            UUID keycloackID = UUID.fromString(jwt.getSubject());

            if(!userRepository.existsById(keycloackID)) {
                User user = new User();
                user.setId(keycloackID);
                user.setName(jwt.getClaimAsString("preferred_username"));
                user.setEmail(jwt.getClaimAsString("email"));
            }
        }

       filterChain.doFilter(request, response);
    }
}
