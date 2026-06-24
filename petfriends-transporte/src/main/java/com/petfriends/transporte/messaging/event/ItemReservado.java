package com.petfriends.transporte.messaging.event;

public record ItemReservado(
        String sku,
        int quantidade,
        String localizacaoArmazem
) {
}
