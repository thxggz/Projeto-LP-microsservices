package com.petfriends.transporte.messaging;

import com.petfriends.transporte.domain.EnderecoEntrega;
import com.petfriends.transporte.domain.Remessa;
import com.petfriends.transporte.domain.RemessaRepository;
import com.petfriends.transporte.messaging.event.EnderecoEntregaDto;
import com.petfriends.transporte.messaging.event.PedidoDespachadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * QUESTÃO 3.4 — Serviço que CONSOME os eventos do PetFriends_Pedidos.
 *
 * Ao receber um PedidoDespachadoEvent, a transportadora cria uma Remessa nova
 * (idempotente: se já existir remessa para o pedido, ignora o evento repetido).
 */
@Service
public class PedidoEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventConsumer.class);

    private final RemessaRepository remessaRepository;

    public PedidoEventConsumer(RemessaRepository remessaRepository) {
        this.remessaRepository = remessaRepository;
    }

    @KafkaListener(topics = "pedidos.pedido-despachado", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void aoReceberPedidoDespachado(PedidoDespachadoEvent evento) {
        log.info("Pedido {} recebido para transporte", evento.pedidoId());

        // Idempotência: não cria remessa duplicada se o evento for reentregue
        if (remessaRepository.findByPedidoId(evento.pedidoId()).isPresent()) {
            log.warn("Remessa já existe para o pedido {} — evento ignorado", evento.pedidoId());
            return;
        }

        EnderecoEntregaDto dto = evento.enderecoEntrega();
        EnderecoEntrega endereco = new EnderecoEntrega(
                dto.logradouro(), dto.numero(), dto.complemento(),
                dto.bairro(), dto.cidade(), dto.estado(), dto.cep());

        Remessa remessa = new Remessa(
                UUID.randomUUID(),
                UUID.randomUUID(),
                evento.pedidoId(),
                endereco,
                gerarCodigoRastreio());

        remessa.despachar(); // já entra em trânsito (Diagrama 1: Em Trânsito)
        remessaRepository.save(remessa);

        log.info("Remessa {} criada para o pedido {} -> {}",
                remessa.getCodigoRastreio(), evento.pedidoId(), endereco.enderecoFormatado());
    }

    private String gerarCodigoRastreio() {
        return "PF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "BR";
    }
}
