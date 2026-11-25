# Gastos Service

Serviço de despesas/receitas usando o namespace Java `br.com.ifrs.gastos`.

## Arquitetura

- `adapter/in/rest`: endpoints REST e autenticação da requisição
- `application/port/in`: operações oferecidas aos adaptadores
- `application/port/out`: contrato de persistência
- `application/service`: casos de uso e transações
- `domain/model`: modelos sem dependência de Quarkus ou JPA
- `adapter/out/persistence`: entidades e consultas Panache

## Desenvolvimento

Requer Java 25 e Maven. Na pasta `gastos`, execute:

```bash
JAVA_TOOL_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED" ./mvnw quarkus:dev
```

O serviço atende na porta 8081 (HTTPS na 8444, quando habilitado).
