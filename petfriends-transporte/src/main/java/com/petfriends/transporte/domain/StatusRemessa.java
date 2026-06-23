package com.petfriends.transporte.domain;

/** Estados da remessa, espelhando os estados de transporte do Diagrama 1. */
public enum StatusRemessa {
    CRIADA,
    EM_TRANSITO,
    ENTREGUE,
    DEVOLVIDA,
    EXTRAVIADA
}
