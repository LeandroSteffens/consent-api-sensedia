package com.sensedia.consentapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensedia.consentapi.domain.ConsentStatus;
import com.sensedia.consentapi.dto.ConsentCreateRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

	@BeforeEach
	void setUp() {
		repository.deleteAll();
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
}