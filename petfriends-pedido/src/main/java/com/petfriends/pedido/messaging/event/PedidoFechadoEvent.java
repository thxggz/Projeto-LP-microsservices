package com.petfriends.pedido.messaging.event;

import java.util.List;
import java.util.UUID;

/**
 * Publicado no tópico "pedidos.pedido-fechado".
 * Campos espelham exatamente o que o Almoxarifado deserializa.
 */
public record PedidoFechadoEvent(
        UUID correlationId,
        UUID pedidoId,
        UUID clienteId,
        String nomeCliente,
        List<ItemPedidoEvent> itens,
        EnderecoEntregaDto enderecoEntrega,
        double pesoKg
) {
}
