package com.umadjo.api.infra.exception.dto;

import java.util.List;

import org.springframework.http.HttpStatusCode;

public record ErrorResponse(String message, List<FieldErrorResponse> details, int code) {

    public static ErrorResponse of(HttpStatusCode status, String message) {
        return new ErrorResponse(message, List.of(), status.value());
    }

    public static ErrorResponse of(HttpStatusCode status, String message, List<FieldErrorResponse> details) {
        return new ErrorResponse(message, details, status.value());
    }
}
