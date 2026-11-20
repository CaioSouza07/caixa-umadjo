package com.umadjo.api.infra.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.umadjo.api.infra.exception.dto.ErrorResponse;
import com.umadjo.api.infra.exception.dto.FieldErrorResponse;
import com.umadjo.api.infra.exception.exceptions.UserAlreadyExistsException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> details = ex.getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Dados inválidos", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class, AccountStatusException.class })
    public ResponseEntity<ErrorResponse> handleLoginFailure(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Autenticação necessária");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Você não tem permissão para realizar esta ação");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        if (ex instanceof org.springframework.web.ErrorResponse springError) {
            HttpStatusCode status = springError.getStatusCode();
            return build(status, messageFor(status));
        }
        log.error("Erro não tratado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor");
    }

    private String messageFor(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método HTTP não suportado para este recurso";
            case 406 -> "Formato de resposta não suportado";
            case 415 -> "Content-Type não suportado";
            default -> status.is4xxClientError() ? "Requisição inválida" : "Erro interno no servidor";
        };
    }

    private ResponseEntity<ErrorResponse> build(HttpStatusCode status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message));
    }

    private ResponseEntity<ErrorResponse> build(HttpStatusCode status, String message, List<FieldErrorResponse> details) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message, details));
    }
}
