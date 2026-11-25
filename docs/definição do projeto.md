# Definição do Projeto

*Trabalho de Verificação e Validação de Software*

## Sistema escolhido

**Sistema de Gerenciamento de Gastos com API Gateway**

Aplicação composta por serviços REST para cadastro e autenticação de usuários e gerenciamento de despesas e receitas. Um API Gateway oferece o ponto de entrada para os clientes, encaminha chamadas aos serviços e protege operações que exigem autenticação JWT. Os dados são persistidos em MySQL.

>### **Link do projeto no GitHub:** https://github.com/carlosfpr01/PW2-ProjetoFinal

## Linguagem de programação

>Java 25 (LTS), padronizado nos três serviços e imagens JVM.

## Frameworks e tecnologias

- **Quarkus 3.29.4**: framework usado nos serviços REST e no Gateway.
- **Hibernate Reactive com Panache**: persistência reativa das entidades.
- **MySQL 8**: banco de dados relacional.
- **SmallRye JWT**: criação e validação de tokens de autenticação.
- **Docker Compose**: inicialização coordenada dos serviços e dependências no ambiente local.
- **Arquitetura**: cada serviço separa adaptadores de entrada/saída, casos de uso/portas e domínio; os modelos de domínio não dependem de Panache/JPA.

## Adequação à disciplina

A aplicação possui funcionalidades e fronteiras suficientes para planejar testes unitários de modelos, testes de componente das APIs e testes de sistema envolvendo cadastro, autenticação, autorização e persistência de despesas. Também permite integrar análise estática e medição de cobertura ao processo Maven.

---

**Acadêmico individual:** Verificação e Validação de Software · 12 de outubro de 2026

**Aluno:** Carlos Felipe Ramos Pires
