package com.umadjo.api.infra.authentication.service;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.umadjo.api.infra.exception.exceptions.TokenJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.umadjo.api.domain.user.entity.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {
    
    @Value("${api.security.token.jwt}")
    private String SECRET_API_TOKEN;

    private final String ISSUER = "caixa-umadjo-api";

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_API_TOKEN);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withExpiresAt(getExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException ex) {
            throw new TokenJwtException(ex.getMessage());
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_API_TOKEN);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException ex) {
            throw new TokenJwtException(ex.getMessage());
        }
    }

    private Instant getExpirationDate() {
        return Instant.now().plus(15, ChronoUnit.DAYS);
    }
}
