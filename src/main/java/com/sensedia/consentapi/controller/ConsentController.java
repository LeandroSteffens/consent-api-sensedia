package com.sensedia.consentapi.controller;

import com.sensedia.consentapi.dto.ConsentCreateRequest;
import com.sensedia.consentapi.dto.ConsentResponse;
import com.sensedia.consentapi.dto.ConsentUpdateRequest;
import com.sensedia.consentapi.service.ConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/consents")
@RequiredArgsConstructor
@Tag(name = "Consentimentos", description = "API para gestão de consentimentos de usuários (Open Insurance)")
public class ConsentController {

    private final ConsentService service;

    @Operation(summary = "Criar um novo consentimento", description = "Endpoint com suporte a idempotência")
    @PostMapping
    public ResponseEntity<ConsentResponse> createConsent(
            @Parameter(description = "Chave de Idempotência", required = true)
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ConsentCreateRequest request) {

        ConsentService.CreationResult result = service.createConsent(idempotencyKey, request);

        if (result.isCreated()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.getResponse()); // 201 Created
        } else {
            return ResponseEntity.ok(result.getResponse()); // 200 OK (Retry da mesma chave)
        }
    }

    @Operation(summary = "Listar todos os consentimentos", description = "Retorna uma lista paginada")
    @GetMapping
    public ResponseEntity<Page<ConsentResponse>> getAllConsents(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<ConsentResponse> responses = service.findAll(pageable);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Buscar consentimento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ConsentResponse> getConsentById(@PathVariable UUID id) {
        ConsentResponse response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar informações do consentimento")
    @PutMapping("/{id}")
    public ResponseEntity<ConsentResponse> updateConsent(
            @PathVariable UUID id,
            @Valid @RequestBody ConsentUpdateRequest request) {

        ConsentResponse response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Revogar um consentimento", description = "Altera o status do consentimento para REVOKED e retorna o objeto atualizado")
    @DeleteMapping("/{id}")
    public ResponseEntity<ConsentResponse> revokeConsent(@PathVariable UUID id) {
        ConsentResponse response = service.revoke(id);
        return ResponseEntity.ok(response);
    }
}