package com.sensedia.consentapi.service;

import com.sensedia.consentapi.domain.Consent;
import com.sensedia.consentapi.domain.ConsentStatus;
import com.sensedia.consentapi.dto.ConsentCreateRequest;
import com.sensedia.consentapi.dto.ConsentResponse;
import com.sensedia.consentapi.exception.ResourceNotFoundException;
import com.sensedia.consentapi.mapper.ConsentMapper;
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

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

    // Cria mocks das dependências para não batermos no banco de verdade
    @Mock
    private ConsentRepository repository;

    @Mock
    private ConsentMapper mapper;

    // Injeta os mocks dentro do Service que vamos testar
    @InjectMocks
    private ConsentService service;

    @Test
    @DisplayName("Deve retornar isCreated=false quando a chave de idempotência já existir (Regra 200 OK)")
    void deveRetornarExistenteQuandoChaveIdempotenciaExiste() {
        // Arrange (Preparação)
        String key = "chave-repetida-123";
        Consent existingConsent = new Consent();
        existingConsent.setId(UUID.randomUUID());

        // Ensinamos o Mockito a simular o comportamento do banco
        when(repository.findByIdempotencyKey(key)).thenReturn(Optional.of(existingConsent));
        when(mapper.toResponse(any())).thenReturn(new ConsentResponse());

        // Act (Ação)
        ConsentService.CreationResult result = service.createConsent(key, new ConsentCreateRequest());

        // Assert (Verificação)
        assertFalse(result.isCreated()); // Garante que a flag de criação é falsa
        verify(repository, never()).save(any()); // Garante que o metodo save nunca foi chamado
    }

    @Test
    @DisplayName("Deve retornar isCreated=true e salvar no banco quando a chave for nova (Regra 201 Created)")
    void deveCriarNovoQuandoChaveNaoExiste() {
        // Arrange
        String key = "chave-nova-456";
        ConsentCreateRequest request = new ConsentCreateRequest();
        Consent consent = new Consent();

        when(repository.findByIdempotencyKey(key)).thenReturn(Optional.empty()); // Banco vazio para essa chave
        when(mapper.toEntity(any())).thenReturn(consent);
        when(repository.save(any())).thenReturn(consent);

        // Act
        ConsentService.CreationResult result = service.createConsent(key, request);

        // Assert
        assertTrue(result.isCreated()); // Garante que a flag de criação é verdadeira
        verify(repository, times(1)).save(any()); // Garante que o metodo save foi chamado exatamente uma vez
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar por um ID que não existe")
    void deveLancarExcecaoQuandoIdNaoExistir() {
        // Arrange
        UUID idFake = UUID.randomUUID();
        when(repository.findById(idFake)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.findById(idFake));
    }

    @Test
    @DisplayName("Deve alterar o status para REVOKED ao chamar o método revoke e retornar a resposta")
    void deveRevogarConsentimentoComSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Consent consent = new Consent();
        consent.setStatus(ConsentStatus.ACTIVE); // Nasce como ativo

        ConsentResponse responseMock = new ConsentResponse();
        responseMock.setStatus(ConsentStatus.REVOKED);

        when(repository.findById(id)).thenReturn(Optional.of(consent));
        when(repository.save(any(Consent.class))).thenReturn(consent);
        when(mapper.toResponse(any(Consent.class))).thenReturn(responseMock);

        // Act
        ConsentResponse result = service.revoke(id);

        // Assert
        assertEquals(ConsentStatus.REVOKED, consent.getStatus()); // Garante a mudança na Entidade
        assertEquals(ConsentStatus.REVOKED, result.getStatus()); // Garante que o Response retornou o status certo
        verify(repository, times(1)).save(consent); // Garante que a alteração foi persistida
    }
}