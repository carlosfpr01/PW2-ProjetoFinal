# Exemplos de Chamadas do Gateway API

**URL Base**: `http://localhost:8080`

---

## 1. USERS - Autenticação e Gerenciamento de Usuários

### 1.1 Criar Usuário
```bash
curl -X POST 'http://localhost:8080/api/users/create' \
  -H 'name: João Silva' \
  -H 'email: joao@example.com' \
  -H 'password: senha123'
```

**Resposta:**
```json
{
  "id": 1,
  "name": "João Silva",
  "email": "joao@example.com",
  "dataCriacao": "2025-11-28T10:30:00"
}
```

---

### 1.2 Fazer Login (Obter Token JWT)
```bash
TOKEN=$(curl -s -X POST 'http://localhost:8080/api/users/login' \
  -H 'email: joao@example.com' \
  -H 'password: senha123')

echo "Token: $TOKEN"
```

**Resposta:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibmFtZSI6Ikpvw6NvIFNpbHZhIiwiaWF0IjoxNjcwNjU5NDAwfQ...
```

---

### 1.3 Atualizar Usuário (Requer Autenticação)
```bash
curl -X PATCH 'http://localhost:8080/api/users/updateUser' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'name: João Silva Atualizado' \
  -H 'email: joao.novo@example.com' \
  -H 'password: novaSenha456'
```

**Resposta:**
```json
{
  "id": 1,
  "name": "João Silva Atualizado",
  "email": "joao.novo@example.com",
  "dataCriacao": "2025-11-28T10:30:00"
}
```

---

## 2. GASTOS - Gerenciamento de Despesas e Receitas

### ⚠️ IMPORTANTE: Todas as rotas de GASTOS requerem autenticação com Token JWT
```bash
# Use o token obtido no login dos usuários
-H "Authorization: Bearer $TOKEN"
```

---

### 2.1 Criar Despesa/Receita
```bash
curl -X POST 'http://localhost:8080/api/gastos/despesa/create?amount=1500.50&operation=D&tag=Alimentação&date=2025-11-28' \
  -H "Authorization: Bearer $TOKEN"
```

**Parâmetros (Query Params):**
- `amount`: (number) Valor da despesa/receita
- `operation`: (string) "D" para Despesa ou "C" para Crédito/Receita
- `tag`: (string) Categoria (ex: Salário, Alimentação, Transporte)
- `date`: (string) Data no formato YYYY-MM-DD

**Resposta:**
```json
{
  "id": 1,
  "idUser": 1,
  "amount": 1500.50,
  "operation": "D",
  "tag": "Alimentação",
  "date": "2025-11-28"
}
```

---

### 2.2 Listar Despesas com Filtros
```bash
curl -X GET 'http://localhost:8080/api/gastos/despesa/listDespesas?startDate=2025-11-01&endDate=2025-11-30' \
  -H "Authorization: Bearer $TOKEN"
```

**Parâmetros de Query:**
- `startDate`: (string) Data inicial - YYYY-MM-DD
- `endDate`: (string) Data final - YYYY-MM-DD

**Resposta:**
```json
[
  {
    "id": 1,
    "idUser": 1,
    "amount": 1500.50,
    "operation": "D",
    "tag": "Alimentação",
    "date": "2025-11-28"
  },
  {
    "id": 2,
    "idUser": 1,
    "amount": 5000.00,
    "operation": "C",
    "tag": "Salário",
    "date": "2025-11-01"
  }
]
```

---

### 2.3 Obter Sumário de Despesas (Total por Tipo)
```bash
curl -X GET 'http://localhost:8083/api/gastos/despesa/sumario?startDate=2025-11-01&endDate=2025-11-30' \
  -H 'accept: application/json' \
  -H "Authorization: Bearer $TOKEN"
```

**Parâmetros de Query:**
- `startDate`: (string) Data inicial - YYYY-MM-DD
- `endDate`: (string) Data final - YYYY-MM-DD

**Resposta:**
```json
{
  "totalDespesas": 2500.50,
  "totalReceitas": 5000.00,
  "saldo": 2499.50
}
```

---

### 2.4 Obter Sumário por Tag
```bash
curl -X GET 'http://localhost:8080/api/gastos/despesa/sumarioTag?tag=Alimentação&startDate=2025-11-01&endDate=2025-11-30' \
  -H "Authorization: Bearer $TOKEN"
```

**Parâmetros de Query:**
- `tag`: (string) Nome da tag/categoria
- `startDate`: (string) Data inicial - YYYY-MM-DD
- `endDate`: (string) Data final - YYYY-MM-DD

**Resposta:**
```json
{
  "tag": "Alimentação",
  "totalDespesas": 450.00,
  "totalReceitas": 0.00
}
```

---

### 2.5 Listar Sumário por Tags
```bash
curl -X GET 'http://localhost:8080/api/gastos/despesa/listTagSum?startDate=2025-11-01&endDate=2025-11-30' \
  -H "Authorization: Bearer $TOKEN"
```

**Parâmetros de Query:**
- `startDate`: (string) Data inicial - YYYY-MM-DD
- `endDate`: (string) Data final - YYYY-MM-DD

**Resposta:**
```json
[
  {
    "tag": "Alimentação",
    "totalDespesas": 450.00,
    "totalReceitas": 0.00
  },
  {
    "tag": "Salário",
    "totalDespesas": 0.00,
    "totalReceitas": 5000.00
  },
  {
    "tag": "Transporte",
    "totalDespesas": 150.00,
    "totalReceitas": 0.00
  }
]
```

---

### 2.6 Atualizar Despesa
```bash
curl -X PATCH 'http://localhost:8080/api/gastos/despesa/update?id=1&amount=2000.00&operation=D&tag=Alimentação Premium&date=2025-11-28' \
  -H "Authorization: Bearer $TOKEN"
```

**Parâmetros (Query Params):**
- `id`: (number) ID da despesa a atualizar
- `amount`: (number) Novo valor (opcional)
- `operation`: (string) Novo tipo (opcional)
- `tag`: (string) Nova tag (opcional)
- `date`: (string) Nova data (opcional)

**Resposta:**
```json
{
  "id": 1,
  "idUser": 1,
  "amount": 2000.00,
  "operation": "D",
  "tag": "Alimentação Premium",
  "date": "2025-11-28"
}
```

---

### 2.7 Deletar Despesa
```bash
curl -X DELETE 'http://localhost:8080/api/gastos/despesa/delete?id=1' \
  -H "Authorization: Bearer $TOKEN"
```

**Parâmetros de Query:**
- `id`: (number) ID da despesa a deletar

**Resposta:**
```json
{
  "message": "Despesa deletada com sucesso"
}
```

---

### 2.8 Testar Autenticação
```bash
curl -X GET 'http://localhost:8080/api/gastos/test-auth' \
  -H 'accept: application/json' \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta:**
```json
{
  "userId": "1",
  "upn": "joao@example.com",
  "groups": "[user]",
  "authenticated": true
}
```

---

## Script de Teste Completo

```bash
#!/bin/bash

# 1. Criar usuário
echo "=== Criando usuário ==="
curl -X POST 'http://localhost:8080/api/users/create' \
  -H 'name: Teste User' \
  -H 'email: teste@example.com' \
  -H 'password: teste123'

echo -e "\n\n=== Fazendo login ==="
TOKEN=$(curl -s -X POST 'http://localhost:8080/api/users/login' \
  -H 'email: teste@example.com' \
  -H 'password: teste123')

echo "Token obtido: $TOKEN"

echo -e "\n\n=== Criando despesa ==="
curl -X POST 'http://localhost:8080/api/gastos/despesa/create?amount=150.00&operation=D&tag=Alimentação&date=2025-11-28' \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n\n=== Listando despesas ==="
curl -X GET 'http://localhost:8080/api/gastos/despesa/listDespesas?startDate=2025-11-01&endDate=2025-11-30' \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n\n=== Obter sumário ==="
curl -X GET 'http://localhost:8080/api/gastos/despesa/sumario?startDate=2025-11-01&endDate=2025-11-30' \
  -H 'accept: application/json' \
  -H "Authorization: Bearer $TOKEN"
```

---

## Códigos de Erro Comuns

| Código | Significado |
|--------|------------|
| 200 | ✅ Sucesso |
| 201 | ✅ Criado com sucesso |
| 400 | ❌ Requisição inválida (parâmetros errados) |
| 401 | ❌ Não autenticado (falta token ou token expirado) |
| 404 | ❌ Recurso não encontrado |
| 500 | ❌ Erro interno do servidor |

---

## Notas Importantes

1. **Token JWT**: Válido por **1 hora** (3600 segundos)
2. **Datas**: Formato `YYYY-MM-DD` (ex: 2025-11-28)
3. **Operation**: Use `"D"` para Despesa ou `"C"` para Crédito/Receita
4. **Tag**: Recomendado usar tags padrão como: Salário, Alimentação, Transporte, Saúde, Educação, Lazer, Outros
5. **Authorization Header**: Sempre use `Authorization: Bearer <TOKEN>`
6. **Parâmetros Users**: Sempre via Headers (`-H 'name: valor'`)
7. **Parâmetros Gastos**: Sempre via Query Params (`?amount=valor&operation=D`)
8. **Senha**: Nunca é retornada nas respostas JSON (protegida com @JsonIgnore)

---

## Gateway - Arquitetura

```
┌─────────────────────────────────────────┐
│        Client (seu curl/postman)        │
└────────────────┬────────────────────────┘
                 │
                 ▼
        ┌────────────────────┐
        │  API GATEWAY       │
        │  (porta 8080)      │
        └─┬──────────────┬───┘
          │              │
          ▼              ▼
    ┌─────────────┐  ┌──────────────┐
    │ USERS       │  │ GASTOS       │
    │ (porta 8082)│  │ (porta 8081) │
    └─────────────┘  └──────────────┘
```

---

**Última atualização**: 28 de Novembro de 2025
