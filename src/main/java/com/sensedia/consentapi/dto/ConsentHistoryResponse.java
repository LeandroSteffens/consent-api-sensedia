package com.sensedia.consentapi.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConsentHistoryResponse {

    private String action;
    private LocalDateTime timestamp;
    private ConsentResponse consentSnapshot;
}