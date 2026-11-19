package com.umadjo.api.domain.user.dto;

import com.umadjo.api.domain.user.entity.UserRole;
import com.umadjo.api.infra.validation.MaxBytes;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome pode ter no máximo 150 caracteres")
    String name,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Size(max = 254, message = "E-mail pode ter no máximo 254 caracteres")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    @MaxBytes(value = 72, message = "Senha muito longa: máximo de 72 caracteres (acentos e emojis contam como 2 a 4)")
    String password,

    // Opcional: quando não informado, o usuário é criado como COMMON
    UserRole role
) {}
