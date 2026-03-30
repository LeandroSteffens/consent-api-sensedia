package com.sensedia.consentapi.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ConsentHistoryResponse {

    private String action;
    private LocalDateTime timestamp;
    private ConsentResponse consentSnapshot;
}