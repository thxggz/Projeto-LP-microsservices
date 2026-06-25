package com.petfriends.pedido.messaging;

import com.petfriends.pedido.domain.PedidoRepository;
import com.petfriends.pedido.messaging.event.PedidoRejeitadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoRejeitadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoRejeitadoConsumer.class);

    private final PedidoRepository pedidoRepository;

    public PedidoRejeitadoConsumer(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @KafkaListener(
            topics = "almoxarifado.pedido-rejeitado",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "rejeitadoListenerContainerFactory"
    )
    @Transactional
    public void aoReceberPedidoRejeitado(PedidoRejeitadoEvent evento) {
        if (evento == null || evento.getPedidoId() == null) return;

        pedidoRepository.findById(evento.getPedidoId()).ifPresentOrElse(
                pedido -> {
                    pedido.marcarComoRejeitado(evento.getMotivo());
                    pedidoRepository.save(pedido);
                    log.warn("[correlationId={}] Pedido {} marcado como REJEITADO. Motivo: {}",
                            evento.getCorrelationId(), evento.getPedidoId(), evento.getMotivo());
                },
                () -> log.warn("[correlationId={}] Pedido {} não encontrado para marcar como rejeitado",
                        evento.getCorrelationId(), evento.getPedidoId())
        );
    }
}
