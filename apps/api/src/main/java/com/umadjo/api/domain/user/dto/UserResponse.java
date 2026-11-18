package com.umadjo.api.domain.user.dto;

import java.util.UUID;

import com.umadjo.api.domain.user.entity.UserRole;

public record UserResponse(UUID id, String name, String email, UserRole role) {}
