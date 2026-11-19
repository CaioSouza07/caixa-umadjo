package com.umadjo.api.domain.user.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umadjo.api.domain.user.dto.CreateUserRequest;
import com.umadjo.api.domain.user.dto.LoginUserRequest;
import com.umadjo.api.domain.user.dto.UserResponse;
import com.umadjo.api.domain.user.entity.User;
import com.umadjo.api.domain.user.mapper.UserMapper;
import com.umadjo.api.domain.user.repository.UserRepository;
import com.umadjo.api.infra.authentication.dto.AuthResponse;
import com.umadjo.api.infra.authentication.service.TokenService;
import com.umadjo.api.infra.exception.exceptions.UserAlreadyExistsException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public UserResponse register(CreateUserRequest data) {
        if (repository.existsByEmail(mapper.normalizeEmail(data.email()))) {
            throw new UserAlreadyExistsException("E-mail informado já cadastrado");
        }

        try {
            User user = repository.saveAndFlush(mapper.toEntity(data, passwordEncoder.encode(data.password())));
            return mapper.toResponse(user);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException("E-mail informado já cadastrado");
        }
    }

    public AuthResponse login(LoginUserRequest data) {
        var credentials = new UsernamePasswordAuthenticationToken(mapper.normalizeEmail(data.email()), data.password());
        var authentication = authenticationManager.authenticate(credentials);

        User user = (User) authentication.getPrincipal();
        return new AuthResponse(tokenService.generateToken(user), mapper.toResponse(user));
    }
}
