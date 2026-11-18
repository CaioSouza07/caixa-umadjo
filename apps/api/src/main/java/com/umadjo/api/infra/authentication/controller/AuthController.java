package com.umadjo.api.infra.authentication.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umadjo.api.domain.user.dto.CreateUserRequest;
import com.umadjo.api.domain.user.dto.LoginUserRequest;
import com.umadjo.api.domain.user.dto.UserResponse;
import com.umadjo.api.domain.user.service.UserService;
import com.umadjo.api.infra.authentication.dto.AuthResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid CreateUserRequest data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(data));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginUserRequest data) {
        return ResponseEntity.ok(userService.login(data));
    }
}
