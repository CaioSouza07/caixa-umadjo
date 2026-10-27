package com.umadjo.api.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

    @NotBlank(message = "Nome é obrigatório")
    @Size(message = "Nome pode ter no máximo 150 caracteres")
    String name,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    @Size(max = 6, message = "Senha deve ter no mínimo 6 caracteres")
    String password,

    String securityToken
) {}