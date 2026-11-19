package com.umadjo.api.infra.authentication.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import tools.jackson.databind.json.JsonMapper;

class SecurityErrorHandlerTest {

    private final SecurityErrorHandler handler = new SecurityErrorHandler(JsonMapper.builder().build());

    @Test
    @DisplayName("401: responde JSON no formato ErrorResponse")
    void unauthorized() throws Exception {
        var response = new MockHttpServletResponse();

        handler.commence(new MockHttpServletRequest(), response, new InsufficientAuthenticationException("sem token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString(StandardCharsets.UTF_8))
                .isEqualTo("{\"message\":\"Token inválido, expirado ou ausente\",\"details\":[],\"code\":401}");
    }

    @Test
    @DisplayName("403: responde JSON no formato ErrorResponse")
    void forbidden() throws Exception {
        var response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("sem role"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString(StandardCharsets.UTF_8))
                .isEqualTo("{\"message\":\"Você não tem permissão para realizar esta ação\",\"details\":[],\"code\":403}");
    }
}
