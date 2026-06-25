package com.petfriends.pedido.web;

import com.petfriends.pedido.domain.EnderecoEntrega;
import com.petfriends.pedido.domain.ItemPedido;
import com.petfriends.pedido.domain.Pedido;
import com.petfriends.pedido.domain.PedidoRepository;
import com.petfriends.pedido.messaging.event.EnderecoEntregaDto;
import com.petfriends.pedido.messaging.event.ItemPedidoEvent;
import com.petfriends.pedido.messaging.event.PedidoFechadoEvent;
import com.petfriends.pedido.web.dto.CriarPedidoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private static final Logger log = LoggerFactory.getLogger(PedidoController.class);
    private static final String TOPICO = "pedidos.pedido-fechado";

    private final PedidoRepository pedidoRepository;
    private final KafkaTemplate<String, PedidoFechadoEvent> kafkaTemplate;

    public PedidoController(PedidoRepository pedidoRepository,
                            KafkaTemplate<String, PedidoFechadoEvent> kafkaTemplate) {
        this.pedidoRepository = pedidoRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> criarPedido(@RequestBody CriarPedidoRequest request) {
        UUID correlationId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        List<ItemPedido> itens = request.itens().stream()
                .map(i -> new ItemPedido(i.sku(), i.quantidade()))
                .toList();

        EnderecoEntrega endereco = new EnderecoEntrega(
                request.enderecoEntrega().logradouro(),
                request.enderecoEntrega().numero(),
                request.enderecoEntrega().complemento(),
                request.enderecoEntrega().bairro(),
                request.enderecoEntrega().cidade(),
                request.enderecoEntrega().estado(),
                request.enderecoEntrega().cep()
        );

        Pedido pedido = new Pedido(pedidoId, correlationId, clienteId, request.nomeCliente(), itens, endereco, request.pesoKg());
        pedidoRepository.save(pedido);

        List<ItemPedidoEvent> itensEvento = request.itens().stream()
                .map(i -> new ItemPedidoEvent(i.sku(), i.quantidade()))
                .toList();

        EnderecoEntregaDto enderecoEvento = new EnderecoEntregaDto(
                request.enderecoEntrega().logradouro(),
                request.enderecoEntrega().numero(),
                request.enderecoEntrega().complemento(),
                request.enderecoEntrega().bairro(),
                request.enderecoEntrega().cidade(),
                request.enderecoEntrega().estado(),
                request.enderecoEntrega().cep()
        );

        PedidoFechadoEvent evento = new PedidoFechadoEvent(
                correlationId, pedidoId, clienteId, request.nomeCliente(), itensEvento, enderecoEvento, request.pesoKg()
        );

        kafkaTemplate.send(TOPICO, pedidoId.toString(), evento);

        log.info("[correlationId={}] Pedido {} criado e publicado no tópico '{}'", correlationId, pedidoId, TOPICO);

        return ResponseEntity.ok(Map.of(
                "correlationId", correlationId,
                "pedidoId", pedidoId,
                "clienteId", clienteId,
                "status", "ABERTO",
                "mensagem", "Pedido criado. Consulte  /pedidos/" + pedidoId + " para acompanhar o status."
        ));
    }

    @GetMapping("/{correlationId}")
    public ResponseEntity<?> buscarPedido(@PathVariable UUID correlationId) {
        return pedidoRepository.findByCorrelationId(correlationId)
                .map(p -> {
                    var body = new java.util.LinkedHashMap<String, Object>();
                    body.put("correlationId", p.getCorrelationId());
                    body.put("pedidoId", p.getId());
                    body.put("clienteId", p.getClienteId());
                    body.put("nomeCliente", p.getNomeCliente());
                    body.put("status", p.getStatus());
                    body.put("criadoEm", p.getCriadoEm());
                    body.put("itens", p.getItens().stream()
                            .map(i -> Map.of("sku", i.getSku(), "quantidade", i.getQuantidade()))
                            .toList());
                    if (p.getMotivoRejeicao() != null) {
                        body.put("motivoRejeicao", p.getMotivoRejeicao());
                    }
                    return ResponseEntity.ok(body);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
