package com.petfriends.almoxarifado.messaging.event;

import java.util.List;
import java.util.UUID;


public record PedidoFechadoEvent(
        UUID pedidoId,
        UUID clienteId,
        List<ItemPedido> itens
) {
}
