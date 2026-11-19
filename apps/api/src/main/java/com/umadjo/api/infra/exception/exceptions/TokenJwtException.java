package com.umadjo.api.infra.exception.exceptions;

public class TokenJwtException extends RuntimeException {
    public TokenJwtException(String message) {
        super(message);
    }
}
