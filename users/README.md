# Users Service - Microserviço de Usuários

Microserviço responsável por autenticação e gerenciamento de usuários.

## 🎯 Funcionalidades

- Criação de usuários com senha criptografada (BCrypt)
- Login com geração de token JWT
- Atualização de dados do usuário
- Exclusão de conta
- Proteção de senha em respostas JSON (`@JsonIgnore`)

## 🚀 Executar em Desenvolvimento

```shell script
./mvnw quarkus:dev -Dquarkus.http.port=8082
```

O serviço estará disponível em: <http://localhost:8082>

Dev UI disponível em: <http://localhost:8082/q/dev/>

## 📡 Endpoints

**IMPORTANTE**: Todos os parâmetros são enviados via **HTTP Headers** (não JSON body)

### Criar Usuário
```bash
curl -X POST 'http://localhost:8082/create' \
  -H 'name: João Silva' \
  -H 'email: joao@example.com' \
  -H 'password: senha123'
```

### Login (retorna JWT)
```bash
TOKEN=$(curl -s -X POST 'http://localhost:8082/login' \
  -H 'email: joao@example.com' \
  -H 'password: senha123')
```

### Atualizar Usuário (requer autenticação)
```bash
curl -X PATCH 'http://localhost:8082/updateUser' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'name: João Silva Atualizado' \
  -H 'email: joao.novo@example.com' \
  -H 'password: novaSenha456'
```

### Deletar Usuário (requer autenticação)
```bash
curl -X DELETE 'http://localhost:8082/delete' \
  -H "Authorization: Bearer $TOKEN"
```

## 🔐 Segurança

- **Password Hashing**: BCrypt com salt automático
- **JWT**: SmallRye JWT com RS256
- **@JsonIgnore**: Senha nunca retornada em respostas
- **@RolesAllowed**: Proteção de endpoints por role

## 🗄️ Banco de Dados

- MySQL 8.0
- Hibernate Reactive com Panache
- Tabela: `users`

### Configuração MySQL
```properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=pw2
quarkus.datasource.password=pw2
quarkus.datasource.reactive.url=mysql://localhost:3306/pw2
```

## 📦 Empacotar e Executar

Gerar o JAR:
```shell script
./mvnw package
```

Executar:
```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

## 🐳 Docker

Build da imagem:
```shell script
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t users-service .
```

Executar container:
```shell script
docker run -i --rm -p 8082:8082 users-service
```

## 🧪 Health Check

```bash
curl http://localhost:8082/q/health
```

## 📚 Tecnologias

- Quarkus 3.29.4
- Java 21
- MySQL 8.0 (Reactive)
- Hibernate Reactive Panache
- SmallRye JWT
- BCrypt

---

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/code-with-quarkus-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code for Hibernate ORM via the active record or the repository pattern
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

## Provided Code

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)

[Related Hibernate with Panache section...](https://quarkus.io/guides/hibernate-orm-panache)


### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
