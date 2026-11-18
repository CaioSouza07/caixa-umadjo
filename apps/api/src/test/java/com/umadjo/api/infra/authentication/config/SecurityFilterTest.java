package com.umadjo.api.infra.authentication.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.umadjo.api.domain.user.entity.User;
import com.umadjo.api.domain.user.entity.UserRole;
import com.umadjo.api.domain.user.repository.UserRepository;
import com.umadjo.api.infra.authentication.service.TokenService;
import com.umadjo.api.infra.exception.exceptions.TokenJwtException;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;
    @Mock
    private UserRepository userRepository;

    private SecurityFilter filter;
    private MockHttpServletRequest request;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new SecurityFilter(tokenService, userRepository);
        request = new MockHttpServletRequest("POST", "/auth/register");
        chain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User user(UserRole role, boolean active) {
        User user = new User();
        user.setEmail("fulano@teste.com");
        user.setRole(role);
        user.setActive(active);
        return user;
    }

    private void run() throws Exception {
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        // em todos os casos a requisição segue adiante; quem barra é a regra de autorização
        assertThat(chain.getRequest()).as("filterChain.doFilter foi chamado").isNotNull();
    }

    @Test
    @DisplayName("sem header Authorization: não autentica e nem tenta validar token")
    void noHeader() throws Exception {
        run();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("header sem prefixo 'Bearer ': ignorado")
    void headerWithoutBearerPrefix() throws Exception {
        request.addHeader("Authorization", "token-sem-prefixo");

        run();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("token válido de usuário ativo: autentica com as authorities da role")
    void validTokenAuthenticates() throws Exception {
        request.addHeader("Authorization", "Bearer token-bom");
        User admin = user(UserRole.ADMIN, true);
        when(tokenService.validateToken("token-bom")).thenReturn("fulano@teste.com");
        when(userRepository.findByEmail("fulano@teste.com")).thenReturn(Optional.of(admin));

        run();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isSameAs(admin);
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_COMMON");
    }

    @Test
    @DisplayName("token inválido: não autentica e não propaga a exceção")
    void invalidToken() throws Exception {
        request.addHeader("Authorization", "Bearer token-ruim");
        when(tokenService.validateToken("token-ruim")).thenThrow(new TokenJwtException("assinatura inválida"));

        run();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("token válido de usuário desativado: não autentica")
    void disabledUser() throws Exception {
        request.addHeader("Authorization", "Bearer token-bom");
        when(tokenService.validateToken("token-bom")).thenReturn("fulano@teste.com");
        when(userRepository.findByEmail("fulano@teste.com")).thenReturn(Optional.of(user(UserRole.ADMIN, false)));

        run();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("token válido de usuário que não existe mais: não autentica")
    void userNotFound() throws Exception {
        request.addHeader("Authorization", "Bearer token-bom");
        when(tokenService.validateToken("token-bom")).thenReturn("apagado@teste.com");
        when(userRepository.findByEmail("apagado@teste.com")).thenReturn(Optional.empty());

        run();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
