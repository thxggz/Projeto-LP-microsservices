package com.petfriends.pedido.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class ItemPedido {

    private String sku;
    private int quantidade;

    protected ItemPedido() {}

    public ItemPedido(String sku, int quantidade) {
        this.sku = sku;
        this.quantidade = quantidade;
    }

    public String getSku() { return sku; }
    public int getQuantidade() { return quantidade; }
}
