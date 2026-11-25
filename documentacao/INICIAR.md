# Inicialização da aplicação

Use o único script de inicialização na raiz do projeto:

```bash
./start-services.sh
```

O script executa `docker compose up`, iniciando todos os serviços definidos em `docker-compose.yml`. O gateway aguarda Users e Gastos passarem nos health checks.

Para iniciar em segundo plano:

```bash
./start-services.sh -d
```

Para acompanhar a inicialização em segundo plano:

```bash
docker compose logs -f gateway users gastos
```

O gateway só inicia após Users e Gastos passarem nos health checks. Endpoints:

- Gateway: http://localhost:8080
- Users: http://localhost:8082
- Gastos: http://localhost:8081
- Health do gateway: http://localhost:8080/health

Para parar a stack, use `Ctrl+C` quando executada em primeiro plano ou `docker compose down` quando executada em segundo plano. Também é possível chamar `docker compose up` diretamente na raiz do projeto.
