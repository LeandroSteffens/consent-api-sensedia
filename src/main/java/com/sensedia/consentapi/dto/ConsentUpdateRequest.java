package com.sensedia.consentapi.dto;

import com.sensedia.consentapi.domain.ConsentStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO que representa o payload para atualização de um consentimento existente.
 * Expõe apenas os campos mutáveis, protegendo dados imutáveis (como CPF e ID) contra alterações indevidas.
 */
@Data
public class ConsentUpdateRequest {

    private ConsentStatus status;

    private LocalDateTime expirationDateTime;

    @Size(min = 1, max = 50, message = "O campo additionalInfo deve ter entre 1 e 50 caracteres")
    private String additionalInfo;
}