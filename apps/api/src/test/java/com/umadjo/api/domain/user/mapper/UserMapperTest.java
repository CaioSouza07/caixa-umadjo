package com.umadjo.api.domain.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umadjo.api.domain.user.dto.CreateUserRequest;
import com.umadjo.api.domain.user.dto.UserResponse;
import com.umadjo.api.domain.user.entity.User;
import com.umadjo.api.domain.user.entity.UserRole;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    @DisplayName("toEntity: copia nome, normaliza e-mail, usa o hash recebido e role COMMON por padrão")
    void toEntityMapsFieldsWithDefaultRole() {
        var request = new CreateUserRequest("Fulano", " Fulano@Teste.com ", "senha-em-texto", null);

        User user = mapper.toEntity(request, "hash-bcrypt");

        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isEqualTo("Fulano");
        assertThat(user.getEmail()).isEqualTo("fulano@teste.com");
        assertThat(user.getPassword()).isEqualTo("hash-bcrypt");
        assertThat(user.getRole()).isEqualTo(UserRole.COMMON);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("toEntity: mantém a role ADMIN quando informada")
    void toEntityKeepsAdminRole() {
        var request = new CreateUserRequest("Chefe", "chefe@teste.com", "x", UserRole.ADMIN);

        assertThat(mapper.toEntity(request, "hash").getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("toResponse: expõe id, nome, e-mail e role (sem senha)")
    void toResponseMapsPublicFields() {
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setName("Fulano");
        user.setEmail("fulano@teste.com");
        user.setPassword("hash");
        user.setRole(UserRole.ADMIN);

        UserResponse response = mapper.toResponse(user);

        assertThat(response).isEqualTo(new UserResponse(id, "Fulano", "fulano@teste.com", UserRole.ADMIN));
    }
}
