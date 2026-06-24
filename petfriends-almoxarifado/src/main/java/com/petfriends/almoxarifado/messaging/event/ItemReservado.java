package com.petfriends.almoxarifado.messaging.event;

public record ItemReservado(
        String sku,
        int quantidade,
        String localizacaoArmazem
) {
}
