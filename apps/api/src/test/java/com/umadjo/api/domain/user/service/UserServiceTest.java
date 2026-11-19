package com.umadjo.api.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.umadjo.api.domain.user.dto.CreateUserRequest;
import com.umadjo.api.domain.user.dto.LoginUserRequest;
import com.umadjo.api.domain.user.dto.UserResponse;
import com.umadjo.api.domain.user.entity.User;
import com.umadjo.api.domain.user.entity.UserRole;
import com.umadjo.api.domain.user.mapper.UserMapper;
import com.umadjo.api.domain.user.repository.UserRepository;
import com.umadjo.api.infra.authentication.dto.AuthResponse;
import com.umadjo.api.infra.authentication.service.TokenService;
import com.umadjo.api.infra.exception.exceptions.UserAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, new UserMapper(), authenticationManager, passwordEncoder, tokenService);
    }

    private void saveReturnsEntityWithId() {
        when(repository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
    }

    @Test
    @DisplayName("register: salva com senha em hash, e-mail normalizado e role COMMON por padrão")
    void registerSavesUserWithHashedPasswordAndDefaultRole() {
        var request = new CreateUserRequest("Fulano", "  Fulano@Teste.COM ", "senha123", null);
        when(repository.existsByEmail("fulano@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
        saveReturnsEntityWithId();

        UserResponse response = service.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).saveAndFlush(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getPassword()).isEqualTo("hash-bcrypt");
        assertThat(saved.getEmail()).isEqualTo("fulano@teste.com");
        assertThat(saved.getRole()).isEqualTo(UserRole.COMMON);
        assertThat(saved.isActive()).isTrue();

        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("fulano@teste.com");
        assertThat(response.role()).isEqualTo(UserRole.COMMON);
    }

    @Test
    @DisplayName("register: respeita a role informada na requisição")
    void registerKeepsRequestedRole() {
        var request = new CreateUserRequest("Chefe", "chefe@teste.com", "senha123", UserRole.ADMIN);
        when(repository.existsByEmail("chefe@teste.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        saveReturnsEntityWithId();

        UserResponse response = service.register(request);

        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("register: e-mail já cadastrado lança UserAlreadyExistsException sem salvar")
    void registerRejectsExistingEmail() {
        var request = new CreateUserRequest("Fulano", "FULANO@teste.com", "senha123", null);
        when(repository.existsByEmail("fulano@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("E-mail informado já cadastrado");

        verify(passwordEncoder, never()).encode(anyString());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("register: violação de UNIQUE no banco (cadastro simultâneo) vira UserAlreadyExistsException")
    void registerTranslatesUniqueViolationIntoConflict() {
        var request = new CreateUserRequest("Fulano", "fulano@teste.com", "senha123", null);
        when(repository.existsByEmail("fulano@teste.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(repository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("E-mail informado já cadastrado");
    }

    @Test
    @DisplayName("login: autentica com e-mail normalizado e devolve token + usuário")
    void loginAuthenticatesWithNormalizedEmailAndReturnsToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Admin");
        user.setEmail("admin@teste.com");
        user.setRole(UserRole.ADMIN);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(tokenService.generateToken(user)).thenReturn("jwt-gerado");

        AuthResponse response = service.login(new LoginUserRequest(" ADMIN@Teste.com ", "Admin@123"));

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("admin@teste.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("Admin@123");

        assertThat(response.token()).isEqualTo("jwt-gerado");
        assertThat(response.user().email()).isEqualTo("admin@teste.com");
        assertThat(response.user().role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("login: credenciais inválidas propagam a exceção e não geram token")
    void loginPropagatesBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> service.login(new LoginUserRequest("admin@teste.com", "errada")))
                .isInstanceOf(BadCredentialsException.class);

        verify(tokenService, never()).generateToken(any());
    }
}
