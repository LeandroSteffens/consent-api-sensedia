package com.sensedia.consentapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensedia.consentapi.domain.Consent;
import com.sensedia.consentapi.domain.ConsentStatus;
import com.sensedia.consentapi.dto.ConsentCreateRequest;
import com.sensedia.consentapi.repository.ConsentHistoryRepository;
import com.sensedia.consentapi.repository.ConsentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração ponta a ponta utilizando Testcontainers para isolamento do banco de dados.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ConsentApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ConsentRepository repository;

	@Autowired
	private ConsentHistoryRepository historyRepository; // Injetamos o novo repositório aqui!

	@BeforeEach
	void setUp() {
		// Limpamos as duas tabelas antes de cada teste para um não interferir no outro
		repository.deleteAll();
		historyRepository.deleteAll();
	}

	/**
	 * Valida o ciclo de vida de criação e a integridade da regra de idempotência
	 * integrando com uma instância real de MongoDB.
	 */
	@Test
	@DisplayName("Fluxo Completo: Criar consentimento e testar idempotência com MongoDB Real")
	void deveCriarConsentimentoETestarIdempotenciaNaIntegracao() throws Exception {
		// Preparação do cenário
		ConsentCreateRequest request = new ConsentCreateRequest();
		request.setCpf("123.456.789-00");
		request.setStatus(ConsentStatus.ACTIVE);
		request.setExpirationDateTime(LocalDateTime.now().plusYears(1));
		request.setAdditionalInfo("Teste de Integração Sensedia");

		String jsonPayload = objectMapper.writeValueAsString(request);
		String idempotencyKey = "chave-integracao-123";

		// Execução e Validação: Primeira chamada (Criação)
		mockMvc.perform(post("/consents")
						.header("X-Idempotency-Key", idempotencyKey)
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.cpf").value("123.456.789-00"));

		assertEquals(1, repository.count());

		// Execução e Validação: Segunda chamada (Idempotência)
		mockMvc.perform(post("/consents")
						.header("X-Idempotency-Key", idempotencyKey)
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonPayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists());

		// Garante que nenhum registro duplicado foi inserido
		assertEquals(1, repository.count());
	}

	/**
	 * Valida se a exclusão lógica está funcionando e se o evento de auditoria
	 * foi corretamente salvo e exposto pela API de histórico.
	 */
	@Test
	@DisplayName("Fluxo Completo: Revogar consentimento e consultar histórico (Auditoria)")
	void deveRevogarEConsultarHistorico() throws Exception {
		// 1. Prepara e salva um consentimento direto no banco para o teste
		Consent consent = new Consent();
		consent.setId(UUID.randomUUID());
		consent.setCpf("111.222.333-44");
		consent.setStatus(ConsentStatus.ACTIVE);
		consent.setCreationDateTime(LocalDateTime.now());
		repository.save(consent);

		// 2. Chama a API para revogar (DELETE)
		mockMvc.perform(delete("/consents/" + consent.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REVOKED"));

		// 3. Garante que a revogação disparou o gatilho de histórico no banco
		assertEquals(1, historyRepository.count());

		// 4. Chama a API de histórico para ler a auditoria (GET /history)
		mockMvc.perform(get("/consents/" + consent.getId() + "/history"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].action").value("REVOKE")) // O item mais recente deve ser a revogação
				.andExpect(jsonPath("$[0].consentSnapshot.status").value("REVOKED"));
	}
}