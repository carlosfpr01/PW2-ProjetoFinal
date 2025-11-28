# 🚀 Guia Rápido de Inicialização

## Problema: Porta 8080 não está funcionando

O gateway **precisa ser iniciado separadamente**. Siga os passos:

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
curl http://localhost:8080/api/health

# Testar Users
curl http://localhost:8082/health

# Testar Gastos
curl http://localhost:8081/health
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
