package com.petfriends.almoxarifado.messaging;

import com.petfriends.almoxarifado.domain.ItemEstoque;
import com.petfriends.almoxarifado.domain.ItemEstoqueRepository;
import com.petfriends.almoxarifado.messaging.event.ItemPedido;
import com.petfriends.almoxarifado.messaging.event.ItemReservado;
import com.petfriends.almoxarifado.messaging.event.PedidoFechadoEvent;
import com.petfriends.almoxarifado.messaging.event.PedidoReservadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Consome PedidoFechadoEvent, reserva o estoque de cada item e, em seguida,
 * publica PedidoReservadoEvent com correlationId para o microsserviço de Transporte.
 *
 * O correlationId gerado aqui permite rastrear o pedido de ponta a ponta entre serviços.
 * Toda a operação de reserva é @Transactional: ou reserva tudo, ou nada (atomicidade).
 */
@Service
public class PedidoEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventConsumer.class);
    private static final String TOPICO_PEDIDO_RESERVADO = "almoxarifado.pedido-reservado";

    private final ItemEstoqueRepository itemEstoqueRepository;
    private final KafkaOperations<String, PedidoReservadoEvent> kafkaTemplate;

    public PedidoEventConsumer(ItemEstoqueRepository itemEstoqueRepository,
                               KafkaOperations<String, PedidoReservadoEvent> kafkaTemplate) {
        this.itemEstoqueRepository = itemEstoqueRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "pedidos.pedido-fechado", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void aoReceberPedidoFechado(PedidoFechadoEvent evento) {
        UUID correlationId = UUID.randomUUID();

        log.info("[correlationId={}] Pedido {} recebido para separação ({} itens)",
                correlationId, evento.pedidoId(), evento.itens().size());

        List<ItemReservado> itensReservados = new ArrayList<>();

        for (ItemPedido item : evento.itens()) {
            ItemEstoque itemEstoque = itemEstoqueRepository.findBySku(item.sku())
                    .orElseThrow(() -> new IllegalStateException("SKU não encontrado: " + item.sku()));

            itemEstoque.reservar(item.quantidade());
            itemEstoqueRepository.save(itemEstoque);

            String localizacao = itemEstoque.getLocalizacao() != null
                    ? itemEstoque.getLocalizacao().descricao() : "n/d";

            log.info("[correlationId={}] Reservadas {} unidade(s) do SKU {} (localização {})",
                    correlationId, item.quantidade(), item.sku(), localizacao);

            itensReservados.add(new ItemReservado(item.sku(), item.quantidade(), localizacao));
        }

        PedidoReservadoEvent eventoReservado = new PedidoReservadoEvent(
                correlationId,
                evento.pedidoId(),
                evento.clienteId(),
                evento.nomeCliente(),
                itensReservados,
                evento.enderecoEntrega(),
                evento.pesoKg(),
                Instant.now()
        );

        kafkaTemplate.send(TOPICO_PEDIDO_RESERVADO, evento.pedidoId().toString(), eventoReservado);

        log.info("[correlationId={}] PedidoReservadoEvent publicado em '{}' para o pedido {}",
                correlationId, TOPICO_PEDIDO_RESERVADO, evento.pedidoId());
    }
}
