# Users Service

Serviço de autenticação e gerenciamento de usuários. O namespace Java é
`br.com.ifrs.users`.

## Arquitetura

- `adapter/in/rest`: endpoints REST e conversão de respostas HTTP
- `application/port/in`: operações oferecidas aos adaptadores
- `application/port/out`: contratos para persistência e segurança
- `application/service`: casos de uso
- `domain/model`: modelo independente de Quarkus e JPA
- `adapter/out`: persistência Panache, hashing de senha e emissão JWT

## Desenvolvimento

Requer Java 25 e Maven. Na pasta `users`, execute:

```bash
JAVA_TOOL_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED" ./mvnw quarkus:dev
```

O serviço atende na porta 8082 (HTTPS na 8443, quando habilitado).
