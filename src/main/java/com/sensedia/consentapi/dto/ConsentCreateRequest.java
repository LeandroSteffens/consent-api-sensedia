package com.sensedia.consentapi.dto;

import com.sensedia.consentapi.domain.ConsentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsentCreateRequest {

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", message = "O CPF deve estar no formato ###.###.###-##")
    private String cpf;

    @NotNull(message = "O status é obrigatório")
    private ConsentStatus status;

    // Opcional, sem validação de obrigatoriedade
    private LocalDateTime expirationDateTime;

    // Opcional, mas se for enviado, deve respeitar o tamanho mínimo e máximo
    @Size(min = 1, max = 50, message = "O campo additionalInfo deve ter entre 1 e 50 caracteres")
    private String additionalInfo;
}