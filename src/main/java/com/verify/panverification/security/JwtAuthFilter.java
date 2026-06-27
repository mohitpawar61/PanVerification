package com.verify.panverification.security;

import com.verify.panverification.entity.User;
import com.verify.panverification.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
        throws ServletException, IOException{

        log.debug("JWT Filter triggered for URI: {}", request.getRequestURI());
        String authHeader =
                request.getHeader("Authorization");
        log.debug("Auth header present: {}", authHeader != null);

        if(authHeader == null || !authHeader.startsWith("Bearer "))
        {
            filterChain.doFilter(
                    request,response
            );
            return;
        }

        String token =
                authHeader.substring(7);

        String email;
        String role;
        try {
            email = jwtService.extractUsername(token);
            role = jwtService.extractRole(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("User not found: " + email));
            log.info("JWT authenticated user: {}, role: {}", email, role);
            List<GrantedAuthority> authorities =
                    List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + role
                            )
                    );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }
        catch (Exception e)
        {
            log.error("JWT validation failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        }



            filterChain.doFilter(
                    request, response);



    }
}
