package com.sensedia.consentapi.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data // Gera Getters, Setters, toString, equals e hashCode
@Builder // Padrão Builder para facilitar a criação de objetos nos testes e services
@NoArgsConstructor // Construtor vazio (exigência de frameworks como o Spring/Jackson)
@AllArgsConstructor // Construtor com todos os argumentos
@Document(collection = "consents") // Diz ao Spring que isso é uma coleção do MongoDB
public class Consent {

    @Id
    private UUID id;

    private String cpf;

    private ConsentStatus status;

    @CreatedDate
    private LocalDateTime creationDateTime;

    private LocalDateTime expirationDateTime;

    private String additionalInfo;

    // Para a egra de idempotência
    @Indexed(unique = true)
    private String idempotencyKey;
}