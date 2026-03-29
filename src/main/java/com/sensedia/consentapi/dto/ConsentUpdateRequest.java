package com.sensedia.consentapi.dto;

import com.sensedia.consentapi.domain.ConsentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsentUpdateRequest {

    @NotNull(message = "O status é obrigatório e deve ser válido (ACTIVE, REVOKED ou EXPIRED)")
    private ConsentStatus status;

    @Future(message = "A data de expiração deve ser uma data futura")
    private LocalDateTime expirationDateTime;

    @Size(min = 1, max = 50, message = "O campo additionalInfo deve ter entre 1 e 50 caracteres")
    private String additionalInfo;
}