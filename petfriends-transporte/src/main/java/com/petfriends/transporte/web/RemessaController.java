package com.petfriends.transporte.web;

import com.petfriends.transporte.domain.Remessa;
import com.petfriends.transporte.domain.RemessaRepository;
import com.petfriends.transporte.messaging.event.EntregaConfirmadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/remessas")
public class RemessaController {

    private static final Logger log = LoggerFactory.getLogger(RemessaController.class);
    private static final String TOPICO_ENTREGA_CONFIRMADA = "transporte.entrega-confirmada";

    private final RemessaRepository repository;
    private final KafkaTemplate<String, EntregaConfirmadaEvent> kafkaTemplate;

    public RemessaController(RemessaRepository repository,
                             KafkaTemplate<String, EntregaConfirmadaEvent> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // GET /remessas -> lista todas as remessas
    @GetMapping
    public List<Remessa> listar() {
        return repository.findAll();
    }

    // POST /remessas/{codigoRastreio}/confirmar-entrega -> EM_TRANSITO -> ENTREGUE
    @PostMapping("/{codigoRastreio}/confirmar-entrega")
    public ResponseEntity<?> confirmarEntrega(@PathVariable String codigoRastreio) {
        return repository.findByCodigoRastreio(codigoRastreio)
                .<ResponseEntity<?>>map(remessa -> {
                    remessa.confirmarEntrega();
                    repository.save(remessa);

                    EntregaConfirmadaEvent evento = new EntregaConfirmadaEvent(
                            remessa.getCorrelationId(),
                            remessa.getPedidoId(),
                            remessa.getCodigoRastreio()
                    );
                    kafkaTemplate.send(TOPICO_ENTREGA_CONFIRMADA, remessa.getPedidoId().toString(), evento);
                    log.info("[correlationId={}] EntregaConfirmadaEvent publicado para o pedido {}",
                            remessa.getCorrelationId(), remessa.getPedidoId());

                    return ResponseEntity.ok(Map.of(
                            "codigoRastreio", remessa.getCodigoRastreio(),
                            "status", remessa.getStatus().name(),
                            "mensagem", "Entrega confirmada com sucesso."
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of("erro", "Remessa nao encontrada: " + codigoRastreio)));
    }
}