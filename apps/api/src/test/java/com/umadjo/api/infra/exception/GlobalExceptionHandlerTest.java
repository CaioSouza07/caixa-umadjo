package com.umadjo.api.infra.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import com.umadjo.api.domain.user.dto.CreateUserRequest;
import com.umadjo.api.infra.authentication.controller.AuthController;
import com.umadjo.api.infra.exception.dto.ErrorResponse;
import com.umadjo.api.infra.exception.dto.FieldErrorResponse;
import com.umadjo.api.infra.exception.exceptions.UserAlreadyExistsException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /** Mesmo mecanismo que o Spring MVC usa para decidir qual @ExceptionHandler trata cada exceção. */
    private static final ExceptionHandlerMethodResolver RESOLVER = new ExceptionHandlerMethodResolver(GlobalExceptionHandler.class);

    private static String handlerChosenBySpringFor(Exception ex) {
        return RESOLVER.resolveMethod(ex).getName();
    }

    /** Todo erro precisa sair com status HTTP == code do corpo. */
    private void assertError(ResponseEntity<ErrorResponse> response, HttpStatus status, String message) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(status.value());
        assertThat(response.getBody().message()).isEqualTo(message);
    }

    @Test
    @DisplayName("validação: 400 com um item em details por campo inválido")
    void validation() throws Exception {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "data");
        bindingResult.addError(new FieldError("data", "email", "Formato de e-mail inválido"));
        bindingResult.addError(new FieldError("data", "password", "Senha é obrigatória"));
        var parameter = new MethodParameter(AuthController.class.getMethod("register", CreateUserRequest.class), 0);

        var response = handler.handleValidation(new MethodArgumentNotValidException(parameter, bindingResult));

        assertError(response, HttpStatus.BAD_REQUEST, "Dados inválidos");
        assertThat(response.getBody().details()).containsExactly(
                new FieldErrorResponse("email", "Formato de e-mail inválido"),
                new FieldErrorResponse("password", "Senha é obrigatória"));
    }

    @Test
    @DisplayName("corpo ilegível: 400 com details vazio")
    void unreadableBody() {
        var ex = new HttpMessageNotReadableException("JSON quebrado", new MockHttpInputMessage(new byte[0]));

        var response = handler.handleUnreadableBody(ex);

        assertError(response, HttpStatus.BAD_REQUEST, "Corpo da requisição inválido");
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    @DisplayName("usuário já existe: 409 com a mensagem da exceção")
    void userAlreadyExists() {
        var response = handler.handleUserAlreadyExists(new UserAlreadyExistsException("E-mail informado já cadastrado"));

        assertError(response, HttpStatus.CONFLICT, "E-mail informado já cadastrado");
    }

    @Test
    @DisplayName("falhas de login (senha errada, inexistente, desativado, bloqueado): 401 'E-mail ou senha inválidos'")
    void loginFailures() {
        List<AuthenticationException> loginFailures = List.of(
                new BadCredentialsException("Bad credentials"),
                new UsernameNotFoundException("not found"),
                new DisabledException("User is disabled"),
                new LockedException("User is locked"));

        for (AuthenticationException ex : loginFailures) {
            assertThat(handlerChosenBySpringFor(ex)).as(ex.getClass().getSimpleName()).isEqualTo("handleLoginFailure");
            assertError(handler.handleLoginFailure(ex), HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
        }
    }

    @Test
    @DisplayName("outras falhas de autenticação: 401 'Autenticação necessária' (não fala em e-mail/senha)")
    void otherAuthenticationFailures() {
        var ex = new InsufficientAuthenticationException("Full authentication is required");

        assertThat(handlerChosenBySpringFor(ex)).isEqualTo("handleAuthentication");
        assertError(handler.handleAuthentication(ex), HttpStatus.UNAUTHORIZED, "Autenticação necessária");
    }

    @Test
    @DisplayName("autorização negada no controller (ex.: @PreAuthorize): 403, e não 500 pelo handler genérico")
    void accessDenied() {
        List<AccessDeniedException> denied = List.of(
                new AccessDeniedException("Access Denied"),
                new AuthorizationDeniedException("Access Denied"));

        for (AccessDeniedException ex : denied) {
            assertThat(handlerChosenBySpringFor(ex)).as(ex.getClass().getSimpleName()).isEqualTo("handleAccessDenied");
            assertError(handler.handleAccessDenied(ex), HttpStatus.FORBIDDEN, "Você não tem permissão para realizar esta ação");
        }
    }

    @Test
    @DisplayName("exceções do Spring MVC mantêm o status original com mensagem em português")
    void springMvcExceptions() {
        assertError(handler.handleUnexpected(new ResponseStatusException(HttpStatus.NOT_FOUND)),
                HttpStatus.NOT_FOUND, "Recurso não encontrado");
        assertError(handler.handleUnexpected(new HttpRequestMethodNotSupportedException("GET")),
                HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado para este recurso");
        assertError(handler.handleUnexpected(new HttpMediaTypeNotSupportedException("text/plain")),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type não suportado");
        assertError(handler.handleUnexpected(new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT)),
                HttpStatus.UNPROCESSABLE_CONTENT, "Requisição inválida");
    }

    @Test
    @DisplayName("exceção inesperada: 500 sem vazar detalhes internos")
    void unexpected() {
        var response = handler.handleUnexpected(new IllegalStateException("detalhe interno sensível"));

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor");
        assertThat(response.getBody().details()).isEqualTo(List.of());
    }
}
