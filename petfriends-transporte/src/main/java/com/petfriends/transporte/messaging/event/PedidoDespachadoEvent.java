package com.petfriends.transporte.messaging.event;

import java.util.UUID;


public record PedidoDespachadoEvent(
        UUID pedidoId,
        UUID clienteId,
        String nomeDestinatario,
        EnderecoEntregaDto enderecoEntrega,
        double pesoKg
) {
}
