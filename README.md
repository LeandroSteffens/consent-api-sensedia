# API de Consentimentos - Open Insurance Brasil

Este projeto é uma API REST para gestão de consentimentos de usuários no ecossistema de Open Insurance, desenvolvida como parte do desafio técnico para a posição de Software Developer Júnior na Sensedia.

## Tecnologias Utilizadas

- Java 21
- Spring Boot 3.3.4 (Web, Validation, Data MongoDB)
- Spring WebFlux (WebClient para consumo de API externa)
- MongoDB
- Lombok
- MapStruct
- OpenAPI / Swagger
- JUnit 5 & Mockito
- Testcontainers
- Docker & Docker Compose

## Funcionalidades e Regras de Negócio

- Idempotência: O endpoint de criação (POST /consents) exige o cabeçalho X-Idempotency-Key. Requisições subsequentes com a mesma chave não geram duplicidade no banco de dados e retornam status 200 OK com o recurso originalmente criado.
- Integração e Enriquecimento: Utilização de WebClient para consultar a API externa ViaCEP. Caso um CEP válido seja enviado, os dados de endereço são populados automaticamente no registro de consentimento.
- Exclusão Lógica: A revogação de um consentimento (DELETE /consents/{id}) altera o status do registro para REVOKED, mantendo o histórico na base de dados e retornando o objeto atualizado.
- Paginação: A listagem de consentimentos (GET /consents) implementa paginação nativa através da interface Pageable do Spring Data.
- Tratamento Global de Exceções: Utilização de @RestControllerAdvice para interceptar erros de validação (Bean Validation) e recursos não encontrados, padronizando o formato da resposta de erro.
- Proteção de Dados Sensíveis: O endpoint de atualização (PUT /consents/{id}) utiliza DTOs e MapStruct configurados para ignorar campos imutáveis, como ID, CPF e Data de Criação.
- Trilha de Auditoria: Implementação de um fluxo de histórico que salva automaticamente uma "foto" (snapshot) do consentimento em uma coleção separada (`consent_history`) sempre que ocorre uma mutação (CREATE, UPDATE ou REVOKE). Esse histórico de vida do dado pode ser consultado via endpoint específico (GET /consents/{id}/history).

## Pré-requisitos

Para executar este projeto, é necessário ter instalado:
- Java 21
- Docker e Docker Compose

## Como Executar a Aplicação

Passo 1: Suba a infraestrutura do banco de dados (MongoDB) utilizando o Docker Compose:
> docker-compose up -d

Passo 2: Na raiz do projeto, inicie a aplicação utilizando o Maven Wrapper:
> ./mvnw spring-boot:run

Passo 3: Acesse a documentação interativa da API (Swagger UI) pelo navegador:
> http://localhost:8080/swagger-ui.html

## Como Executar os Testes

O projeto possui uma suíte de testes unitários e de integração. Os testes de integração utilizam a biblioteca Testcontainers para provisionar contêineres efêmeros do MongoDB, garantindo isolamento completo da base de dados.

Para rodar a suíte completa de testes, execute na raiz do projeto:
> ./mvnw clean test

## Solução de Problemas Comuns

Caso encontre problemas ao rodar o projeto ou os testes, verifique as soluções abaixo:

1. Erro nos testes: "Could not find a valid Docker environment" ou "BadRequestException (Status 400)"
   Causa: Incompatibilidade de comunicação entre o Testcontainers e versões recentes do Docker Desktop (v29+), ou o Docker não está em execução.
   Solução: Certifique-se de que o Docker Desktop está aberto e rodando. O projeto já está configurado no pom.xml com a versão 1.21.4 do testcontainers, que resolve nativamente o conflito com a nova API do Docker.

2. Erro: "Port 8080 was already in use" ou "Port 27017 was already in use"
   Causa: Outro serviço ou aplicação na sua máquina já está ocupando a porta da API ou do banco de dados.
   Solução: Para o banco de dados, pare outros containers do MongoDB que possam estar rodando. Para a API, encerre o processo que está usando a porta 8080 ou altere a porta da aplicação adicionando server.port=8081 no arquivo application.properties.

3. Erro de compilação: "cannot find symbol" (Getters/Setters não encontrados)
   Causa: A IDE ou o compilador não processou as anotações do Lombok corretamente.
   Solução: Execute uma limpeza completa com o comando ./mvnw clean install. Se estiver utilizando IntelliJ IDEA, certifique-se de que a opção "Enable Annotation Processing" está ativada nas configurações do projeto.