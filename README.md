# Consent API - Open Insurance Brasil (Desafio Sensedia)

Este projeto é uma API REST para gestão de consentimentos de usuários no ecossistema de Open Insurance, desenvolvida como parte do desafio técnico para a posição de Software Developer Júnior na Sensedia. 

A API garante um modelo de dados limpo, endpoints bem definidos, código testável e aplica o pilar de segurança e privacidade exigido pelo contexto de OPIN (Open Insurance), incluindo o tratamento rigoroso de idempotência.

## 📋 Checklist de Requisitos do Desafio

### 1. Requisitos Funcionais (Endpoints)
- [ ] `POST /consents`: Criar um novo consentimento.
- [ ] `GET /consents`: Listar todos os consentimentos (com paginação).
- [ ] `GET /consents/{id}`: Buscar um consentimento específico por ID.
- [ ] `PUT /consents/{id}`: Atualizar informações (ex: estender data de expiração).
- [ ] `DELETE /consents/{id}`: Revogar um consentimento (Alterar status para `REVOKED` ou exclusão lógica).

### 2. Regra de Negócio: Idempotência (Obrigatório)
- [ ] Receber e processar o header personalizado `X-Idempotency-Key` no endpoint `POST /consents`.
- [ ] Garantir que chamadas com a mesma chave não criem registros duplicados no banco.
- [ ] Em caso de reenvio com a mesma chave, retornar HTTP Status `200 OK` com o corpo do recurso criado na primeira tentativa (em vez de `201 Created`).

### 3. Modelo de Dados e Validações
- [x] `id`: UUID (Gerado pelo sistema).
- [x] `cpf`: String com validação de formato (`###.###.###-##`).
- [x] `status`: Enum (`ACTIVE`, `REVOKED`, `EXPIRED`).
- [x] `creationDateTime`: LocalDateTime (Gerado automaticamente).
- [x] `expirationDateTime`: LocalDateTime (Opcional).
- [x] `additionalInfo`: String (Opcional, tamanho máximo 50, mínimo 1).
- [x] Uso de Bean Validation (`@Valid`, `@NotNull`, `@Pattern`, `@Size`, etc.).

### 4. Requisitos Técnicos e Arquitetura
- [x] Linguagem: Java 21 ou superior.
- [x] Framework: Spring Boot.
- [x] Gerenciador de dependências: Maven.
- [x] Persistência: MongoDB (Preferencial) ou Relacional (H2/PostgreSQL).
- [x] Mapeamento de objetos: Uso de DTOs e MapStruct (ou similar).
- [ ] Tratamento de Erros: Uso de `@ControllerAdvice` para retornar erros estruturados (ex: 400 Bad Request para CPF inválido).
- [ ] Estrutura do projeto organizada em camadas (`domain`, `dto`, `service`, `repository`).

### 5. Testes e Qualidade
- [ ] Testes unitários com JUnit 5 e Mockito.
- [ ] Testes de integração utilizando Testcontainers (para o banco de dados escolhido).
- [ ] Cobertura adequada e clareza nos testes.

### 6. Boas Práticas e Entrega
- [ ] Documentação da API com Swagger (OpenAPI).
- [ ] Histórico do Git utilizando Commits Semânticos (ex: `feat: ...`, `fix: ...`, `test: ...`).
- [ ] Código em repositório público (ex: `consent-api`).
- [ ] README contendo instruções claras de como compilar e executar a aplicação.

### 7. Diferenciais (Bônus)
- [ ] Integração com WebClient para chamadas externas (se aplicável na modelagem).
- [ ] Histórico de alterações (Changelog).
- [ ] Arquivo `Dockerfile` configurado.
- [ ] Arquivo `docker-compose.yml` para subir a aplicação e dependências (banco de dados) com facilidade.
