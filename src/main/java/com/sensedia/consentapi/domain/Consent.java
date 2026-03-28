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

/**
 * Entidade de domínio representando o consentimento no banco de dados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "consents")
public class Consent {

    @Id
    private UUID id;

    private String cpf;

    private ConsentStatus status;

    @CreatedDate
    private LocalDateTime creationDateTime;

    private LocalDateTime expirationDateTime;

    private String additionalInfo;

    // Chave única para garantir a regra de idempotência na criação
    @Indexed(unique = true)
    private String idempotencyKey;

    // Dados de endereço populados via integração externa (ViaCEP)
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
}