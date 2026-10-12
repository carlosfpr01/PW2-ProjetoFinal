# Microserviços - Users e Gastos com API Gateway

Sistema de microserviços para gerenciamento de usuários e controle de gastos.

## 🏗️ Arquitetura

```
                    ┌─────────────────┐
                    │   API Gateway   │
                    │   (porta 8080)  │
                    └────────┬────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
            ┌───────▼──────┐   ┌──────▼───────┐
            │ Users Service│   │Gastos Service│
            │  (porta 8082)│   │ (porta 8081) │
            └───────┬──────┘   └──────┬───────┘
                    │                │
                    |                |
                    ▼                ▼
          ┌─────────────┐         ┌─────────────┐
          │    MySQL    │         │    MySQL    │
          │ (porta 3306)│         │ (porta 3307)│
          └─────────────┘         └─────────────┘

```

## 📦 Componentes

- **API Gateway**: Ponto único de entrada, roteamento e validação JWT
- **Users Service**: Autenticação, registro e gerenciamento de usuários
- **Gastos Service**: CRUD de despesas e relatórios financeiros
- **MySQL**: Banco de dados compartilhado

## 🚀 Início Rápido

### Opção 1: Docker Compose (Recomendado)

```bash
# No Linux, configure o limite necessário pelo OpenSearch
sudo sysctl -w vm.max_map_count=262144

# Inicia todos os serviços, bancos de dados e ferramentas de observabilidade
./start-services.sh

# Acesse:
# - API Gateway: http://localhost:8080
# - Users Service: http://localhost:8082
# - Gastos Service: http://localhost:8081
# - Jaeger UI: http://localhost:16686
# - Graylog: http://localhost:9000
#   usuário: admin | senha: admin (ambiente local)
```

Para encerrar, execute `docker compose down`. Os dados dos bancos permanecem nos volumes; para apagá-los também, use `docker compose down -v`.

### Opção 2: Desenvolvimento Local (sem Compose)

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
# Criar usuário
curl -X POST http://localhost:8080/api/users/create \
  -H "Content-Type: application/json" \
  -d '{"name":"João Silva","email":"joao@example.com","password":"senha123"}'

# Login (retorna JWT token)
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@example.com","password":"senha123"}'
```

### Gerenciar Despesas (requer token)

```bash
# Salvar o token em uma variável
TOKEN="seu_token_aqui"

# Criar despesa
curl -X POST http://localhost:8080/api/gastos/despesa/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"amount":150.50,"operation":"D","date":"2025-11-28","tag":"Alimentação"}'

# Listar despesas
curl -X GET "http://localhost:8080/api/gastos/despesa/listDespesas?startDate=2025-01-01&endDate=2025-12-31" \
  -H "Authorization: Bearer $TOKEN"

# Resumo de gastos
curl -X GET "http://localhost:8080/api/gastos/despesa/sumario?startDate=2025-01-01&endDate=2025-12-31" \
  -H "Authorization: Bearer $TOKEN"
```

## 🔐 Segurança

- **JWT**: Tokens assinados com RS256
- **Validação**: Gateway valida tokens antes de rotear
- **CORS**: Configurado para desenvolvimento
- **HTTPS**: Suporte SSL/TLS configurado

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
.
├── gateway/          # API Gateway (porta 8080)
├── users/            # Serviço de usuários (porta 8082)
├── gastos/           # Serviço de gastos (porta 8081)
├── docker-compose.yml
└── start-services.sh # Inicia toda a stack com Docker Compose
```

### Tecnologias

- **Framework**: Quarkus 3.29.4
- **Java**: 21
- **Banco**: MySQL 8.0
- **Comunicação**: REST Client Reactive
- **Segurança**: SmallRye JWT
- **Observabilidade**: OpenTelemetry, Jaeger, Graylog

### Hot Reload

Quarkus suporta hot reload em modo dev. Apenas salve o arquivo e veja as mudanças instantaneamente.

## 📝 Configurações de Porta

| Serviço | Porta | URL |
|---------|-------|-----|
| API Gateway | 8080 | http://localhost:8080 |
| Gastos Service | 8081 | http://localhost:8081 |
| Users Service | 8082 | http://localhost:8082 |
| MySQL | 3306 | localhost:3306 |
| Jaeger UI | 16686 | http://localhost:16686 |
| Graylog | 9000 | http://localhost:9000 |

## 🧪 Testes

```bash
# Testar health do gateway
curl http://localhost:8080/api/health

# Criar usuário de teste
curl -X POST http://localhost:8080/api/users/create \
  -H "Content-Type: application/json" \
  -d '{"name":"Teste","email":"teste@test.com","password":"123456"}'

# Fazer login
TOKEN=$(curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teste@test.com","password":"123456"}' -s)

echo "Token: $TOKEN"

# Testar autenticação
curl -X GET http://localhost:8080/api/gastos/test-auth \
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
./start-services.sh
```

### Limpar ambiente

```bash
docker-compose down -v
docker system prune -f
```

## 📚 Documentação Adicional

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
