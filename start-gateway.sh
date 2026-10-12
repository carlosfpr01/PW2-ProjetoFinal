#!/bin/bash

echo "🚀 Iniciando API Gateway na porta 8080..."
echo ""

# Verificar se MySQL está rodando
if ! docker ps | grep -q mysql-db; then
    echo "❌ MySQL não está rodando!"
    echo "Execute primeiro: docker run -d --name mysql-db -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=pw2 -e MYSQL_USER=pw2 -e MYSQL_PASSWORD=pw2 -p 3306:3306 mysql:8.0"
    exit 1
fi

echo "✅ MySQL está rodando"
echo ""

# Verificar se Users está rodando
if ! curl -s http://localhost:8082/health > /dev/null 2>&1; then
    echo "⚠️  Users Service não está respondendo na porta 8082"
    echo "Inicie em outro terminal: cd users && ./mvnw quarkus:dev"
    echo ""
fi

# Verificar se Gastos está rodando
if ! curl -s http://localhost:8081/health > /dev/null 2>&1; then
    echo "⚠️  Gastos Service não está respondendo na porta 8081"
    echo "Inicie em outro terminal: cd gastos && ./mvnw quarkus:dev"
    echo ""
fi

# Ir para o diretório do gateway
cd "$(dirname "$0")/gateway"

echo "🌐 Iniciando Gateway..."
echo "Acesse: http://localhost:8080/api/health"
echo ""

./mvnw quarkus:dev
