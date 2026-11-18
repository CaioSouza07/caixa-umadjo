package com.umadjo.api.infra.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MaxBytesValidatorTest {

    private static final String EMOJI = "😀"; // 😀 = 4 bytes em UTF-8

    private MaxBytesValidator validator;

    @BeforeEach
    void setUp() {
        MaxBytes annotation = mock(MaxBytes.class);
        when(annotation.value()).thenReturn(72);
        validator = new MaxBytesValidator();
        validator.initialize(annotation);
    }

    @Test
    @DisplayName("null é válido (obrigatoriedade é responsabilidade do @NotBlank)")
    void nullIsValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("ASCII: 1 caractere = 1 byte; 72 passa, 73 não")
    void asciiBoundary() {
        assertThat(validator.isValid("a".repeat(72), null)).isTrue();
        assertThat(validator.isValid("a".repeat(73), null)).isFalse();
    }

    @Test
    @DisplayName("acentos: 'ç' = 2 bytes; 36 passa (72 bytes), 37 não (74 bytes)")
    void accentedBoundary() {
        assertThat(validator.isValid("ç".repeat(36), null)).isTrue();
        assertThat(validator.isValid("ç".repeat(37), null)).isFalse();
    }

    @Test
    @DisplayName("emoji: 4 bytes cada; 18 passa (72 bytes), 19 não (76 bytes)")
    void emojiBoundary() {
        assertThat(validator.isValid(EMOJI.repeat(18), null)).isTrue();
        assertThat(validator.isValid(EMOJI.repeat(19), null)).isFalse();
    }
}
