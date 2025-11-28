#!/bin/bash

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  Inicializador de Microserviços        ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
echo ""

# Função para verificar se uma porta está em uso
check_port() {
    lsof -i :$1 > /dev/null 2>&1
    return $?
}

# Função para verificar se um serviço está respondendo
check_service() {
    curl -s -o /dev/null -w "%{http_code}" $1 | grep -q "200\|404"
    return $?
}

# 1. Verificar MySQL
echo -e "${YELLOW}[1/4]${NC} Verificando MySQL..."
if docker ps | grep -q mysql-db; then
    echo -e "${GREEN}✅ MySQL já está rodando${NC}"
else
    echo -e "${YELLOW}⚠️  MySQL não encontrado. Iniciando...${NC}"
    docker run -d --name mysql-db \
      -e MYSQL_ROOT_PASSWORD=root \
      -e MYSQL_DATABASE=pw2 \
      -e MYSQL_USER=pw2 \
      -e MYSQL_PASSWORD=pw2 \
      -p 3306:3306 \
      mysql:8.0 > /dev/null 2>&1
    
    echo -e "${YELLOW}⏳ Aguardando MySQL inicializar (20s)...${NC}"
    sleep 20
    echo -e "${GREEN}✅ MySQL iniciado${NC}"
fi
echo ""

# 2. Verificar Users Service
echo -e "${YELLOW}[2/4]${NC} Verificando Users Service (porta 8082)..."
if check_service "http://localhost:8082/health"; then
    echo -e "${GREEN}✅ Users Service está respondendo${NC}"
elif check_port 8082; then
    echo -e "${YELLOW}⚠️  Porta 8082 em uso, mas serviço não responde${NC}"
else
    echo -e "${RED}❌ Users Service NÃO está rodando${NC}"
    echo -e "${YELLOW}   Execute em outro terminal: cd users && ./mvnw quarkus:dev${NC}"
fi
echo ""

# 3. Verificar Gastos Service
echo -e "${YELLOW}[3/4]${NC} Verificando Gastos Service (porta 8081)..."
if check_service "http://localhost:8081/health"; then
    echo -e "${GREEN}✅ Gastos Service está respondendo${NC}"
elif check_port 8081; then
    echo -e "${YELLOW}⚠️  Porta 8081 em uso, mas serviço não responde${NC}"
else
    echo -e "${RED}❌ Gastos Service NÃO está rodando${NC}"
    echo -e "${YELLOW}   Execute em outro terminal: cd gastos && ./mvnw quarkus:dev${NC}"
fi
echo ""

# 4. Verificar Gateway
echo -e "${YELLOW}[4/4]${NC} Verificando Gateway (porta 8080)..."
if check_service "http://localhost:8080/api/health"; then
    echo -e "${GREEN}✅ Gateway está respondendo${NC}"
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  ✅ TODOS OS SERVIÇOS ESTÃO ATIVOS!   ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
elif check_port 8080; then
    echo -e "${YELLOW}⚠️  Porta 8080 em uso, mas gateway não responde${NC}"
else
    echo -e "${RED}❌ Gateway NÃO está rodando${NC}"
    echo ""
    echo -e "${GREEN}══════════════════════════════════════════${NC}"
    echo -e "${GREEN}Para iniciar o Gateway, execute:${NC}"
    echo -e "${YELLOW}  cd gateway && ./mvnw quarkus:dev${NC}"
    echo -e "${GREEN}══════════════════════════════════════════${NC}"
fi

echo ""
echo -e "${GREEN}📡 Status dos Endpoints:${NC}"
echo "   - Gateway:       http://localhost:8080/api/health"
echo "   - Users Service: http://localhost:8082/health"
echo "   - Gastos Service: http://localhost:8081/health"
echo ""
echo -e "${YELLOW}📖 Para mais informações, leia: INICIAR.md${NC}"
