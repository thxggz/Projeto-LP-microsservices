package com.petfriends.pedido.web.dto;

public record EnderecoRequest(
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String cep
) {
}
