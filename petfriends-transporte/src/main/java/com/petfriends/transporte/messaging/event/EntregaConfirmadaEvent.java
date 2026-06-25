package com.petfriends.transporte.messaging.event;

import java.util.UUID;

public record EntregaConfirmadaEvent(
        UUID correlationId,
        UUID pedidoId,
        String codigoRastreio
) {}
