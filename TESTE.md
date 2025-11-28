# 🧪 Guia de Teste Rápido - API Gateway

## 1. Iniciar os Serviços

```bash
# Na raiz do projeto
cd /workspaces/pw2-2025_2

# Dar permissão ao script
chmod +x start-services.sh

# Executar
./start-services.sh
```

## 2. Verificar Saúde dos Serviços

```bash
# Gateway
curl http://localhost:8080/api/health

# Users
curl http://localhost:8082/health || echo "Users ainda inicializando..."

# Gastos  
curl http://localhost:8081/health || echo "Gastos ainda inicializando..."
```

## 3. Criar Usuário

```bash
curl -X POST http://localhost:8080/api/users/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@example.com",
    "password": "senha123"
  }'
```

## 4. Fazer Login e Obter Token

```bash
# Fazer login
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "password": "senha123"
  }'

# Copie o token retornado e use nas próximas requisições
```

## 5. Testar Autenticação

```bash
# Substitua SEU_TOKEN_AQUI pelo token obtido no login
export TOKEN="SEU_TOKEN_AQUI"

curl -X GET http://localhost:8080/api/gastos/test-auth \
  -H "Authorization: Bearer $TOKEN"
```

## 6. Criar Despesa

```bash
curl -X POST http://localhost:8080/api/gastos/despesa/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 150.50,
    "operation": "D",
    "date": "2025-11-28",
    "tag": "Alimentação"
  }'
```

## 7. Criar mais Despesas para Teste

```bash
# Despesa 2
curl -X POST http://localhost:8080/api/gastos/despesa/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 80.00,
    "operation": "D",
    "date": "2025-11-27",
    "tag": "Transporte"
  }'

# Crédito (entrada)
curl -X POST http://localhost:8080/api/gastos/despesa/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 2000.00,
    "operation": "C",
    "date": "2025-11-25",
    "tag": "Salário"
  }'
```

## 8. Listar Despesas

```bash
curl -X GET "http://localhost:8080/api/gastos/despesa/listDespesas?startDate=2025-01-01&endDate=2025-12-31" \
  -H "Authorization: Bearer $TOKEN"
```

## 9. Ver Resumo de Gastos

```bash
curl -X GET "http://localhost:8080/api/gastos/despesa/sumario?startDate=2025-01-01&endDate=2025-12-31" \
  -H "Authorization: Bearer $TOKEN"
```

## 10. Ver Resumo por Tags

```bash
curl -X GET "http://localhost:8080/api/gastos/despesa/listTagSum?startDate=2025-01-01&endDate=2025-12-31" \
  -H "Authorization: Bearer $TOKEN"
```

## 🎯 Teste Completo Automatizado

Copie e cole este script completo:

```bash
#!/bin/bash

echo "🧪 Iniciando testes da API Gateway..."
echo ""

# 1. Health check
echo "1️⃣ Verificando saúde do Gateway..."
curl -s http://localhost:8080/api/health | jq '.' || echo "Gateway não está respondendo"
echo ""

# 2. Criar usuário
echo "2️⃣ Criando usuário teste..."
curl -s -X POST http://localhost:8080/api/users/create \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@test.com","password":"123456"}' | jq '.'
echo ""

# 3. Fazer login
echo "3️⃣ Fazendo login..."
TOKEN=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456"}')

echo "Token obtido: ${TOKEN:0:50}..."
echo ""

# 4. Testar autenticação
echo "4️⃣ Testando autenticação..."
curl -s -X GET http://localhost:8080/api/gastos/test-auth \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 5. Criar despesas
echo "5️⃣ Criando despesas de teste..."
curl -s -X POST http://localhost:8080/api/gastos/despesa/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"amount":150.50,"operation":"D","date":"2025-11-28","tag":"Alimentação"}' | jq '.'
echo ""

curl -s -X POST http://localhost:8080/api/gastos/despesa/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"amount":2000.00,"operation":"C","date":"2025-11-25","tag":"Salário"}' | jq '.'
echo ""

# 6. Listar despesas
echo "6️⃣ Listando despesas..."
curl -s -X GET "http://localhost:8080/api/gastos/despesa/listDespesas?startDate=2025-01-01&endDate=2025-12-31" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 7. Ver resumo
echo "7️⃣ Obtendo resumo de gastos..."
curl -s -X GET "http://localhost:8080/api/gastos/despesa/sumario?startDate=2025-01-01&endDate=2025-12-31" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

echo "✅ Testes completos!"
```

Salve como `test-api.sh`, dê permissão (`chmod +x test-api.sh`) e execute (`./test-api.sh`).

## 📊 Observabilidade

### Jaeger (Tracing)
```bash
# Abra no navegador
xdg-open http://localhost:16686
# ou
$BROWSER http://localhost:16686
```

### Logs dos Serviços
```bash
# Gateway
tail -f gateway/quarkus.log

# Users
tail -f users/quarkus.log

# Gastos
tail -f gastos/quarkus.log
```

## 🔍 Troubleshooting

### Serviço não inicia
```bash
# Verificar se a porta está em uso
netstat -tuln | grep -E "8080|8081|8082"

# Matar processo na porta
kill -9 $(lsof -t -i:8080)
```

### Token inválido
- Verifique se copiou o token completo
- Token expira em 1 hora
- Faça login novamente

### Erro de conexão com MySQL
```bash
# Verificar se MySQL está rodando
docker ps | grep mysql

# Ver logs do MySQL
docker logs mysql-db
```

## ✅ Checklist de Sucesso

- [ ] Gateway respondendo em http://localhost:8080
- [ ] Users respondendo em http://localhost:8082
- [ ] Gastos respondendo em http://localhost:8081
- [ ] Criação de usuário funcionando
- [ ] Login retornando token JWT
- [ ] Criação de despesas funcionando
- [ ] Listagem de despesas funcionando
- [ ] Resumos sendo gerados corretamente
- [ ] Jaeger mostrando traces (opcional)
