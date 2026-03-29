package com.sensedia.consentapi.mapper;

import com.sensedia.consentapi.domain.Consent;
import com.sensedia.consentapi.dto.ConsentCreateRequest;
import com.sensedia.consentapi.dto.ConsentResponse;
import com.sensedia.consentapi.dto.ConsentUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ConsentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDateTime", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    Consent toEntity(ConsentCreateRequest request);

    ConsentResponse toResponse(Consent consent);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cpf", ignore = true)
    @Mapping(target = "creationDateTime", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    void updateEntityFromRequest(ConsentUpdateRequest request, @MappingTarget Consent consent);
}