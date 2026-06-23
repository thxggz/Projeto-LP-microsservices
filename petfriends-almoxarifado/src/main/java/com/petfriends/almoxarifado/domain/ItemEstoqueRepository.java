package com.petfriends.almoxarifado.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/** 1 repositorio por agregado (item estoque)
 * findBySku() permite buscar o item pelo código do produto (SKU),
 * que é o que o consumidor Kafka usa ao receber um evento de pedido.
 */

public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, UUID> {
    Optional<ItemEstoque> findBySku(String sku);
}
