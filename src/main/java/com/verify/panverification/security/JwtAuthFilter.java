package com.verify.panverification.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
        throws ServletException, IOException{

        System.out.println("JWT Filter");
        String authHeader =
                request.getHeader("Authorization");
        System.out.println(">>> Auth header: [" + authHeader + "]");

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

            List<GrantedAuthority> authorities =
                    List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + role
                            )
                    );

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authorities
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(auth);
        }
        catch (Exception e)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        }



            filterChain.doFilter(
                    request, response);



    }
}
