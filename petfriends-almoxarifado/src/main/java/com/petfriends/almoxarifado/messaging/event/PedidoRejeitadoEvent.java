package com.petfriends.almoxarifado.messaging.event;

import java.util.UUID;

public record PedidoRejeitadoEvent(UUID correlationId, UUID pedidoId, String motivo) {
}
