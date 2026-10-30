package com.umadjo.api.infra.authentication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.umadjo.api.domain.user.entity.User;

@Service
public class TokenService {
    
    @Value("${api.security.token.jwt}")
    private String SECRET_API_TOKEN;

    private final String ISSUER = "caixa-umadjo-api";

    public String generateToken(User user) {
        
        Algorithm algorithm = Algorithm.HMAC256(SECRET_API_TOKEN);
        return JWT.create()
                .withIssuer(ISSUER)
    }
}
