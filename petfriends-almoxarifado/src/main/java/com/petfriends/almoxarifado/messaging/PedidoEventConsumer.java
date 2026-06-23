package com.petfriends.almoxarifado.messaging;

import com.petfriends.almoxarifado.domain.ItemEstoque;
import com.petfriends.almoxarifado.domain.ItemEstoqueRepository;
import com.petfriends.almoxarifado.messaging.event.ItemPedido;
import com.petfriends.almoxarifado.messaging.event.PedidoFechadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * QUESTÃO 3.2 — Serviço que CONSOME os eventos do PetFriends_Pedidos.
 *
 * Ao receber um PedidoFechadoEvent, o almoxarifado reserva o estoque de cada
 no caso ela pega os dados recebidos em json e transforma em objeto
 * item. Toda a operação é @Transactional: ou reserva tudo, ou nada (atomicidade).
 */
@Service
public class PedidoEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventConsumer.class);

    private final ItemEstoqueRepository itemEstoqueRepository;

    public PedidoEventConsumer(ItemEstoqueRepository itemEstoqueRepository) {
        this.itemEstoqueRepository = itemEstoqueRepository;
    }

    @KafkaListener(topics = "pedidos.pedido-fechado", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void aoReceberPedidoFechado(PedidoFechadoEvent evento) {
        log.info("Pedido {} recebido para separação ({} itens)",
                evento.pedidoId(), evento.itens().size());

        for (ItemPedido item : evento.itens()) {
            ItemEstoque itemEstoque = itemEstoqueRepository.findBySku(item.sku())
                    .orElseThrow(() -> new IllegalStateException("SKU não encontrado: " + item.sku()));

            itemEstoque.reservar(item.quantidade());
            itemEstoqueRepository.save(itemEstoque);

            log.info("Reservadas {} unidade(s) do SKU {} (localização {})",
                    item.quantidade(), item.sku(),
                    itemEstoque.getLocalizacao() != null
                            ? itemEstoque.getLocalizacao().descricao() : "n/d");
        }
    }
}
