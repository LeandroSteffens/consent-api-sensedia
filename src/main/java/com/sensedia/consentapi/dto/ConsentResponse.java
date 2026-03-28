package com.sensedia.consentapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sensedia.consentapi.domain.ConsentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO que representa o payload de saída dos consentimentos.
 * Padroniza os dados expostos pela API e formata as datas para o padrão.
 */
@Data
public class ConsentResponse {

    private UUID id;
    private String cpf;
    private ConsentStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime creationDateTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime expirationDateTime;

    private String additionalInfo;

    // Dados de endereço (populados caso o CEP tenha sido enviado na requisição)
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
}