# Microserviços - Users e Gastos com API Gateway

Sistema de microserviços para gerenciamento de usuários e controle de gastos.

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────┐
│        Client (curl/postman/browser)    │
└────────────────┬────────────────────────┘
                 │
                 ▼ HTTP Requests
        ┌────────────────────┐
        │  API GATEWAY       │
        │  (porta 8080)      │
        │                    │
        │  • Roteamento      │
        │  • Validação JWT   │
        │  • CORS            │
        └─┬──────────────┬───┘
          │              │
          │ REST         │ REST
          │ Client       │ Client
          ▼              ▼
    ┌─────────────┐  ┌──────────────┐
    │ USERS       │  │ GASTOS       │
    │ (porta 8082)│  │ (porta 8081) │
    │             │  │              │
    │ • Login     │  │ • Despesas   │
    │ • Registro  │  │ • Receitas   │
    │ • JWT Gen   │  │ • Relatórios │
    │ • BCrypt    │  │ • Sumários   │
    └──────┬──────┘  └──────┬───────┘
           │                │
           │ Reactive       │ Reactive
           │ Panache        │ Panache
           ▼                ▼
    ┌──────────────┐ ┌──────────────┐
    │  MySQL 8.0   │ │  MySQL 8.0   │
    │ (porta 3308) │ │ (porta 3307) │
    │              │ │              │
    │ DB: users_db │ │ DB: gastos_db│
    │ Table: users │ │ Table:       │
    │              │ │  despesas    │
    └──────────────┘ └──────────────┘
```

## 📦 Componentes

- **API Gateway** (porta 8080): 
  - Ponto único de entrada para todas as requisições
  - Roteamento inteligente para microserviços
  - Validação de tokens JWT
  - Configuração CORS
  - MicroProfile REST Client para comunicação inter-serviços

- **Users Service** (porta 8082):
  - Autenticação com JWT (RS256)
  - Registro de novos usuários
  - Criptografia de senhas com BCrypt
  - CRUD de usuários
  - Campo `password` protegido com `@JsonIgnore`
  - Parâmetros recebidos via **HTTP Headers**

- **Gastos Service** (porta 8081):
  - CRUD de despesas e receitas
  - Relatórios financeiros por período
  - Sumários por tag/categoria
  - Extração de userId do JWT
  - Proteção de rotas com `@RolesAllowed("user")`
  - Parâmetros recebidos via **Query Params**
  - Banco de dados próprio (MySQL porta 3307)

- **Bancos de Dados MySQL 8.0** (isolamento por microserviço):
  - **Users DB** (porta 3308 em dev): Tabela `users`
  - **Gastos DB** (porta 3307 em dev): Tabela `despesas`
  - Acesso reativo via Hibernate Reactive Panache
  - Cada microserviço gerencia seu próprio schema
  - **Dev Services**: Quarkus cria containers MySQL automaticamente em modo dev

## 🚀 Início Rápido

### Opção 1: Script Automatizado (Recomendado)

```bash
# Dê permissão aos scripts (apenas primeira vez)
chmod +x start.sh stop.sh

# Inicia todos os serviços (MySQL + Users + Gastos + Gateway)
./start.sh

# Para parar todos os serviços
./stop.sh
```

### Opção 2: Docker Compose

```bash
# Inicia todos os serviços
docker-compose up --build

# Acesse:
# - API Gateway: http://localhost:8080
# - Jaeger UI: http://localhost:16686
# - Graylog: http://localhost:9000
```

### Opção 3: Manual (para debug)

Ou manualmente:

```bash
# Terminal 1 - MySQL
docker run -d --name mysql-db -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=pw2 -e MYSQL_USER=pw2 -e MYSQL_PASSWORD=pw2 -p 3306:3306 mysql:8.0

# Terminal 2 - Users Service
cd users && ./mvnw quarkus:dev -Dquarkus.http.port=8082

# Terminal 3 - Gastos Service
cd gastos && ./mvnw quarkus:dev -Dquarkus.http.port=8081

# Terminal 4 - API Gateway
cd gateway && ./mvnw quarkus:dev
```

## 📡 Endpoints

**Todas as requisições devem ser feitas via Gateway na porta 8080:**

### Autenticação

```bash
# Criar usuário (parâmetros via Headers)
curl -X POST 'http://localhost:8080/api/users/create' \
  -H 'name: João Silva' \
  -H 'email: joao@example.com' \
  -H 'password: senha123'

# Login (retorna JWT token)
TOKEN=$(curl -s -X POST 'http://localhost:8080/api/users/login' \
  -H 'email: joao@example.com' \
  -H 'password: senha123')

echo "Token: $TOKEN"
```

### Gerenciar Despesas (requer token)

```bash
# Criar despesa (parâmetros via Query Params)
curl -X POST 'http://localhost:8080/api/gastos/despesa/create?amount=150.50&operation=D&tag=Alimentação&date=2025-11-28' \
  -H "Authorization: Bearer $TOKEN"

# Listar despesas
curl -X GET 'http://localhost:8080/api/gastos/despesa/listDespesas?startDate=2025-01-01&endDate=2025-12-31' \
  -H "Authorization: Bearer $TOKEN"

# Resumo de gastos
curl -X GET 'http://localhost:8080/api/gastos/despesa/sumario?startDate=2025-01-01&endDate=2025-12-31' \
  -H "Authorization: Bearer $TOKEN"

# Sumário por tag
curl -X GET 'http://localhost:8080/api/gastos/despesa/sumarioTag?tag=Alimentação&startDate=2025-01-01&endDate=2025-12-31' \
  -H "Authorization: Bearer $TOKEN"
```

## 📝 Formato das Requisições

### Users Service (via Gateway)
- **Parâmetros**: Sempre via **HTTP Headers**
- **Exemplo**: `-H 'name: João' -H 'email: joao@example.com' -H 'password: senha123'`
- **❌ NÃO use**: JSON body (`-d '{...}'`)

### Gastos Service (via Gateway)
- **Parâmetros de dados**: Via **Query Params** (`?amount=100&operation=D&tag=Alimentação&date=2025-11-28`)
- **Autenticação**: Via Header `Authorization: Bearer <TOKEN>`
- **❌ NÃO use**: JSON body para os parâmetros de negócio

### Respostas
- O campo **password** nunca é retornado (protegido com `@JsonIgnore`)
- Todas as rotas de Gastos requerem autenticação JWT
- Token JWT expira em **1 hora** (3600 segundos)

## 🔐 Segurança

- **JWT**: Tokens assinados com RS256
- **Validação**: Gateway valida tokens antes de rotear
- **CORS**: Configurado para desenvolvimento
- **HTTPS**: Suporte SSL/TLS configurado
- **Password Hashing**: BCrypt para senhas de usuários
- **Role-Based Access**: `@RolesAllowed("user")` em endpoints protegidos

## 📊 Observabilidade

### Jaeger (Distributed Tracing)
- URL: http://localhost:16686
- Rastreamento de requisições entre microserviços

### Graylog (Log Aggregation)
- URL: http://localhost:9000
- Usuário: admin
- Senha: admin

### Health Checks
```bash
curl http://localhost:8080/health
curl http://localhost:8080/health/live
curl http://localhost:8080/health/ready
```

## 🛠️ Desenvolvimento

### Estrutura de Diretórios

```
PW2 Projeto Final/
├── gateway/                    # API Gateway (porta 8080)
│   ├── src/main/java/br/com/gateway/
│   │   ├── GastosGatewayResource.java    # Proxy para Gastos
│   │   ├── UsersGatewayResource.java     # Proxy para Users
│   │   └── client/
│   │       ├── GastosClient.java         # REST Client
│   │       └── UsersClient.java          # REST Client
│   └── src/main/resources/
│       └── application.properties        # Config do Gateway
│
├── users/                      # Serviço de Usuários (porta 8082)
│   ├── src/main/java/dev/ifrs/
│   │   ├── UsersResource.java            # Endpoints REST
│   │   └── model/
│   │       └── User.java                 # Entidade User (Panache)
│   └── src/main/resources/
│       └── application.properties        # Config Users
│
├── gastos/                     # Serviço de Gastos (porta 8081)
│   ├── src/main/java/run/gastos/
│   │   ├── GastosResource.java           # Endpoints REST
│   │   └── model/
│   │       ├── Despesa.java              # Entidade Despesa
│   │       └── TagSum.java               # DTO para sumários
│   └── src/main/resources/
│       └── application.properties        # Config Gastos
│
├── start.sh                    # Script para iniciar todos os serviços
├── stop.sh                     # Script para parar todos os serviços
├── EXEMPLOS_CHAMADAS.md        # Exemplos completos da API
├── INICIAR.md                  # Guia de inicialização
├── TESTE.md                    # Casos de teste
└── README.md                   # Este arquivo
```

### Fluxo de Comunicação

1. **Cliente → Gateway** (porta 8080)
   - Headers: `name`, `email`, `password` (Users)
   - Query Params: `amount`, `operation`, `tag`, `date` (Gastos)
   - Header: `Authorization: Bearer <TOKEN>` (autenticação)

2. **Gateway → Microserviços**
   - MicroProfile REST Client (reativo)
   - Configuração via `application.properties`
   - Tratamento de erros com `forward()`

3. **Microserviços → MySQL**
   - Hibernate Reactive Panache
   - Reactive Queries com `Uni<T>`
   - Transações gerenciadas automaticamente

### Tecnologias

- **Framework**: Quarkus 3.29.4 (Supersonic Subatomic Java)
- **Java**: 21 (LTS)
- **Banco de Dados**: MySQL 8.0
- **ORM**: Hibernate Reactive Panache
- **Comunicação**: MicroProfile REST Client Reactive
- **Segurança**: 
  - SmallRye JWT (RS256)
  - BCrypt para hashing de senhas
  - `@RolesAllowed` para autorização
- **Reactive**: Mutiny (`Uni<T>`, `Multi<T>`)
- **Containerização**: Docker, Docker Compose
- **Observabilidade**: OpenTelemetry, Jaeger, Graylog (opcional)

### Padrões de Projeto Utilizados

- **API Gateway Pattern**: Ponto único de entrada
- **Microservices Architecture**: Serviços independentes e escaláveis
- **Repository Pattern**: Panache Active Record
- **DTO Pattern**: `TagSum` para agregações
- **Reactive Programming**: Mutiny Uni para operações assíncronas
- **REST Client Pattern**: Comunicação inter-serviços

### Hot Reload

Quarkus suporta hot reload em modo dev. Apenas salve o arquivo e veja as mudanças instantaneamente.

## 📝 Configurações de Porta

| Serviço | Porta | URL |
|---------|-------|-----|
| API Gateway | 8080 | http://localhost:8080 |
| Gastos Service | 8081 | http://localhost:8081 |
| Users Service | 8082 | http://localhost:8082 |
| MySQL Users DB | 3308 | localhost:3308 (dev) |
| MySQL Gastos DB | 3307 | localhost:3307 (dev) |
| Jaeger UI | 16686 | http://localhost:16686 |
| Graylog | 9000 | http://localhost:9000 |

## 🧪 Testes

```bash
# Testar health checks
curl http://localhost:8080/q/health
curl http://localhost:8082/q/health  # Users direto
curl http://localhost:8081/q/health  # Gastos direto

# Criar usuário de teste
curl -X POST 'http://localhost:8080/api/users/create' \
  -H 'name: Teste User' \
  -H 'email: teste@test.com' \
  -H 'password: 123456'

# Fazer login
TOKEN=$(curl -s -X POST 'http://localhost:8080/api/users/login' \
  -H 'email: teste@test.com' \
  -H 'password: 123456')

echo "Token: $TOKEN"

# Criar despesa de teste
curl -X POST 'http://localhost:8080/api/gastos/despesa/create?amount=100.00&operation=D&tag=Teste&date=2025-11-29' \
  -H "Authorization: Bearer $TOKEN"

# Listar despesas
curl -X GET 'http://localhost:8080/api/gastos/despesa/listDespesas?startDate=2025-11-01&endDate=2025-11-30' \
  -H "Authorization: Bearer $TOKEN"
```

## 🐳 Docker

### Construir imagens

```bash
# Gateway
cd gateway && ./mvnw package && cd ..

# Users
cd users && ./mvnw package && cd ..

# Gastos
cd gastos && ./mvnw package && cd ..

# Subir tudo
docker-compose up --build
```

### Limpar ambiente

```bash
docker-compose down -v
docker system prune -f
```

## 📚 Documentação Adicional

- **[EXEMPLOS_CHAMADAS.md](EXEMPLOS_CHAMADAS.md)** - Exemplos completos de todas as rotas da API
- **[INICIAR.md](INICIAR.md)** - Guia detalhado de inicialização dos serviços
- **[TESTE.md](TESTE.md)** - Casos de teste e validações
- [Gateway README](gateway/README.md) - Documentação detalhada do API Gateway
- [Quarkus Guides](https://quarkus.io/guides/) - Guias oficiais do Quarkus

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto é para fins educacionais.

## 👥 Autores

Desenvolvido como parte do curso de Programação Web 2.
