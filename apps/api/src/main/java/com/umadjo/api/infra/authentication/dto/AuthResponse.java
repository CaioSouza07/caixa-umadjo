package com.umadjo.api.infra.authentication.dto;

import com.umadjo.api.domain.user.dto.UserResponse;

public record AuthResponse(String token, UserResponse user) {}
