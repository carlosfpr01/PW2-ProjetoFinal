# Plano de Teste — Sistema de Gerenciamento de Gastos

**Versão:** 1.0  
**Data:** 12/10/2026  
**Projeto:** sistema de gerenciamento de usuários e gastos com API Gateway

## 1. Identificação do projeto

O sistema é composto por três aplicações Java independentes: o API Gateway recebe as requisições HTTP e encaminha chamadas; Users oferece cadastro, autenticação e manutenção de usuários; Gastos registra e consulta despesas e receitas. Os serviços usam JWT para autenticação e MySQL para persistência. O ambiente local é orquestrado pelo Docker Compose.

- **Linguagem:** Java 25 (LTS), padronizado nos três serviços.
- **Framework principal:** Quarkus 3.29.4.
- **Persistência:** Hibernate Reactive com Panache e cliente reativo MySQL.
- **Testes:** JUnit 5, Quarkus Test e REST Assured.
- **Análise estática:** PMD.
- **Cobertura:** JaCoCo.
- **Teste de sistema:** script Bash com `curl` e `jq`, exercitando a API publicada pelo Gateway.

## 2. Escopo

### Dentro do escopo

- Resposta de saúde do Gateway e disponibilidade das rotas HTTP.
- Modelo de dados de usuário e despesa: construção, acesso a atributos e tipos de operação.
- Respostas HTTP de autenticação e autorização, incluindo acesso sem token.
- Cadastro de usuário, login, emissão e uso de JWT.
- Criação e consulta de despesas autenticadas, persistência e isolamento por usuário.
- Configuração reproduzível de análise estática e cobertura.

### Fora do escopo

- Testes de carga, desempenho e capacidade.
- Testes de interface gráfica: o projeto disponibiliza APIs REST, não uma interface web para o usuário.
- Alta disponibilidade, recuperação de desastre e operação em ambiente de produção.
- Serviços externos de observabilidade (Jaeger e Graylog), exceto sua configuração de execução local.
- Auditoria abrangente de segurança ou certificação de conformidade.

## 3. Estratégia de teste

| Nível | Abordagem | Evidência |
|---|---|---|
| Estático | PMD verifica código de produção e testes durante o ciclo Maven `verify`; violações são reportadas para análise. | Relatório PMD em `target/site/pmd.html` em cada módulo. |
| Unitário | JUnit 5 testa comportamento determinístico dos modelos, sem iniciar HTTP ou banco de dados. | Testes em `src/test/java` e relatório JaCoCo. |
| Componente | `@QuarkusTest` e REST Assured iniciam o Gateway e verificam o endpoint de saúde e a rejeição de chamada protegida sem credenciais. | Testes do módulo `gateway` executados por `mvn test`. |
| Sistema | Script de ponta a ponta chama a API real pelo Gateway, cria usuário, autentica, cria uma despesa, consulta sua persistência e remove os dados de teste. | `tests/system/api-system-test.sh` e saída do terminal. |

### Jornadas críticas

1. Consultar o health check do Gateway e receber HTTP 200 com estado `UP`.
2. Fazer chamada à operação protegida de gastos sem token e receber HTTP 401 ou 403.
3. Criar usuário, autenticar e receber um JWT válido.
4. Criar uma despesa autenticada e confirmar sua presença na listagem do mesmo usuário.
5. Excluir os dados de teste ao final da execução, mesmo em caso de falha.

## 4. Critérios de entrada e saída

### Entrada

- Código e dependências disponíveis nos três módulos Maven.
- JDK 25 e Maven instalados.
- Para testes de componente: dependências Maven disponíveis e configuração Quarkus funcional.
- Para testes de sistema: Docker e Docker Compose funcionais; stack iniciada e Gateway, Users, Gastos e MySQL saudáveis; `curl` e `jq` instalados.
- Dados de teste identificados por e-mail único, para evitar colisão com usuários existentes.

### Saída

- Testes unitários e de componente terminam com código de saída zero.
- Teste de sistema completa cadastro, login, criação e leitura da despesa; asserções HTTP passam e limpeza é executada.
- PMD produz relatório para cada módulo; violações encontradas são revisadas e documentadas. A configuração inicial não bloqueia o build, permitindo avaliar o baseline sem ocultar violações.
- Relatório JaCoCo é gerado quando os testes Maven são executados.
- Falhas, limitações de ambiente e resultados não executados são explicitamente registrados; não se declara aprovação para etapas que não foram executadas.

## 5. Ambiente e ferramentas

- Java/JDK 25; Maven Wrapper configurado nos três módulos.
- Quarkus 3.29.4, JUnit 5 e REST Assured fornecidos pelas dependências Maven.
- PMD Maven Plugin 3.26.0 e JaCoCo Maven Plugin 0.8.13.
- Docker Engine e Docker Compose para MySQL e execução integrada da stack.
- Bash, `curl` e `jq` para o teste de sistema.
- Sistema operacional Linux; o teste de sistema pode ser adaptado para outro ambiente com Bash compatível.

## 6. Execução

Cada aplicação é um projeto Maven independente. Para executar análise estática,
testes e cobertura somente em um módulo, use o wrapper correspondente:

```bash
cd gateway && ./mvnw verify
```

```bash
cd users && ./mvnw verify
```

```bash
cd gastos && ./mvnw verify
```

Para iniciar a stack antes do teste de sistema:

```bash
./start-services.sh -d
./tests/system/api-system-test.sh
```

Os relatórios PMD e JaCoCo são encontrados em `target/site/pmd.html` e `target/site/jacoco/index.html` em cada módulo. Para encerrar o ambiente Compose:

```bash
docker compose down
```

O teste de sistema não inicia nem encerra a stack automaticamente, evitando parar serviços que já estejam sendo usados por outra pessoa.

## 7. Riscos e mitigação

| Risco | Impacto | Mitigação |
|---|---|---|
| Docker indisponível ou banco não saudável | Teste de sistema não executável. | Verificar `docker info` e health checks antes da execução; registrar como não executado, sem substituir por resultado de mock. |
| Código dos serviços e contratos HTTP divergentes | Falha de integração ou dados inesperados. | Testar pelo Gateway e usar os parâmetros/headers realmente expostos pelos recursos REST. |
| Colisão com dados persistidos | Resultados instáveis ou exclusão de dados alheios. | Gerar e-mail de teste único e excluir apenas recursos criados pela execução. |
| PMD encontra violações preexistentes | Relatório ruidoso ou build bloqueado. | Não bloquear o build no baseline; registrar e priorizar violações em etapa posterior. |
| Segredos e chaves locais ausentes/inválidos | Aplicações ou autenticação não iniciam. | Confirmar arquivos de configuração/chaves necessários sem publicar valores secretos nos relatórios. |
| Flutuação de tempo de inicialização de Quarkus e MySQL | Falsas falhas de disponibilidade. | Usar health checks e tentativas limitadas com intervalo e timeout no script de sistema. |

## 8. Cronograma proposto

| Atividade | Duração estimada | Resultado |
|---|---:|---|
| Preparar ambiente e verificar build | 0,5 dia | JDK, Maven e dependências conferidos. |
| Configurar PMD e JaCoCo; avaliar baseline | 0,5 dia | Relatórios estático e de cobertura reproduzíveis. |
| Implementar e executar testes unitários e de componente | 1 dia | Testes JUnit/REST Assured verdes ou falhas documentadas. |
| Implementar e executar jornada de sistema | 1 dia | Evidência ponta a ponta pela API Gateway. |
| Consolidar evidências e revisar plano | 0,5 dia | Artefatos finais e limitações registradas. |

## 9. Evidências a entregar

- Este plano em `docs/plano-de-teste.md`.
- Definição do projeto em PDF: `docs/definicao-do-projeto.pdf`.
- Configuração PMD em `config/pmd-ruleset.xml` e integração Maven nos três módulos.
- Testes JUnit/REST Assured em `src/test/java` nos módulos correspondentes.
- Teste de sistema em `tests/system/api-system-test.sh`.
- Relatórios reproduzíveis localmente por `mvn verify`; arquivos gerados em `target/` não precisam ser versionados.

## 10. Observação sobre arquitetura

Os três serviços usam o namespace `br.com.ifrs` e separam adaptadores de entrada,
casos de uso/portas, domínio e adaptadores de saída. Os serviços Users e Gastos
mantêm os modelos de domínio independentes das entidades Panache; o Gateway
trata REST como adaptador de entrada e saída.
