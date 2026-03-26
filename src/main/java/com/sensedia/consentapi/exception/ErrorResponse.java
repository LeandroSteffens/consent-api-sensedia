package com.sensedia.consentapi.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private List<String> validationErrors; // Aqui vão entrar os erros de CPF, tamanho, etc.
}