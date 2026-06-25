package com.petfriends.pedido.messaging;

import com.petfriends.pedido.domain.PedidoRepository;
import com.petfriends.pedido.messaging.event.EntregaConfirmadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntregaConfirmadaConsumer {

    private static final Logger log = LoggerFactory.getLogger(EntregaConfirmadaConsumer.class);

    private final PedidoRepository pedidoRepository;

    public EntregaConfirmadaConsumer(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @KafkaListener(
            topics = "transporte.entrega-confirmada",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "entregaConfirmadaListenerContainerFactory"
    )
    @Transactional
    public void aoReceberEntregaConfirmada(EntregaConfirmadaEvent evento) {
        if (evento == null || evento.getPedidoId() == null) return;

        pedidoRepository.findById(evento.getPedidoId()).ifPresentOrElse(
                pedido -> {
                    pedido.marcarComoEntregue();
                    pedidoRepository.save(pedido);
                    log.info("[correlationId={}] Pedido {} marcado como ENTREGUE. Rastreio: {}",
                            evento.getCorrelationId(), evento.getPedidoId(), evento.getCodigoRastreio());
                },
                () -> log.warn("[correlationId={}] Pedido {} não encontrado para marcar como entregue",
                        evento.getCorrelationId(), evento.getPedidoId())
        );
    }
}
