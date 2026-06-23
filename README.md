# PetFriends — Microsserviços (Prova)

Projeto Spring Boot com dois microsserviços — **PetFriends_Almoxarifado** e **PetFriends_Transporte** — que recebem eventos de domínio do **PetFriends_Pedidos** via **Apache Kafka**, com observabilidade via **Zipkin/Micrometer**.

## Visão geral da arquitetura

```
                 REST (síncrono)
  PetFriends_Web ───────────────► PetFriends_Clientes
   (ReactJS)     ───────────────► PetFriends_Produtos
                 ───────────────► PetFriends_Pedidos
                                        │
                                        │  Kafka (assíncrono, eventos de domínio)
                          ┌─────────────┴─────────────┐
                          ▼                           ▼
              PetFriends_Almoxarifado        PetFriends_Transporte
              (reserva estoque)              (cria remessa / transporte)
```

- A camada Web fala com Clientes/Produtos/Pedidos de forma **síncrona (REST)**.
- O Pedidos publica **eventos** que Almoxarifado e Transporte **consomem de forma assíncrona** (Kafka). É o que os Diagramas 2 e 3 mostram com as setas vermelhas.

## Como rodar

1. Suba a infraestrutura: `docker compose up -d` (Kafka em `localhost:9092`, Zipkin em `http://localhost:9411`).
2. Em cada serviço: `mvn spring-boot:run`.
   - Almoxarifado → porta `8083`
   - Transporte → porta `8084`
3. Os tópicos (`pedidos.pedido-fechado` e `pedidos.pedido-despachado`) são criados automaticamente. Você pode publicar eventos de teste com o `kafka-console-producer` ou a partir do serviço de Pedidos.

## Mapa: questão → arquivo

| Questão | Arquivo |
|---|---|
| 1.1 Entity + Repository (Almoxarifado) | `domain/ItemEstoque.java`, `domain/ItemEstoqueRepository.java` |
| 1.2 Value Object (Almoxarifado) | `domain/LocalizacaoArmazem.java` |
| 1.3 Entity + Repository (Transporte) | `domain/Remessa.java`, `domain/RemessaRepository.java` |
| 1.4 Value Object (Transporte) | `domain/EnderecoEntrega.java` |
| 3.1 Config Kafka (Almoxarifado) | `messaging/KafkaConsumerConfig.java` |
| 3.2 Serviço consumidor (Almoxarifado) | `messaging/PedidoEventConsumer.java` |
| 3.3 Config Kafka (Transporte) | `messaging/KafkaConsumerConfig.java` |
| 3.4 Serviço consumidor (Transporte) | `messaging/PedidoEventConsumer.java` |

---

# Respostas conceituais

## Questão 2 — Domain Events

**2.1 — Qual funcionalidade síncrona do cliente é afetada pelos eventos de domínio?**
A **consulta do status/situação do pedido**. O cliente lê o pedido de forma síncrona via REST, mas o *valor* desse status (Em Preparação, Em Trânsito, Entregue, Devolvido...) só muda quando os eventos assíncronos são processados por Almoxarifado e Transporte. Ou seja, a leitura é síncrona, mas o conteúdo exibido depende do processamento assíncrono dos eventos — há **consistência eventual**: por um curto intervalo o cliente pode ver um status ainda "antigo".

**2.2 — Diferença entre enviar só o ID do agregado vs. payload completo.**
- **Só o ID** (*thin event* / notificação): a mensagem é mínima; o consumidor precisa fazer uma chamada de volta (REST) ao Pedidos para buscar os detalhes. Vantagem: dado sempre fresco e contrato pequeno. Desvantagem: acopla os serviços em tempo de execução (se o Pedidos cair, o consumidor trava), gera mais tráfego e carga.
- **Payload completo** (*Event-Carried State Transfer* / *fat event*): o evento já carrega tudo que o consumidor precisa. Vantagem: consumidor **autônomo**, sem callback, melhor disponibilidade e desacoplamento. Desvantagem: mensagem maior, risco de dado **defasado** e maior acoplamento ao formato (schema) do evento.

**2.3 — Como projetar o evento Pedidos → Almoxarifado?**
Com **payload completo** (`PedidoFechadoEvent`), porque o almoxarifado precisa saber **quais itens e quantidades** separar — informação que ele não possui. Carregar isso no evento evita chamada de volta e mantém o serviço autônomo. Conteúdo: `pedidoId`, `clienteId` e a lista de itens (`sku`, `quantidade`).

**2.4 — Como projetar o evento Pedidos → Transporte?**
Também com **payload completo** (`PedidoDespachadoEvent`), pois a transportadora precisa de **endereço de entrega, destinatário e dados do pacote** para criar a remessa. Conteúdo: `pedidoId`, `clienteId`, `nomeDestinatario`, `enderecoEntrega` (logradouro, número, bairro, cidade, UF, CEP) e `pesoKg`.

## Questão 4 — Observabilidade

**4.1 — Gateway de Serviço.**
É um **ponto único de entrada** entre os clientes externos e os microsserviços. Ele roteia requisições e centraliza responsabilidades transversais (autenticação, rate limiting, SSL, roteamento, agregação).
- *Vantagens*: esconde a topologia interna; concentra segurança e políticas num só lugar; simplifica o cliente.
- *Desvantagens*: vira possível **ponto único de falha** e gargalo; adiciona um salto extra (latência); pode acumular lógica demais e virar um novo "monólito" de borda.

**4.2 — ID de Correlação.**
É um identificador único gerado na borda (no Gateway) e **propagado por toda a cadeia** de chamadas (síncronas e assíncronas), permitindo amarrar todos os logs/traces de uma mesma transação ponto a ponto.
- *Pré-requisitos*: (1) ser **gerado/injetado na entrada**; (2) ser **propagado** entre os serviços — em REST via header (ex.: `X-Correlation-Id`) e em mensageria via header da mensagem Kafka; (3) ser **registrado nos logs** de cada serviço (ex.: no MDC).

**4.3 — Função do Micrometer e relação com o Zipkin.**
O **Micrometer** é a biblioteca de **instrumentação/telemetria** da aplicação: coleta métricas e cria os *spans* de tracing distribuído (via Micrometer Tracing). Ele é a "fachada" que mede o que acontece. O **Zipkin** é o **backend de tracing** que recebe, armazena e exibe esses spans. Relação: o Micrometer **gera e exporta** os traces; o Zipkin os **coleta e visualiza** — juntos mostram o caminho completo de uma requisição entre os microsserviços.

**4.4 — Agregador de Logs.**
É uma solução que **centraliza os logs** de todos os microsserviços num único lugar pesquisável (ex.: ELK — Elasticsearch, Logstash, Kibana).
- *Vantagens*: busca e correlação em um só ponto (especialmente com ID de Correlação); visão unificada; dashboards e alertas.
- *Desvantagens*: infraestrutura adicional para operar e escalar; custo de armazenamento; risco de virar ponto único de falha; exige padronização do formato dos logs.
