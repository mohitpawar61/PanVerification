package com.verify.panverification.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private  String SECRET;

    public String generateToken(
            String email
            ,String role
    )
    {
        log.debug("Generating JWT Token for email: {}, role: {}",email,role);
        String token = Jwts.builder()
                .subject(email)
                .claim("role",role)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis()+ 8640000))

                                .signWith(
                                        Keys.hmacShaKeyFor(
                                                SECRET.getBytes()
                                        )
                                ).compact();

        log.debug("JWT token generated successfully for: {}",email);

        return token;
    }


    public String extractUsername(
            String token
    )
    {
        log.debug("Extracting username from JWT token");
        String username = Jwts.parser()
                .verifyWith(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes()
                        )
                )
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        log.debug("Extracted username: {}",username);
        return username;
    }
    public String extractRole(String token) {

        log.debug("Extracting role from JWT token");
        String role = Jwts.parser()
                .verifyWith(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes()
                        )
                )
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);

        log.debug("Extracted role: {}",role);
        return role;
    }
}
