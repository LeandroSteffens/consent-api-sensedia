package com.sensedia.consentapi.repository;

import com.sensedia.consentapi.domain.Consent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentRepository extends MongoRepository<Consent, UUID> {

    Optional<Consent> findByIdempotencyKey(String idempotencyKey);

}