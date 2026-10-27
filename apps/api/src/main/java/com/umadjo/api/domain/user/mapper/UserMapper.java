package com.umadjo.api.domain.user.mapper;

import org.springframework.stereotype.Component;

import com.umadjo.api.domain.user.dto.CreateUserRequest;
import com.umadjo.api.domain.user.dto.UserResponse;
import com.umadjo.api.domain.user.entity.User;

@Component 
public class UserMapper {
    
    public User toEntity(CreateUserRequest request, String passwordHash) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordHash);
        return user;
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

}
