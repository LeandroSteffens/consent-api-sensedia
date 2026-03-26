package com.sensedia.consentapi.mapper;

import com.sensedia.consentapi.domain.Consent;
import com.sensedia.consentapi.dto.ConsentCreateRequest;
import com.sensedia.consentapi.dto.ConsentResponse;
import com.sensedia.consentapi.dto.ConsentUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Interface de mapeamento entre as Entidades de Domínio e os DTOs.
 * O componentModel = "spring" permite que esta classe seja injetada como um Bean do Spring.
 */
@Mapper(componentModel = "spring")
public interface ConsentMapper {

    /**
     * Converte o DTO de criação para a Entidade de domínio.
     * Ignoramos campos que não devem ser definidos pelo usuário no momento da criação.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDateTime", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    Consent toEntity(ConsentCreateRequest request);

    /**
     * Converte a Entidade de domínio para o DTO de resposta.
     * Este é o objeto que o Swagger e o cliente da API receberão.
     */
    ConsentResponse toResponse(Consent consent);

    /**
     * Atualiza uma entidade existente com base nos dados de um DTO de atualização.
     * O uso de @MappingTarget garante que a entidade original seja modificada em vez de criar uma nova.
     * CPF, ID e Data de Criação são imutáveis após a criação do consentimento.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cpf", ignore = true)
    @Mapping(target = "creationDateTime", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    void updateEntityFromRequest(ConsentUpdateRequest request, @MappingTarget Consent consent);
}