package com.umadjo.api.infra.authentication.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umadjo.api.domain.user.dto.UserResponse;
import com.umadjo.api.domain.user.entity.User;
import com.umadjo.api.domain.user.entity.UserRole;
import com.umadjo.api.domain.user.repository.UserRepository;
import com.umadjo.api.domain.user.service.UserService;
import com.umadjo.api.infra.authentication.config.SecurityConfig;
import com.umadjo.api.infra.authentication.config.SecurityErrorHandler;
import com.umadjo.api.infra.authentication.dto.AuthResponse;
import com.umadjo.api.infra.authentication.service.TokenService;

/**
 * Regras de acesso do SecurityConfig aplicadas aos endpoints de /auth.
 * Sobe só a camada web (sem banco): service, token e repositório são mocks.
 */
@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, SecurityErrorHandler.class })
@TestPropertySource(properties = "api.security.cors.origins=http://localhost:5173")
class AuthControllerSecurityTest {

    private static final String VALID_REGISTER = """
            {"name":"Novo","email":"novo@teste.com","password":"senha123"}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private UserRepository userRepository;

    private static User user(String email, UserRole role, boolean active) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setRole(role);
        user.setActive(active);
        return user;
    }

    @BeforeEach
    void setUp() {
        when(tokenService.validateToken("token-admin")).thenReturn("admin@teste.com");
        when(tokenService.validateToken("token-comum")).thenReturn("comum@teste.com");
        when(tokenService.validateToken("token-desativado")).thenReturn("off@teste.com");
        when(userRepository.findByEmail("admin@teste.com")).thenReturn(Optional.of(user("admin@teste.com", UserRole.ADMIN, true)));
        when(userRepository.findByEmail("comum@teste.com")).thenReturn(Optional.of(user("comum@teste.com", UserRole.COMMON, true)));
        when(userRepository.findByEmail("off@teste.com")).thenReturn(Optional.of(user("off@teste.com", UserRole.ADMIN, false)));
        when(userService.register(any())).thenReturn(
                new UserResponse(UUID.randomUUID(), "Novo", "novo@teste.com", UserRole.COMMON));
    }

    // ---------- /auth/register ----------

    @Test
    @DisplayName("register sem token: 401")
    void registerWithoutToken() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTER))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.details").isEmpty());
        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("register como ADMIN: 201")
    void registerAsAdmin() throws Exception {
        mvc.perform(post("/auth/register").header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("novo@teste.com"))
                .andExpect(jsonPath("$.role").value("COMMON"));
    }

    @Test
    @DisplayName("register como COMMON: 403 e o service nem é chamado")
    void registerAsCommon() throws Exception {
        mvc.perform(post("/auth/register").header("Authorization", "Bearer token-comum")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTER))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Você não tem permissão para realizar esta ação"));
        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("register com token de ADMIN desativado: 401")
    void registerAsDisabledAdmin() throws Exception {
        mvc.perform(post("/auth/register").header("Authorization", "Bearer token-desativado")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTER))
                .andExpect(status().isUnauthorized());
        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("register como ADMIN com corpo inválido: 400 com details")
    void registerInvalidBody() throws Exception {
        mvc.perform(post("/auth/register").header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name":"","email":"invalido","password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.details.length()").value(3));
        verify(userService, never()).register(any());
    }

    // ---------- /auth/login ----------

    @Test
    @DisplayName("login é público: POST sem token chega no service")
    void loginIsPublic() throws Exception {
        when(userService.login(any())).thenReturn(new AuthResponse("jwt",
                new UserResponse(UUID.randomUUID(), "Admin", "admin@teste.com", UserRole.ADMIN)));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@teste.com\",\"password\":\"Admin@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /auth/login: 405 (e não 401)")
    void loginWrongMethod() throws Exception {
        mvc.perform(get("/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(405));
    }

    // ---------- demais rotas ----------

    @Test
    @DisplayName("rota qualquer sem token: 401")
    void otherRoutesRequireAuthentication() throws Exception {
        mvc.perform(get("/qualquer-coisa"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
