package com.petfriends.pedido.messaging;

import com.petfriends.pedido.domain.Pedido;
import com.petfriends.pedido.domain.PedidoRepository;
import com.petfriends.pedido.messaging.event.PedidoReservadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoReservadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoReservadoConsumer.class);

    private final PedidoRepository pedidoRepository;

    public PedidoReservadoConsumer(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @KafkaListener(topics = "almoxarifado.pedido-reservado", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void aoReceberPedidoReservado(PedidoReservadoEvent evento) {
        if (evento == null || evento.getPedidoId() == null) return;

        pedidoRepository.findById(evento.getPedidoId()).ifPresentOrElse(
                pedido -> {
                    pedido.marcarComoReservado();
                    pedidoRepository.save(pedido);
                    log.info("[correlationId={}] Pedido {} atualizado para RESERVADO — fluxo completo: pedido → almoxarifado → transporte",
                            evento.getCorrelationId(), evento.getPedidoId());
                },
                () -> log.warn("[correlationId={}] Pedido {} não encontrado para atualizar status",
                        evento.getCorrelationId(), evento.getPedidoId())
        );
    }
}
