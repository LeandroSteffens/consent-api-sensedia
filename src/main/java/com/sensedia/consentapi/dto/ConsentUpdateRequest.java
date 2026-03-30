package com.sensedia.consentapi.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsentUpdateRequest {

    @Future(message = "A data de expiração deve ser uma data futura")
    private LocalDateTime expirationDateTime;

    @Size(min = 1, max = 50, message = "O campo additionalInfo deve ter entre 1 e 50 caracteres")
    private String additionalInfo;
}