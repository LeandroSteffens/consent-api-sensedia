package com.sensedia.consentapi.service;

import com.sensedia.consentapi.client.ViaCepClient;
import com.sensedia.consentapi.client.ViaCepResponse;
import com.sensedia.consentapi.domain.Consent;
import com.sensedia.consentapi.domain.ConsentHistory;
import com.sensedia.consentapi.domain.ConsentStatus;
import com.sensedia.consentapi.dto.ConsentCreateRequest;
import com.sensedia.consentapi.dto.ConsentResponse;
import com.sensedia.consentapi.dto.ConsentUpdateRequest;
import com.sensedia.consentapi.exception.ResourceNotFoundException;
import com.sensedia.consentapi.mapper.ConsentMapper;
import com.sensedia.consentapi.repository.ConsentHistoryRepository;
import com.sensedia.consentapi.repository.ConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestrador das regras de negócio para gestão de consentimentos.
 */
@Service
@RequiredArgsConstructor
public class ConsentService {

    private final ConsentRepository repository;
    private final ConsentMapper mapper;
    private final ViaCepClient viaCepClient;
    private final ConsentHistoryRepository historyRepository;

    /**
     * Wrapper para retorno de criação, diferenciando novos registros de retornos por idempotência.
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
     * Implementa a criação de consentimento com suporte a idempotência e enriquecimento de endereço.
     */
    public CreationResult createConsent(String idempotencyKey, ConsentCreateRequest request) {
        // Validação de idempotência
        Optional<Consent> existingConsent = repository.findByIdempotencyKey(idempotencyKey);
        if (existingConsent.isPresent()) {
            return new CreationResult(mapper.toResponse(existingConsent.get()), false);
        }

        Consent consent = mapper.toEntity(request);

        // Atribuição de ID e auditoria manual para conformidade com MongoDB
        if (consent.getId() == null) consent.setId(UUID.randomUUID());
        if (consent.getCreationDateTime() == null) consent.setCreationDateTime(LocalDateTime.now());

        consent.setIdempotencyKey(idempotencyKey);

        // Enriquecimento de dados via integração externa
        if (request.getCep() != null && !request.getCep().isBlank()) {
            ViaCepResponse addressInfo = viaCepClient.getAddressByCep(request.getCep());

            if (addressInfo != null && addressInfo.cep() != null) {
                consent.setCep(addressInfo.cep());
                consent.setLogradouro(addressInfo.logradouro());
                consent.setBairro(addressInfo.bairro());
                consent.setCidade(addressInfo.localidade());
                consent.setUf(addressInfo.uf());
            }
        }

        Consent savedConsent = repository.save(consent);
        registerHistory(savedConsent, "CREATE");
        return new CreationResult(mapper.toResponse(savedConsent), true);
    }

    /**
     * Retorna listagem paginada de consentimentos.
     */
    public Page<ConsentResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    /**
     * Recupera um consentimento por ID.
     */
    public ConsentResponse findById(UUID id) {
        return mapper.toResponse(getConsentByIdOrThrow(id));
    }

    /**
     * Atualiza dados mutáveis de um registro existente.
     */
    public ConsentResponse update(UUID id, ConsentUpdateRequest request) {
        Consent consent = getConsentByIdOrThrow(id);
        mapper.updateEntityFromRequest(request, consent);

        Consent updatedConsent = repository.save(consent);
        registerHistory(updatedConsent, "UPDATE");

        return mapper.toResponse(updatedConsent);
    }

    /**
     * Executa a revogação lógica do consentimento.
     */
    public ConsentResponse revoke(UUID id) {
        Consent consent = getConsentByIdOrThrow(id);
        consent.setStatus(ConsentStatus.REVOKED);

        Consent savedConsent = repository.save(consent);
        registerHistory(savedConsent, "REVOKE");

        return mapper.toResponse(savedConsent);
    }

    /**
     * Busca entidade no repositório ou lança exceção de recurso não encontrado.
     */
    private Consent getConsentByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consentimento não encontrado com o ID: " + id));
    }

    /**
     * Salva uma cópia exata do estado atual do consentimento para fins de auditoria.
     */
    private void registerHistory(Consent consent, String action) {
        ConsentHistory history = ConsentHistory.builder()
                .id(UUID.randomUUID())
                .consentId(consent.getId())
                .action(action)
                .consentSnapshot(consent)
                .timestamp(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }
}