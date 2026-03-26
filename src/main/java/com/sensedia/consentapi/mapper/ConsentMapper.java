package com.sensedia.consentapi.mapper;

import com.sensedia.consentapi.domain.Consent;
import com.sensedia.consentapi.dto.ConsentCreateRequest;
import com.sensedia.consentapi.dto.ConsentResponse;
import com.sensedia.consentapi.dto.ConsentUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// O componentModel = "spring" diz ao MapStruct para transformar essa interface
// em um Bean do Spring, permitindo que a gente use @Autowired ou injeção via construtor.
@Mapper(componentModel = "spring")
public interface ConsentMapper {

    // Converte o DTO de criação para a Entidade.
    // Ignoramos o ID, data de criação e chave de idempotência porque o sistema vai gerá-los.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDateTime", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    Consent toEntity(ConsentCreateRequest request);

    // Converte a Entidade salva no banco para o DTO de resposta que o usuário vai ver.
    ConsentResponse toResponse(Consent consent);

    // Atualiza uma entidade existente com os dados do DTO de atualização (usado no PUT).
    // O @MappingTarget avisa o MapStruct para não criar um objeto novo, mas sim
    // jogar os dados do 'request' para dentro do 'consent' que já existe.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cpf", ignore = true)
    @Mapping(target = "creationDateTime", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    void updateEntityFromRequest(ConsentUpdateRequest request, @MappingTarget Consent consent);
}