package com.sensedia.consentapi.client;

public record ViaCepResponse(
        String cep,
        String logradouro,
        String bairro,
        String localidade, // O ViaCEP chama cidade de "localidade"
        String uf
) {}