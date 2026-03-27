package com.sensedia.consentapi.service;

import com.sensedia.consentapi.domain.Consent;
import com.sensedia.consentapi.domain.ConsentStatus;
import com.sensedia.consentapi.dto.ConsentCreateRequest;
import com.sensedia.consentapi.dto.ConsentResponse;
import com.sensedia.consentapi.dto.ConsentUpdateRequest;
import com.sensedia.consentapi.exception.ResourceNotFoundException;
import com.sensedia.consentapi.mapper.ConsentMapper;
import com.sensedia.consentapi.repository.ConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private final ConsentRepository repository;
    private final ConsentMapper mapper;

    /**
     * Classe auxiliar para transportar o resultado da criação e o status (Created ou OK).
     */
    public static class CreationResult {
        private final ConsentResponse response;
        private final boolean isCreated;

        public CreationResult(ConsentResponse response, boolean isCreated) {
            this.response = response;
            this.isCreated = isCreated;
        }
        public ConsentResponse getResponse() { return response; }
        public boolean isCreated() { return isCreated; }
    }

    /**
     * REGRA DE IDEMPOTÊNCIA: Cria um consentimento ou retorna um existente caso a chave seja repetida.
     */
    public CreationResult createConsent(String idempotencyKey, ConsentCreateRequest request) {
        // 1. Verifica se já existe um registro com esta chave de idempotência
        Optional<Consent> existingConsent = repository.findByIdempotencyKey(idempotencyKey);

        if (existingConsent.isPresent()) {
            // Se já existe, retorna o recurso e avisa que NÃO foi criado um novo (Status 200)
            return new CreationResult(mapper.toResponse(existingConsent.get()), false);
        }

        // 2. Mapeia o DTO para a Entidade de Domínio
        Consent consent = mapper.toEntity(request);

        // 3. Garantia de Identidade e Auditoria Manual
        // Necessário pois o MongoDB não autogera UUID e a auditoria falha com IDs manuais
        if (consent.getId() == null) {
            consent.setId(UUID.randomUUID());
        }

        // Garante que a data de criação não venha nula no Swagger
        if (consent.getCreationDateTime() == null) {
            consent.setCreationDateTime(LocalDateTime.now());
        }

        consent.setIdempotencyKey(idempotencyKey);

        // 4. Persiste no MongoDB
        Consent savedConsent = repository.save(consent);

        // 5. Retorna o recurso salvo e confirma a criação (Status 201)
        return new CreationResult(mapper.toResponse(savedConsent), true);
    }

    /**
     * Busca todos os consentimentos com suporte a paginação.
     */
    public Page<ConsentResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    /**
     * Busca um consentimento específico pelo seu UUID.
     */
    public ConsentResponse findById(UUID id) {
        Consent consent = getConsentByIdOrThrow(id);
        return mapper.toResponse(consent);
    }

    /**
     * Atualiza dados de um consentimento existente.
     */
    public ConsentResponse update(UUID id, ConsentUpdateRequest request) {
        Consent consent = getConsentByIdOrThrow(id);
        mapper.updateEntityFromRequest(request, consent);
        Consent updatedConsent = repository.save(consent);
        return mapper.toResponse(updatedConsent);
    }

    /**
     * Revoga um consentimento alterando seu status.
     */
    public ConsentResponse revoke(UUID id) {
        Consent consent = getConsentByIdOrThrow(id);
        consent.setStatus(ConsentStatus.REVOKED);
        Consent savedConsent = repository.save(consent);
        return mapper.toResponse(savedConsent);
    }

    /**
     * Metodo auxiliar para centralizar a lógica de busca e erro 404.
     */
    private Consent getConsentByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consentimento não encontrado com o ID: " + id));
    }
}