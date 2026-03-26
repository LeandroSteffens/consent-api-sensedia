package com.sensedia.consentapi.dto;

import com.sensedia.consentapi.domain.ConsentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ConsentResponse {

    private UUID id;
    private String cpf;
    private ConsentStatus status;
    private LocalDateTime creationDateTime;
    private LocalDateTime expirationDateTime;
    private String additionalInfo;

}