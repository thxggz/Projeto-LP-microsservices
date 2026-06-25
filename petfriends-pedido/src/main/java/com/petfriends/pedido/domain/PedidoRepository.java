package com.petfriends.pedido.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
    Optional<Pedido> findByCorrelationId(UUID correlationId);
}
