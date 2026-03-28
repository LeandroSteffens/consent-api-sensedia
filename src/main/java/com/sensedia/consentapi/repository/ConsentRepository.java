package com.sensedia.consentapi.repository;

import com.sensedia.consentapi.domain.Consent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface de acesso a dados para a coleção de consentimentos no MongoDB.
 */
@Repository
public interface ConsentRepository extends MongoRepository<Consent, UUID> {

    /**
     * Busca um consentimento através de sua chave de idempotência única.
     */
    Optional<Consent> findByIdempotencyKey(String idempotencyKey);

}