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

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private final ConsentRepository repository;
    private final ConsentMapper mapper;

    // Classe auxiliar para o Controller saber se deve retornar 201 ou 200
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

    // REGRA DE IDEMPOTÊNCIA
    public CreationResult createConsent(String idempotencyKey, ConsentCreateRequest request) {
        Optional<Consent> existingConsent = repository.findByIdempotencyKey(idempotencyKey);

        if (existingConsent.isPresent()) {
            // Se a chave já existe, retorna o recurso e avisa que NÃO foi criado um novo
            return new CreationResult(mapper.toResponse(existingConsent.get()), false);
        }

        Consent consent = mapper.toEntity(request);
        consent.setIdempotencyKey(idempotencyKey);

        Consent savedConsent = repository.save(consent);

        // Retorna o recurso salvo e avisa que FOI criado
        return new CreationResult(mapper.toResponse(savedConsent), true);
    }

    // LISTAGEM COM PAGINAÇÃO
    public Page<ConsentResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    // BUSCA POR ID
    public ConsentResponse findById(UUID id) {
        Consent consent = getConsentByIdOrThrow(id);
        return mapper.toResponse(consent);
    }

    // ATUALIZAÇÃO
    public ConsentResponse update(UUID id, ConsentUpdateRequest request) {
        Consent consent = getConsentByIdOrThrow(id);
        mapper.updateEntityFromRequest(request, consent);
        Consent updatedConsent = repository.save(consent);
        return mapper.toResponse(updatedConsent);
    }

    // REVOGAÇÃO (Alterar status para REVOKED)
    public void revoke(UUID id) {
        Consent consent = getConsentByIdOrThrow(id);
        consent.setStatus(ConsentStatus.REVOKED);
        repository.save(consent);
    }

    // Metodo privado para evitar repetição de código nas buscas
    private Consent getConsentByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consentimento não encontrado com o ID: " + id));
    }
}