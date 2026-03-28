package com.sensedia.consentapi.mapper;

import com.sensedia.consentapi.domain.Consent;
import com.sensedia.consentapi.dto.ConsentCreateRequest;
import com.sensedia.consentapi.dto.ConsentResponse;
import com.sensedia.consentapi.dto.ConsentUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Responsável pelas conversões entre a entidade de domínio e os DTOs.
 */
@Mapper(componentModel = "spring")
public interface ConsentMapper {

    /**
     * Mapeia dados de criação para a entidade, ignorando campos de controle interno.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDateTime", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    Consent toEntity(ConsentCreateRequest request);

    /**
     * Converte a entidade de domínio para o DTO de resposta.
     */
    ConsentResponse toResponse(Consent consent);

    /**
     * Atualiza a entidade existente mantendo a integridade de campos imutáveis (ID, CPF, Datas).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cpf", ignore = true)
    @Mapping(target = "creationDateTime", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    void updateEntityFromRequest(ConsentUpdateRequest request, @MappingTarget Consent consent);
}