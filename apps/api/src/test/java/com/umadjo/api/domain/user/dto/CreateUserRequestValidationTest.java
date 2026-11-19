package com.umadjo.api.domain.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umadjo.api.domain.user.entity.UserRole;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/** Garante que as anotações do DTO de cadastro estão montadas como esperado. */
class CreateUserRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private Set<String> invalidFields(CreateUserRequest request) {
        return validator.validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private Set<String> messagesFor(CreateUserRequest request, String field) {
        return validator.validate(request).stream()
                .filter(v -> v.getPropertyPath().toString().equals(field))
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("requisição válida, com ou sem role, não tem violações")
    void validRequest() {
        assertThat(invalidFields(new CreateUserRequest("Fulano", "fulano@teste.com", "senha123", null))).isEmpty();
        assertThat(invalidFields(new CreateUserRequest("Fulano", "fulano@teste.com", "senha123", UserRole.ADMIN))).isEmpty();
    }

    @Test
    @DisplayName("nome vazio, só com espaços ou acima de 150 caracteres é inválido")
    void invalidName() {
        assertThat(invalidFields(new CreateUserRequest("", "a@b.com", "senha123", null))).containsExactly("name");
        assertThat(invalidFields(new CreateUserRequest("   ", "a@b.com", "senha123", null))).containsExactly("name");
        assertThat(invalidFields(new CreateUserRequest("A".repeat(151), "a@b.com", "senha123", null))).containsExactly("name");
        assertThat(invalidFields(new CreateUserRequest("A".repeat(150), "a@b.com", "senha123", null))).isEmpty();
    }

    @Test
    @DisplayName("e-mail vazio, mal formatado ou acima de 254 caracteres é inválido")
    void invalidEmail() {
        assertThat(invalidFields(new CreateUserRequest("A", "", "senha123", null))).containsExactly("email");
        assertThat(invalidFields(new CreateUserRequest("A", "sem-arroba", "senha123", null))).containsExactly("email");
        assertThat(messagesFor(new CreateUserRequest("A", "a".repeat(64) + "@" + "b".repeat(186) + ".com", "senha123", null), "email"))
                .contains("E-mail pode ter no máximo 254 caracteres");
    }

    @Test
    @DisplayName("senha com menos de 6 caracteres é inválida")
    void passwordTooShort() {
        assertThat(messagesFor(new CreateUserRequest("A", "a@b.com", "12345", null), "password"))
                .containsExactly("Senha deve ter no mínimo 6 caracteres");
        assertThat(invalidFields(new CreateUserRequest("A", "a@b.com", "123456", null))).isEmpty();
    }

    @Test
    @DisplayName("senha é limitada a 72 BYTES: 72 ASCII passa, 73 não; 36 'ç' (72 bytes) passa, 40 'ç' (80 bytes) não")
    void passwordLimitedInBytes() {
        assertThat(invalidFields(new CreateUserRequest("A", "a@b.com", "x".repeat(72), null))).isEmpty();
        assertThat(invalidFields(new CreateUserRequest("A", "a@b.com", "x".repeat(73), null))).containsExactly("password");
        assertThat(invalidFields(new CreateUserRequest("A", "a@b.com", "ç".repeat(36), null))).isEmpty();
        assertThat(messagesFor(new CreateUserRequest("A", "a@b.com", "ç".repeat(40), null), "password"))
                .containsExactly("Senha muito longa: máximo de 72 caracteres (acentos e emojis contam como 2 a 4)");
    }
}
