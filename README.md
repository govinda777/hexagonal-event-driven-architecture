# 🏛️ Hexagonal Event-Driven Architecture (HEDA) - Order Execution Flow

Este projeto é uma aplicação completa e funcional desenvolvida em **Java 21**, **Spring Boot 3.2.5** e baseada em **Arquitetura Limpa/Hexagonal**. Ele implementa uma máquina de estados robusta orientada a eventos, inspirada no padrão de orquestração de larga escala do **Netflix Conductor** (usando a raiz conceitual `com.empresa.execution`).

O domínio funcional escolhido é o **Processamento de Pedidos de E-Commerce (Order Flow)**, onde os pedidos transitam por múltiplos estados de forma assíncrona utilizando filas **AWS SQS** para orquestração e transição segura de estados.

---

## 📐 Princípios Arquiteturais e Design do Projeto

A aplicação segue rigorosamente as seguintes diretrizes de arquitetura:

1. **Isolamento de Domínio (`business`):** O pacote `business` é o núcleo do sistema e foi mantido em **Java Puro**. Não há dependências de frameworks externos (como Spring, JPA/Hibernate, Jackson, AWS SDK, etc.). Toda interação externa é abstraída por interfaces no pacote `gateway`.
2. **Abolição da Herança:** Priorização estrita de **interfaces e composição** em vez de classes abstratas ou herança de classes, reduzindo o acoplamento oculto.
3. **Injeção de Dependência Sem Poluição:** Os Casos de Uso e as estratégias de negócio em `logic` são POJOs puros e não contêm anotações como `@Component` ou `@Service`. Seus Beans são registrados programaticamente na infraestrutura em `UseCaseConfig.java`.
4. **Mapeamento de Entidades:** A entidade de domínio (`OrderEntity`) é isolada da entidade de persistência (`OrderJpaEntity`). O adaptador `OrderRepositoryAdapter` realiza o mapeamento bidirecional.
5. **Granularidade (Single Responsibility):** Cada operação única de negócio é isolada em um Caso de Uso independente (`CreateOrderUseCase`, `ExecuteOrderStepUseCase`, `GetOrderByIdUseCase`).

---

## 📂 Estrutura de Pacotes

A organização das pastas respeita fielmente o diagrama arquitetural proposto:

```text
src/main/java/com/empresa/execution/
├── business/                               # Core de Negócio (Java Puro)
│   ├── domain/                             # Entidades e Enums puros do Domínio
│   │   ├── OrderEntity.java                # Estado e histórico do pedido
│   │   └── OrderStatus.java                # Ciclo de Vida do Pedido
│   ├── gateway/                            # Interfaces de comunicação (Contratos)
│   │   ├── ProcessPublisherGateway.java    # Contrato para publicação SQS
│   │   └── ProcessRepositoryGateway.java   # Contrato para persistência do pedido
│   ├── logic/                              # Estratégias e Regras de Transição de Estado (Strategy)
│   │   ├── StepLogic.java                  # Contrato base para lógicas de etapa
│   │   ├── ProcessPaymentLogic.java        # Etapa 1: CREATED -> PAYMENT_APPROVED
│   │   └── ReserveInventoryLogic.java      # Etapa 2: PAYMENT_APPROVED -> COMPLETED
│   └── usecase/                            # Casos de Uso (Operações unitárias)
│       ├── CreateOrderUseCase.java         # Inicializa o pedido e envia o 1º evento SQS
│       ├── ExecuteOrderStepUseCase.java    # Executa a transição de estado da fila
│       └── GetOrderByIdUseCase.java        # Consulta de pedidos
│
└── infrastructure/                         # Infraestrutura (Detalhes de Frameworks)
    ├── delivery/webapp/
    │   ├── consumers/                      # Consumidores SQS (Spring Cloud AWS)
    │   │   └── OrderSqsConsumer.java       # Escuta mensagens e ativa a máquina de estados
    │   ├── dependencies/                   # Injeção de Beans de Negócio
    │   │   └── UseCaseConfig.java          # Declaração programática dos Casos de Uso
    │   ├── handlers/                       # Controllers / REST Entrypoints
    │   │   └── OrderHandler.java           # Endpoints POST e GET
    │   ├── messages/                       # DTOs de Mensageria (SQS JSON Payload)
    │   │   └── OrderMessage.java
    │   ├── middlewares/                    # Tratamento de Exceções Global
    │   │   └── GlobalExceptionHandler.java  # Retorna erros HTTP limpos e padronizados
    │   ├── requests/                       # DTOs de Entrada HTTP
    │   │   └── CreateOrderRequest.java
    │   └── responses/                      # DTOs de Saída HTTP
    │       └── OrderResponse.java
    ├── publisher/                          # Publicação na AWS SQS
    │   └── SqsPublisherAdapter.java        # Envia transições para a fila
    └── repository/                         # Persistência com Banco de Dados H2
        ├── OrderJpaEntity.java             # Entidade anotada com JPA
        ├── SpringDataOrderRepository.java  # Interface Spring Data JPA
        └── OrderRepositoryAdapter.java     # Implementa o contrato de persistência
```

---

## 🔄 Fluxo de Estados e Orquestração Orientada a Eventos

O ciclo de vida do pedido passa sequencialmente pelas seguintes transições de estado:

```text
 [ Cliente ]
     │  (POST /orders)
     ▼
 ┌───────────────┐
 │    CREATED    │  ──► [ Publica Evento SQS ]
 └───────────────┘
         │
         ▼  (Consumidor SQS captura)
 ┌───────────────┐
 │PAYMENT_APPROVED│  ──► [ Publica Evento SQS ]
 └───────────────┘
         │
         ▼  (Consumidor SQS captura)
 ┌───────────────┐
 │   COMPLETED   │  ──► [ Fluxo Encerrado. Não publica SQS ]
 └───────────────┘
```

Se ocorrer alguma falha de negócio ou exceção técnica durante a transição de estados, o status é alterado para `FAILED` e o histórico do erro é anexado ao `historyLog` da entidade, interrompendo o ciclo de mensageria de forma resiliente para evitar loops infinitos.

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
*   **Java 21** instalado.
*   **Maven 3.x** instalado.

### 1. Compilar e Executar Testes
Para garantir a conformidade arquitetural e testar todos os cenários (lógicas, uso dos gateways, controladores e deserializações), execute:
```bash
mvn clean test
```

### 2. Executar a Aplicação
Como o banco de dados (H2 em memória) e o Spring Cloud AWS SQS estão configurados com mocks de teste por padrão para execução instantânea, você pode subir o servidor Spring Boot localmente na porta `8080` com o seguinte comando:
```bash
mvn spring-boot:run
```

---

## 📬 Documentação da API REST

Abaixo estão os comandos `curl` necessários para interagir com a aplicação:

### 1. Criar um Pedido (`POST /orders`)
Cria um pedido com status inicial `CREATED` e publica a primeira mensagem no SQS para dar início ao processamento assíncrono.

**Requisição:**
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUSTOMER-ABC-123",
    "totalAmount": 149.90
  }'
```

**Resposta de Sucesso (201 Created):**
```json
{
  "id": "e4f0a28b-b6fb-4560-b08e-8a7c29e47264",
  "customerId": "CUSTOMER-ABC-123",
  "totalAmount": 149.90,
  "status": "CREATED",
  "historyLog": "[CREATED] Order initialized",
  "createdAt": "2026-07-24T23:00:00.123456",
  "updatedAt": "2026-07-24T23:00:00.123456"
}
```

---

### 2. Consultar o Status do Pedido (`GET /orders/{id}`)
Permite acompanhar em tempo real o avanço assíncrono do pedido pelas etapas da máquina de estados.

**Requisição:**
```bash
curl -X GET http://localhost:8080/orders/e4f0a28b-b6fb-4560-b08e-8a7c29e47264
```

**Resposta de Sucesso (200 OK) - Após processamento completo:**
```json
{
  "id": "e4f0a28b-b6fb-4560-b08e-8a7c29e47264",
  "customerId": "CUSTOMER-ABC-123",
  "totalAmount": 149.90,
  "status": "COMPLETED",
  "historyLog": "[CREATED] Order initialized\n[PAYMENT] Approved at 2026-07-24T23:00:01.456 - AuthCode: PAY-9982\n[INVENTORY] Reserved items successfully at 2026-07-24T23:00:02.789",
  "createdAt": "2026-07-24T23:00:00.123456",
  "updatedAt": "2026-07-24T23:00:02.789"
}
```

---

## ⚙️ Esquema das Mensagens SQS (`OrderMessage`)

As mensagens trafegadas no AWS SQS utilizam o seguinte formato de payload JSON estruturado:

```json
{
  "orderId": "e4f0a28b-b6fb-4560-b08e-8a7c29e47264",
  "currentStatus": "CREATED",
  "eventTimestamp": "2026-07-24T23:00:00.123456"
}
```

---

## 🛡️ Tratamento de Erros e Validações

O sistema conta com um `GlobalExceptionHandler` robusto que intercepta e formata de maneira amigável os erros de entrada:

- **Campos nulos ou negativos:** Retorna `400 Bad Request` detalhando cada campo inconsistente.
- **Transições inválidas ou IDs não localizados:** Retorna `400 Bad Request` com explicação amigável.
