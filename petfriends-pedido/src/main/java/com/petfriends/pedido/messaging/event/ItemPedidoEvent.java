package com.petfriends.pedido.messaging.event;

public record ItemPedidoEvent(String sku, int quantidade) {
}
