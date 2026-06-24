package com.petfriends.almoxarifado.messaging.event;

import java.util.List;
import java.util.UUID;

public record PedidoFechadoEvent(
        UUID pedidoId,
        UUID clienteId,
        String nomeCliente,
        List<ItemPedido> itens,
        EnderecoEntregaDto enderecoEntrega,
        double pesoKg
) {
}
