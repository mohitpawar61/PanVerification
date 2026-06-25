package com.verify.panverification.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final String SECRET =
            "1234567890123456789012345678901234567890123456789012345678901234";

    public String generateToken(
            String email
            ,String role
    )
    {
        return Jwts.builder()
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
    }


    public String extractUsername(
            String token
    )
    {
        return Jwts.parser()
                .verifyWith(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes()
                        )
                )
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public String extractRole(String token) {

        return Jwts.parser()
                .verifyWith(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes()
                        )
                )
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}
