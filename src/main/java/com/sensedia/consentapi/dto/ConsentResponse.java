package com.sensedia.consentapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sensedia.consentapi.domain.ConsentStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class ConsentResponse {

    private UUID id;
    private String cpf;
    private ConsentStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime creationDateTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime expirationDateTime;

    private String additionalInfo;
}