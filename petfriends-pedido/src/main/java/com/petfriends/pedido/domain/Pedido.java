package com.petfriends.pedido.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    private UUID id;

    private UUID correlationId;

    private UUID clienteId;
    private String nomeCliente;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pedido_itens", joinColumns = @JoinColumn(name = "pedido_id"))
    private List<ItemPedido> itens;

    @Embedded
    private EnderecoEntrega enderecoEntrega;

    private double pesoKg;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    private Instant criadoEm;

    private String motivoRejeicao;

    protected Pedido() {}

    public Pedido(UUID id, UUID correlationId, UUID clienteId, String nomeCliente,
                  List<ItemPedido> itens, EnderecoEntrega enderecoEntrega, double pesoKg) {
        this.id = id;
        this.correlationId = correlationId;
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
        this.itens = itens;
        this.enderecoEntrega = enderecoEntrega;
        this.pesoKg = pesoKg;
        this.status = StatusPedido.ABERTO;
        this.criadoEm = Instant.now();
    }

    public void marcarComoReservado() {
        this.status = StatusPedido.RESERVADO;
    }

    public void marcarComoRejeitado(String motivo) {
        this.status = StatusPedido.REJEITADO;
        this.motivoRejeicao = motivo;
    }

    public UUID getId() { return id; }
    public UUID getCorrelationId() { return correlationId; }
    public UUID getClienteId() { return clienteId; }
    public String getNomeCliente() { return nomeCliente; }
    public List<ItemPedido> getItens() { return itens; }
    public EnderecoEntrega getEnderecoEntrega() { return enderecoEntrega; }
    public double getPesoKg() { return pesoKg; }
    public StatusPedido getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
    public String getMotivoRejeicao() { return motivoRejeicao; }
}
