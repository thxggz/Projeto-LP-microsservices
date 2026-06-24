package com.petfriends.transporte.messaging.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Evento recebido do microsserviço Almoxarifado após a reserva bem-sucedida do pedido.
 * O correlationId permite rastrear o pedido de ponta a ponta entre os serviços.
 */
public record PedidoReservadoEvent(
        UUID correlationId,
        UUID pedidoId,
        UUID clienteId,
        String nomeCliente,
        List<ItemReservado> itensReservados,
        EnderecoEntregaDto enderecoEntrega,
        double pesoKg,
        Instant reservadoEm
) {
}
