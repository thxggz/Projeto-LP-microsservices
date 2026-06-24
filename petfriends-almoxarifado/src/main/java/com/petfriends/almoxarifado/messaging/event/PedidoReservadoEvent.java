package com.petfriends.almoxarifado.messaging.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Evento publicado pelo Almoxarifado após reservar com sucesso todos os itens do pedido.
 * O correlationId permite rastrear o pedido de ponta a ponta entre os microsserviços.
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
