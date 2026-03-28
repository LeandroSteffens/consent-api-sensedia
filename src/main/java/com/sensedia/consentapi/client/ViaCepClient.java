package com.sensedia.consentapi.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Cliente para integração com a API pública do ViaCEP.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViaCepClient {

    private final WebClient webClient;

    /**
     * Busca os dados de endereço a partir de um CEP.
     * Implementa fallback retornando null em caso de falha para não quebrar o fluxo principal.
     */
    public ViaCepResponse getAddressByCep(String cep) {
        try {
            // Remove caracteres não numéricos
            String cleanCep = cep.replaceAll("\\D", "");

            return webClient.get()
                    .uri("https://viacep.com.br/ws/" + cleanCep + "/json/")
                    .retrieve()
                    .bodyToMono(ViaCepResponse.class)
                    .block();

        } catch (Exception e) {
            log.error("Falha ao consultar ViaCEP para o CEP {}. O consentimento será salvo sem endereço.", cep);
            return null;
        }
    }
}