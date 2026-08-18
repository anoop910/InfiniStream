package com.anoop.videoStream.service.authService;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.anoop.videoStream.Model.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(
            @Value("${jwt.secret}") String secret) {

        byte[] keyBytes =
                Decoders.BASE64.decode(secret);

        this.secretKey =
                Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {

        return Jwts.builder()

                // User ID
                .subject(
                        user.getId().toString()
                )

                // User email
                .claim(
                        "email",
                        user.getEmail()
                )

                // User role
                .claim(
                        "role",
                        user.getRole()
                )

                // Token creation time
                .issuedAt(new Date())

                // =================================
                // ACCESS TOKEN EXPIRATION
                // =================================
                // TESTING: 1 minute
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                + 1000L * 60 * 60
                        )
                )

                .signWith(secretKey)

                .compact();
    }

    public Long getUserId(String token) {

        return Long.parseLong(
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject()
        );
    }
}