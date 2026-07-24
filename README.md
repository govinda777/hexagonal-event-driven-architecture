# 🏛️ Hexagonal Event-Driven Architecture (HEDA) - Java Specification

> **Guia de Referência Arquitetural e Template de Aplicação Backend em Java**

Este repositório documenta a especificação arquitetural padronizada para aplicações Java/Spring Boot reativas e baseadas em eventos. O objetivo principal desta arquitetura é **isolar rigorosamente as regras de negócio de frameworks, bancos de dados e detalhes de infraestrutura**, garantindo alta testabilidade, baixo acoplamento e resiliência em fluxos complexos de estado.

---

## 📌 Princípios Fundamentais

1. **Isolamento Total do Domínio (`business`):** O núcleo da aplicação é construído em Java puro. Nenhuma classe do pacote `business` pode importar ou depender de anotações/bibliotecas externas (Spring, JPA, AWS SDK, Jackson, etc.).
2. **Abolição da Herança:** Prioriza-se o uso estrito de **interfaces e composição** em vez de classes abstratas e herança de código, evitando acoplamento oculto e hierarquias complexas.
3. **Casos de Uso Granulares (*Single Responsibility*):** Cada operação de negócio é um arquivo/classe único em `usecase`. A nomenclatura da classe reflete exatamente a intenção funcional do sistema.
4. **Desacoplamento Cíclico via Facades:** Interações entre diferentes fluxos de negócio não possuem acoplamento direto; utilizam-se **Facades** na camada de `gateway` para orquestração.
5. **Máquina de Estados Guiada por Eventos:** Fluxos assíncronos e orquestrações por etapas (*steps*) utilizam mensagens (SQS/Kafka) e o pacote `logic` (padrões *Strategy/Chain of Responsibility*) para gerenciar a transição de estados.

---

## 📐 Visão da Arquitetura

O diagrama abaixo ilustra a separação das camadas da aplicação e a direção das dependências (a infraestrutura depende do negócio, nunca o contrário):

```text
+-----------------------------------------------------------------------------------+
|                                  INFRASTRUCTURE                                   |
|                                                                                   |
|  +-----------------------------------------------------------------------------+  |
|  | delivery / webapp                                                           |  |
|  |  [handlers]   [consumers]   [requests]   [responses]   [messages]           |  |
|  |  [middlewares]              [dependencies (Spring / Beans Config)]          |  |
|  +-----------------------------------------------------------------------------+  |
|                                         |                                         |
|      +----------------------------------+----------------------------------+      |
|      |                                  |                                  |      |
|      v                                  v                                  v      |
|  +---------------+              +---------------+              +---------------+  |
|  |  repository   |              |    service    |              |   publisher   |  |
|  |   [config]    |              |   [config]    |              |   [config]    |  |
|  +---------------+              +---------------+              +---------------+  |
+----------------------------------------|------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                                     BUSINESS                                      |
|                                                                                   |
|  +---------------+              +---------------+              +---------------+  |
|  |    gateway    | <----------- |    usecase    | -----------> |    domain     |  |
|  |  (interfaces) |              +---------------+              | (pure entity) |  |
|  +---------------+                      ^                      +---------------+  |
|          ^                              |                                         |
|          +-------------------------- [logic]                                      |
|                                (state transitions)                                |
+-----------------------------------------------------------------------------------+
