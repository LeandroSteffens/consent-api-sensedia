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

@Import(TestcontainersConfiguration.class) // Chama o MongoDB via Docker
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Sobe a aplicação inteira
@AutoConfigureMockMvc // Ferramenta para simular requisições HTTP (como o Swagger)
class ConsentApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ConsentRepository repository;

	@BeforeEach
	void setUp() {
		// Limpa o banco de dados do Testcontainer antes de cada teste para não ter sujeira
		repository.deleteAll();
	}

	@Test
	@DisplayName("Fluxo Completo: Criar consentimento e testar idempotência com MongoDB Real")
	void deveCriarConsentimentoETestarIdempotenciaNaIntegracao() throws Exception {
		// 1. Preparação dos dados (JSON)
		ConsentCreateRequest request = new ConsentCreateRequest();
		request.setCpf("123.456.789-00");
		request.setStatus(ConsentStatus.ACTIVE);
		request.setExpirationDateTime(LocalDateTime.now().plusYears(1));
		request.setAdditionalInfo("Teste de Integração Sensedia");

		// Transforma o Objeto Java em JSON
		String jsonPayload = objectMapper.writeValueAsString(request);
		String idempotencyKey = "chave-integracao-123";

		// 2. PRIMEIRA CHAMADA (Criação)
		mockMvc.perform(post("/consents")
						.header("X-Idempotency-Key", idempotencyKey)
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonPayload))
				.andExpect(status().isCreated()) // Exige HTTP 201
				.andExpect(jsonPath("$.id").exists()) // Exige que o UUID tenha sido gerado no JSON
				.andExpect(jsonPath("$.cpf").value("123.456.789-00"));

		// Vai no banco de dados MongoDB real e verifica se salvou 1 registro
		assertEquals(1, repository.count());

		// 3. SEGUNDA CHAMADA (Idempotência)
		mockMvc.perform(post("/consents")
						.header("X-Idempotency-Key", idempotencyKey)
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonPayload))
				.andExpect(status().isOk()) // Exige HTTP 200
				.andExpect(jsonPath("$.id").exists());

		// Vai no banco de dados e garante que continua com apenas 1 registro
		assertEquals(1, repository.count());
	}
}