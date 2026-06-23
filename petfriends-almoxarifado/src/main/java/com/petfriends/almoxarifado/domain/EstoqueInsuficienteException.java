package com.petfriends.almoxarifado.domain;

public class EstoqueInsuficienteException extends RuntimeException {
    public EstoqueInsuficienteException(String sku, int solicitado, int disponivel) {
        super("Estoque insuficiente para o SKU " + sku
                + ": solicitado=" + solicitado + ", disponível=" + disponivel);
    }
}
