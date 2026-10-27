package com.umadjo.api.domain.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import com.umadjo.api.domain.user.dto.CreateUserRequest;
import com.umadjo.api.domain.user.mapper.UserMapper;
import com.umadjo.api.domain.user.repository.UserRepository;
import com.umadjo.api.infra.authentication.dto.AuthResponse;
import com.umadjo.api.infra.exception.exceptions.UserAlreadyExistsException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final AuthenticationManager authenticationManager;

    @Value("${api.security.token.create-user}")
    private String createUserToken;

    public AuthResponse register(CreateUserRequest data) {

        if (repository.existsByEmail(data.email())) {
            throw new UserAlreadyExistsException("E-mail informado já cadastrado");
        }
        
    }

    public AuthResponse login() {

    }

}
