# API Gateway - Microserviços

Este é o API Gateway que orquestra os microserviços de **Users** e **Gastos**.

## 🏗️ Arquitetura

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│   API Gateway       │
│   (porta 8080)      │
└──────┬──────┬───────┘
       │      │
       ▼      ▼
┌──────────┐ ┌──────────┐
│  Users   │ │  Gastos  │
│ (8082)   │ │ (8081)   │
└────┬─────┘ └────┬─────┘
     │            │
     └─────┬──────┘
           ▼
      ┌─────────┐
      │  MySQL  │
      │  (3306) │
      └─────────┘
```

## 🚀 Como executar

### Desenvolvimento (local)

1. **Inicie os serviços dependentes:**
```bash
# Inicie o MySQL
docker run -d --name mysql-db \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=pw2 \
  -e MYSQL_USER=pw2 \
  -e MYSQL_PASSWORD=pw2 \
  -p 3306:3306 \
  mysql:8.0

# Inicie o serviço de Users (na pasta users)
cd ../users
./mvnw quarkus:dev -Dquarkus.http.port=8082

# Inicie o serviço de Gastos (na pasta gastos)
cd ../gastos
./mvnw quarkus:dev -Dquarkus.http.port=8081

# Inicie o Gateway (na pasta gateway)
cd ../gateway
./mvnw quarkus:dev
```

2. **O Gateway estará disponível em:** `http://localhost:8080`

### Produção (Docker Compose)

```bash
# Na raiz do projeto
docker-compose up --build
```

Isso iniciará:
- MySQL (porta 3306)
- Users Service (porta 8082)
- Gastos Service (porta 8081)
- API Gateway (porta 8080)
- Jaeger (UI em http://localhost:16686)
- Graylog (UI em http://localhost:9000)

## 📡 Endpoints

Todos os endpoints agora são acessados via Gateway na porta **8080** com prefixo `/api`:

### Usuários
```
POST   /api/users/login          - Login e obtenção de token JWT
POST   /api/users/create         - Criar novo usuário
PATCH  /api/users/updateUser     - Atualizar usuário (requer JWT)
GET    /api/users/getUsers       - Listar todos usuários (requer JWT)
GET    /api/users/getUser        - Obter usuário atual (requer JWT)
```

### Gastos
```
GET    /api/gastos/test-auth                - Testar autenticação
POST   /api/gastos/despesa/create           - Criar despesa
GET    /api/gastos/despesa/sumario          - Resumo de gastos
GET    /api/gastos/despesa/sumarioTag       - Resumo por tag
GET    /api/gastos/despesa/listDespesas     - Listar despesas
GET    /api/gastos/despesa/listTagSum       - Listar soma por tags
PATCH  /api/gastos/despesa/update           - Atualizar despesa
DELETE /api/gastos/despesa/delete           - Deletar despesa
```

### Health Check
```
GET    /api/health              - Status do Gateway
GET    /health                  - Health check completo
GET    /health/live            - Liveness probe
GET    /health/ready           - Readiness probe
```

## 🔐 Autenticação

1. **Obter token:**
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"senha123"}'
```

2. **Usar token nas requisições:**
```bash
curl -X GET http://localhost:8080/api/gastos/despesa/listDespesas?startDate=2025-01-01&endDate=2025-12-31 \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

## 🧪 Testar

```bash
# Health check
curl http://localhost:8080/api/health

# Criar usuário
curl -X POST http://localhost:8080/api/users/create \
  -H "Content-Type: application/json" \
  -d '{"name":"João Silva","email":"joao@example.com","password":"senha123"}'

# Login
TOKEN=$(curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@example.com","password":"senha123"}' -s)

# Criar despesa
curl -X POST http://localhost:8080/api/gastos/despesa/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"amount":150.50,"operation":"D","date":"2025-11-28","tag":"Alimentação"}'
```

## 📊 Observabilidade

- **Jaeger (Tracing):** http://localhost:16686
- **Graylog (Logs):** http://localhost:9000 (user: admin, senha: admin)
- **Health Checks:** http://localhost:8080/health

## 🔧 Configuração

As configurações estão em `src/main/resources/application.properties`:

- URLs dos microserviços
- Configurações de JWT
- CORS
- OpenTelemetry
- Logging

## 🛠️ Tecnologias

- **Quarkus 3.29.4** - Framework Java reativo
- **REST Client Reactive** - Comunicação entre microserviços
- **SmallRye JWT** - Validação de tokens
- **OpenTelemetry** - Tracing distribuído
- **Docker** - Containerização
- **MySQL** - Banco de dados

## 📝 Notas

- O Gateway faz proxy transparente para os microserviços
- Tokens JWT são validados no Gateway
- CORS está configurado para aceitar qualquer origem em desenvolvimento
- Health checks garantem que os serviços estejam prontos antes de aceitar requisições
