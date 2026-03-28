package com.sensedia.consentapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sensedia.consentapi.domain.ConsentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO que representa o payload de entrada para a criação de um novo consentimento.
 * Contém as regras de validação sintática (Bean Validation) aplicadas na requisição.
 */
@Data
public class ConsentCreateRequest {

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", message = "O CPF deve estar no formato ###.###.###-##")
    private String cpf;

    @NotNull(message = "O status é obrigatório")
    private ConsentStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime expirationDateTime;

    @Size(min = 1, max = 50, message = "O campo additionalInfo deve ter entre 1 e 50 caracteres")
    private String additionalInfo;

    /**
     * Campo opcional utilizado para enriquecimento de dados de endereço via integração externa.
     */
    private String cep;
}