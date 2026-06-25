package com.petfriends.pedido.web.dto;

import java.util.List;

public record CriarPedidoRequest(
        String nomeCliente,
        List<ItemRequest> itens,
        EnderecoRequest enderecoEntrega,
        double pesoKg
) {
}
