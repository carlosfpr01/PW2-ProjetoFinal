# 🚀 Guia Rápido de Inicialização - Microserviços PW2

## Arquitetura do Projeto

Este projeto utiliza arquitetura de microserviços com API Gateway:
- **Gateway** (porta 8080) - Ponto de entrada único para todas as requisições
- **Users Service** (porta 8082) - Autenticação e gerenciamento de usuários
- **Gastos Service** (porta 8081) - Gerenciamento de despesas e receitas

O gateway **precisa ser iniciado por último**, após users e gastos estarem rodando.

## ✅ Passo a Passo

### 1. Verificar MySQL
```bash
docker ps | grep mysql-db
```

Se não estiver rodando:
```bash
docker run -d --name mysql-db \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=pw2 \
  -e MYSQL_USER=pw2 \
  -e MYSQL_PASSWORD=pw2 \
  -p 3306:3306 \
  mysql:8.0
```

### 2. Iniciar Users Service (Terminal 1)
```bash
cd users
./mvnw quarkus:dev
```

Aguarde até ver: `Listening on: http://localhost:8082`

### 3. Iniciar Gastos Service (Terminal 2)
```bash
cd gastos
./mvnw quarkus:dev
```

Aguarde até ver: `Listening on: http://localhost:8081`

### 4. Iniciar Gateway (Terminal 3)
```bash
cd gateway
./mvnw quarkus:dev
```

Aguarde até ver: `Listening on: http://localhost:8080`

## 🧪 Testar

```bash
# Testar Gateway
curl http://localhost:8080/q/health

# Testar Users
curl http://localhost:8082/q/health

# Testar Gastos
curl http://localhost:8081/q/health

# Teste Completo - Criar usuário via Gateway
curl -X POST 'http://localhost:8080/api/users/create' \
  -H 'name: Test User' \
  -H 'email: test@example.com' \
  -H 'password: test123'

# Login e obter token
TOKEN=$(curl -s -X POST 'http://localhost:8080/api/users/login' \
  -H 'email: test@example.com' \
  -H 'password: test123')

echo "Token: $TOKEN"

# Criar uma despesa
curl -X POST 'http://localhost:8080/api/gastos/despesa/create?amount=100.00&operation=D&tag=Teste&date=2025-11-29' \
  -H "Authorization: Bearer $TOKEN"
```

## 🎯 Script Automatizado

Para facilitar, use:

```bash
# Dar permissão
chmod +x start-gateway.sh

# Executar (depois de iniciar users e gastos)
./start-gateway.sh
```

## 📋 Ordem de Inicialização

1. ✅ MySQL (docker)
2. ✅ Users Service (porta 8082)
3. ✅ Gastos Service (porta 8081)
4. ✅ Gateway (porta 8080) - **Inicia por último!**

## 📝 Importante - Formato das Requisições

### Users Service (via Gateway)
- **Parâmetros**: Sempre via **Headers**
- **Exemplo**: `-H 'name: João' -H 'email: joao@example.com' -H 'password: senha123'`
- **NÃO use**: JSON body (`-d '{...}'`)

### Gastos Service (via Gateway)
- **Parâmetros de dados**: Via **Query Params** (`?amount=100&operation=D`)
- **Autenticação**: Via Header `Authorization: Bearer <TOKEN>`
- **NÃO use**: JSON body para os dados

### Respostas
- O campo **password** nunca é retornado (protegido com @JsonIgnore)
- Todas as rotas de Gastos requerem autenticação JWT

## ❌ Erros Comuns

### "Connection refused" no Gateway
- **Causa:** Users ou Gastos não estão rodando
- **Solução:** Inicie users e gastos primeiro

### "Port already in use"
- **Causa:** Já existe algo na porta
- **Solução:** 
```bash
# Encontrar processo
lsof -i :8080

# Matar processo
kill -9 <PID>
```

### "Cannot connect to MySQL"
- **Causa:** MySQL não está rodando ou não foi criado o banco
- **Solução:**
```bash
docker exec -it mysql-db mysql -upw2 -ppw2 -e "CREATE DATABASE IF NOT EXISTS pw2;"
```

## 🔄 Reiniciar Tudo

```bash
# Parar serviços (Ctrl+C em cada terminal)

# Remover MySQL antigo (opcional)
docker rm -f mysql-db

# Recomeçar do passo 1
```

## 📚 Documentação Completa

- **EXEMPLOS_CHAMADAS.md** - Exemplos detalhados de todas as rotas da API
- **README.md** - Visão geral do projeto
- **TESTE.md** - Testes e validações
