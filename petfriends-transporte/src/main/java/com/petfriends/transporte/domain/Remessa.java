package com.petfriends.transporte.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * QUESTÃO 1.3 — Agregado mais representativo do PetFriends_Transporte.
REMESSA (o envio físico que precisa chegar ao cliente).  (criada -> em trânsito -> entregue /
 * devolvida / extraviada) (STATUSREMESSA)
 */
@Entity
@Table(name = "remessa")
public class Remessa {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID pedidoId;

    @Embedded
    private EnderecoEntrega enderecoEntrega;   // Value Object (QUESTÃO 1.4)

    @Column(nullable = false, unique = true)
    private String codigoRastreio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRemessa status;

    private LocalDateTime criadaEm;

    protected Remessa() {
    }

    public Remessa(UUID id, UUID pedidoId, EnderecoEntrega enderecoEntrega, String codigoRastreio) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("pedidoId é obrigatório");
        }
        if (enderecoEntrega == null) {
            throw new IllegalArgumentException("Endereço de entrega é obrigatório");
        }
        if (codigoRastreio == null || codigoRastreio.isBlank()) {
            throw new IllegalArgumentException("Código de rastreio é obrigatório");
        }
        this.id = id;
        this.pedidoId = pedidoId;
        this.enderecoEntrega = enderecoEntrega;
        this.codigoRastreio = codigoRastreio;
        this.status = StatusRemessa.CRIADA;
        this.criadaEm = LocalDateTime.now();
    }

    public void despachar() {
        if (status != StatusRemessa.CRIADA) {
            throw new IllegalStateException("Só é possível despachar uma remessa recém-criada");
        }
        this.status = StatusRemessa.EM_TRANSITO;
    }

    public void confirmarEntrega() {
        if (status != StatusRemessa.EM_TRANSITO) {
            throw new IllegalStateException("Só entrega remessa que está em trânsito");
        }
        this.status = StatusRemessa.ENTREGUE;
    }

    public void registrarDevolucao() {
        if (status != StatusRemessa.EM_TRANSITO) {
            throw new IllegalStateException("Só devolve remessa que está em trânsito");
        }
        this.status = StatusRemessa.DEVOLVIDA;
    }

    public UUID getId() { return id; }
    public UUID getPedidoId() { return pedidoId; }
    public EnderecoEntrega getEnderecoEntrega() { return enderecoEntrega; }
    public String getCodigoRastreio() { return codigoRastreio; }
    public StatusRemessa getStatus() { return status; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}
