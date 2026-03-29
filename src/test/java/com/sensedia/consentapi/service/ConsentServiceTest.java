package com.sensedia.consentapi.service;

import com.sensedia.consentapi.client.ViaCepClient;
import com.sensedia.consentapi.client.ViaCepResponse;
import com.sensedia.consentapi.domain.Consent;
import com.sensedia.consentapi.domain.ConsentHistory;
import com.sensedia.consentapi.domain.ConsentStatus;
import com.sensedia.consentapi.dto.ConsentCreateRequest;
import com.sensedia.consentapi.dto.ConsentResponse;
import com.sensedia.consentapi.exception.ResourceNotFoundException;
import com.sensedia.consentapi.mapper.ConsentMapper;
import com.sensedia.consentapi.repository.ConsentHistoryRepository;
import com.sensedia.consentapi.repository.ConsentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para validação das regras de negócio do ConsentService.
 */
@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

    @Mock
    private ConsentRepository repository;

    @Mock
    private ConsentMapper mapper;

    @Mock
    private ViaCepClient viaCepClient;

    @Mock
    private ConsentHistoryRepository historyRepository;

    @InjectMocks
    private ConsentService service;

    @Test
    @DisplayName("Deve retornar isCreated=false quando a chave de idempotência já existir (Regra 200 OK)")
    void deveRetornarExistenteQuandoChaveIdempotenciaExiste() {
        String key = "chave-repetida-123";
        Consent existingConsent = new Consent();
        existingConsent.setId(UUID.randomUUID());

        when(repository.findByIdempotencyKey(key)).thenReturn(Optional.of(existingConsent));
        when(mapper.toResponse(any())).thenReturn(new ConsentResponse());

        ConsentService.CreationResult result = service.createConsent(key, new ConsentCreateRequest());

        assertFalse(result.isCreated());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar isCreated=true e salvar no banco quando a chave for nova (Regra 201 Created)")
    void deveCriarNovoQuandoChaveNaoExiste() {
        String key = "chave-nova-456";
        ConsentCreateRequest request = new ConsentCreateRequest();
        Consent consent = new Consent();

        when(repository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(mapper.toEntity(any())).thenReturn(consent);
        when(repository.save(any())).thenReturn(consent);

        ConsentService.CreationResult result = service.createConsent(key, request);

        assertTrue(result.isCreated());
        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar por um ID que não existe")
    void deveLancarExcecaoQuandoIdNaoExistir() {
        UUID idFake = UUID.randomUUID();
        when(repository.findById(idFake)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(idFake));
    }

    @Test
    @DisplayName("Deve alterar o status para REVOKED ao chamar o método revoke e retornar a resposta")
    void deveRevogarConsentimentoComSucesso() {
        UUID id = UUID.randomUUID();
        Consent consent = new Consent();
        consent.setStatus(ConsentStatus.ACTIVE);

        ConsentResponse responseMock = new ConsentResponse();
        responseMock.setStatus(ConsentStatus.REVOKED);

        when(repository.findById(id)).thenReturn(Optional.of(consent));
        when(repository.save(any(Consent.class))).thenReturn(consent);
        when(mapper.toResponse(any(Consent.class))).thenReturn(responseMock);

        ConsentResponse result = service.revoke(id);

        assertEquals(ConsentStatus.REVOKED, consent.getStatus());
        assertEquals(ConsentStatus.REVOKED, result.getStatus());
        verify(repository, times(1)).save(consent);
    }

    @Test
    @DisplayName("Deve buscar dados de endereço no ViaCEP ao criar consentimento com CEP")
    void deveChamarViaCepAoCriarConsentimento() {
        String key = "chave-cep-123";
        ConsentCreateRequest request = new ConsentCreateRequest();
        request.setCep("01001000"); // CEP válido

        Consent consent = new Consent();
        consent.setId(UUID.randomUUID());

        ViaCepResponse viaCepResponse = new ViaCepResponse("01001-000", "Praça da Sé", "Sé", "São Paulo", "SP");

        when(repository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(mapper.toEntity(any())).thenReturn(consent);
        when(viaCepClient.getAddressByCep("01001000")).thenReturn(viaCepResponse);
        when(repository.save(any())).thenReturn(consent);

        service.createConsent(key, request);

        assertEquals("01001-000", consent.getCep());
        assertEquals("São Paulo", consent.getCidade());
        verify(viaCepClient, times(1)).getAddressByCep(anyString());
        verify(historyRepository, times(1)).save(any()); // Garante que salvou histórico
    }

    @Test
    @DisplayName("Deve retornar a lista de histórico de um consentimento existente")
    void deveRetornarHistoricoComSucesso() {
        UUID id = UUID.randomUUID();
        Consent consent = new Consent();
        consent.setId(id);

        ConsentHistory history = ConsentHistory.builder()
                .action("CREATE")
                .consentSnapshot(consent)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(consent));
        when(historyRepository.findByConsentIdOrderByTimestampDesc(id)).thenReturn(java.util.List.of(history));
        when(mapper.toResponse(any())).thenReturn(new ConsentResponse());

        var result = service.getHistory(id);

        assertFalse(result.isEmpty());
        assertEquals("CREATE", result.get(0).getAction());
        verify(historyRepository, times(1)).findByConsentIdOrderByTimestampDesc(id);
    }
}