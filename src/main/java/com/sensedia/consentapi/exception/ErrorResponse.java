package com.sensedia.consentapi.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo padronizado para respostas de erro da API.
 */
@Data
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;

    /**
     * Lista detalhada de violações de campos (ex: validações do Bean Validation).
     */
    private List<String> validationErrors;
}