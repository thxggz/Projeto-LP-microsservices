package com.petfriends.transporte.messaging;

import com.petfriends.transporte.domain.EnderecoEntrega;
import com.petfriends.transporte.domain.Remessa;
import com.petfriends.transporte.domain.RemessaRepository;
import com.petfriends.transporte.messaging.event.EnderecoEntregaDto;
import com.petfriends.transporte.messaging.event.PedidoReservadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Consome PedidoReservadoEvent publicado pelo Almoxarifado após a reserva do estoque.
 * Cria uma Remessa e a despacha imediatamente para transporte.
 *
 * O correlationId propagado no evento permite rastrear o pedido de ponta a ponta.
 * Idempotente: ignora eventos duplicados se já existir remessa para o mesmo pedido.
 */
@Service
public class PedidoReservadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoReservadoConsumer.class);

    private final RemessaRepository remessaRepository;

    public PedidoReservadoConsumer(RemessaRepository remessaRepository) {
        this.remessaRepository = remessaRepository;
    }

    @KafkaListener(
            topics = "almoxarifado.pedido-reservado",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "pedidoReservadoContainerFactory"
    )
    @Transactional
    public void aoReceberPedidoReservado(PedidoReservadoEvent evento) {
        log.info("[correlationId={}] Pedido {} reservado no almoxarifado recebido pelo Transporte",
                evento.correlationId(), evento.pedidoId());

        if (remessaRepository.findByPedidoId(evento.pedidoId()).isPresent()) {
            log.warn("[correlationId={}] Remessa já existe para o pedido {} — evento ignorado",
                    evento.correlationId(), evento.pedidoId());
            return;
        }

        EnderecoEntregaDto dto = evento.enderecoEntrega();
        EnderecoEntrega endereco = new EnderecoEntrega(
                dto.logradouro(), dto.numero(), dto.complemento(),
                dto.bairro(), dto.cidade(), dto.estado(), dto.cep());

        Remessa remessa = new Remessa(
                UUID.randomUUID(),
                evento.correlationId(),
                evento.pedidoId(),
                endereco,
                gerarCodigoRastreio());

        remessa.despachar();
        remessaRepository.save(remessa);

        log.info("[correlationId={}] Remessa {} criada e despachada para o pedido {} -> {}",
                evento.correlationId(), remessa.getCodigoRastreio(),
                evento.pedidoId(), endereco.enderecoFormatado());
    }

    private String gerarCodigoRastreio() {
        return "PF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "BR";
    }
}
