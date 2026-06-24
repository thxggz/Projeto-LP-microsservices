package com.petfriends.transporte.web;

import com.petfriends.transporte.domain.Remessa;
import com.petfriends.transporte.domain.RemessaRepository;
import org.springframework.http.ResponseEntity;
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

    private final RemessaRepository repository;

    public RemessaController(RemessaRepository repository) {
        this.repository = repository;
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