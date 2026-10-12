#!/bin/bash

# Script de inicialização dos microserviços em modo desenvolvimento

echo "🚀 Iniciando microserviços..."

# Verifica se o MySQL está rodando
if ! docker ps | grep -q mysql-db; then
    echo "📦 Iniciando MySQL..."
    docker run -d --name mysql-db \
      -e MYSQL_ROOT_PASSWORD=root \
      -e MYSQL_DATABASE=pw2 \
      -e MYSQL_USER=pw2 \
      -e MYSQL_PASSWORD=pw2 \
      -p 3306:3306 \
      mysql:8.0
    
    echo "⏳ Aguardando MySQL inicializar..."
    sleep 20
else
    echo "✅ MySQL já está rodando na porta 3306"
fi

# Verifica se o banco pw2 existe, se não, cria
docker exec mysql-db mysql -upw2 -ppw2 -e "CREATE DATABASE IF NOT EXISTS pw2;" 2>/dev/null || true

echo "✅ MySQL pronto!"
echo ""

# Informa ao usuário para abrir terminais separados
echo "📋 Para iniciar os serviços, abra 3 terminais e execute:"
echo ""
echo "Terminal 1 - Users Service:"
echo "  cd users && ./mvnw quarkus:dev"
echo ""
echo "Terminal 2 - Gastos Service:"
echo "  cd gastos && ./mvnw quarkus:dev"
echo ""
echo "Terminal 3 - API Gateway:"
echo "  cd gateway && ./mvnw quarkus:dev"
echo ""
echo "Ou use este comando para iniciar tudo em background:"
echo "  cd users && ./mvnw quarkus:dev > /tmp/users.log 2>&1 &"
echo "  cd gastos && ./mvnw quarkus:dev > /tmp/gastos.log 2>&1 &"
echo "  cd gateway && ./mvnw quarkus:dev"
