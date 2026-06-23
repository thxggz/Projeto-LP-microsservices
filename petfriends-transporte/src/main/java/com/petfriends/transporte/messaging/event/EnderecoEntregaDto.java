package com.petfriends.transporte.messaging.event;


public record EnderecoEntregaDto(
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String cep
) {
}
