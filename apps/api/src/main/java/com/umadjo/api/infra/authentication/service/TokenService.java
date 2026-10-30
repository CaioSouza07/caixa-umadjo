package com.umadjo.api.infra.authentication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    
    @Value("${api.security.token.jwt}")
    private String secretApiToken;
}
