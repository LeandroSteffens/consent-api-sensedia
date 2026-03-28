package com.sensedia.consentapi.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade de domínio que representa o histórico de auditoria (audit trail)
 * das alterações de estado de um consentimento.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "consent_history")
public class ConsentHistory {

    /**
     * Identificador único do registro de histórico.
     */
    @Id
    private UUID id;

    /**
     * Identificador único do consentimento original que sofreu a mutação.
     */
    private UUID consentId;

    /**
     * Tipo de ação realizada sobre o consentimento (ex: CREATE, UPDATE, REVOKE).
     */
    private String action;

    /**
     * Cópia exata (snapshot) do estado do consentimento imediatamente após a ação.
     * Salvo como um sub-documento embutido no MongoDB.
     */
    private Consent consentSnapshot;

    /**
     * Data e hora exatas em que a alteração foi registrada no sistema.
     */
    private LocalDateTime timestamp;
}