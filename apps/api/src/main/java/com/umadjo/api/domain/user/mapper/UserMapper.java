package com.umadjo.api.domain.user.mapper;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.umadjo.api.domain.user.dto.CreateUserRequest;
import com.umadjo.api.domain.user.dto.UserResponse;
import com.umadjo.api.domain.user.entity.User;
import com.umadjo.api.domain.user.entity.UserRole;

@Component 
public class UserMapper {
    
    public User toEntity(CreateUserRequest request, String passwordHash) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(normalizeEmail(request.email()));
        user.setPassword(passwordHash);
        user.setRole(request.role() != null ? request.role() : UserRole.COMMON);
        return user;
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

}
