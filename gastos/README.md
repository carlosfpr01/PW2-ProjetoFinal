# Gastos Service - Microserviço de Despesas e Receitas

Microserviço responsável pelo gerenciamento de despesas e receitas dos usuários.

## 🎯 Funcionalidades

- Criar despesas e receitas
- Listar despesas por período
- Obter sumário financeiro (total despesas, receitas e saldo)
- Sumário por tag/categoria
- Atualizar e deletar despesas
- Autenticação via JWT

## 🚀 Executar em Desenvolvimento

```shell script
./mvnw quarkus:dev -Dquarkus.http.port=8081
```

O serviço estará disponível em: <http://localhost:8081>

Dev UI disponível em: <http://localhost:8081/q/dev/>

## 📡 Endpoints

**IMPORTANTE**: 
- Todos os parâmetros de dados são enviados via **Query Params** (não JSON body)
- Autenticação via Header `Authorization: Bearer <TOKEN>`
- Todas as rotas requerem autenticação JWT

### Criar Despesa/Receita
```bash
curl -X POST 'http://localhost:8081/despesa/create?amount=150.00&operation=D&tag=Alimentação&date=2025-11-29' \
  -H "Authorization: Bearer $TOKEN"
```

**Parâmetros:**
- `amount`: Valor (number)
- `operation`: "D" para Despesa, "C" para Crédito/Receita
- `tag`: Categoria (string)
- `date`: Data no formato YYYY-MM-DD

### Listar Despesas
```bash
curl -X GET 'http://localhost:8081/despesa/listDespesas?startDate=2025-11-01&endDate=2025-11-30' \
  -H "Authorization: Bearer $TOKEN"
```

### Obter Sumário Financeiro
```bash
curl -X GET 'http://localhost:8081/despesa/sumario?startDate=2025-11-01&endDate=2025-11-30' \
  -H "Authorization: Bearer $TOKEN"
```

**Retorna:**
```json
{
  "totalDespesas": 1500.00,
  "totalReceitas": 5000.00,
  "saldo": 3500.00
}
```

### Sumário por Tag
```bash
curl -X GET 'http://localhost:8081/despesa/sumarioTag?tag=Alimentação&startDate=2025-11-01&endDate=2025-11-30' \
  -H "Authorization: Bearer $TOKEN"
```

### Listar Sumário de Todas as Tags
```bash
curl -X GET 'http://localhost:8081/despesa/listTagSum?startDate=2025-11-01&endDate=2025-11-30' \
  -H "Authorization: Bearer $TOKEN"
```

### Atualizar Despesa
```bash
curl -X PATCH 'http://localhost:8081/despesa/update?id=1&amount=200.00&tag=Alimentação Premium' \
  -H "Authorization: Bearer $TOKEN"
```

### Deletar Despesa
```bash
curl -X DELETE 'http://localhost:8081/despesa/delete?id=1' \
  -H "Authorization: Bearer $TOKEN"
```

## 🔐 Segurança

- **JWT**: Extração automática do userId do token
- **@RolesAllowed("user")**: Proteção de endpoints
- **SecurityIdentity**: Injeção de contexto de segurança

## 🗄️ Banco de Dados

- MySQL 8.0
- Hibernate Reactive com Panache
- Tabela: `despesas`

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
docker build -f src/main/docker/Dockerfile.jvm -t gastos-service .
```

Executar container:
```shell script
docker run -i --rm -p 8081:8081 gastos-service
```

## 🧪 Health Check

```bash
curl http://localhost:8081/q/health
```

## 📚 Tecnologias

- Quarkus 3.29.4
- Java 21
- MySQL 8.0 (Reactive)
- Hibernate Reactive Panache
- SmallRye JWT
- HQL (Hibernate Query Language)

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
