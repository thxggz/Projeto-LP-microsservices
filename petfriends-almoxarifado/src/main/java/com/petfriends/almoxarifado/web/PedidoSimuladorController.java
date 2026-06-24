package com.petfriends.almoxarifado.web;

import com.petfriends.almoxarifado.messaging.event.EnderecoEntregaDto;
import com.petfriends.almoxarifado.messaging.event.ItemPedido;
import com.petfriends.almoxarifado.messaging.event.PedidoFechadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoint de simulação: publica um PedidoFechadoEvent no Kafka
 * para disparar o fluxo almoxarifado → transporte sem precisar
 * de um serviço de pedidos real.
 */
@RestController
@RequestMapping("/pedidos")
public class PedidoSimuladorController {

    private static final Logger log = LoggerFactory.getLogger(PedidoSimuladorController.class);
    private static final String TOPICO = "pedidos.pedido-fechado";

    private final KafkaTemplate<String, PedidoFechadoEvent> kafkaTemplate;

    public PedidoSimuladorController(KafkaTemplate<String, PedidoFechadoEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public record PedidoRequest(
            String nomeCliente,
            List<ItemPedido> itens,
            EnderecoEntregaDto enderecoEntrega,
            double pesoKg
    ) {}

    @PostMapping("/simular")
    public ResponseEntity<Map<String, Object>> simular(@RequestBody PedidoRequest request) {
        UUID pedidoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        PedidoFechadoEvent evento = new PedidoFechadoEvent(
                pedidoId,
                clienteId,
                request.nomeCliente(),
                request.itens(),
                request.enderecoEntrega(),
                request.pesoKg()
        );

        kafkaTemplate.send(TOPICO, pedidoId.toString(), evento);

        log.info("Pedido {} publicado no tópico '{}'", pedidoId, TOPICO);

        return ResponseEntity.ok(Map.of(
                "pedidoId", pedidoId,
                "clienteId", clienteId,
                "nomeCliente", request.nomeCliente(),
                "mensagem", "Pedido enviado ao Kafka. Em segundos consulte /itens-estoque e /remessas."
        ));
    }
}
