package com.petfriends.transporte.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * QUESTÃO 1.3 — Repository do agregado Remessa (um repository por agregado).
 */
public interface RemessaRepository extends JpaRepository<Remessa, UUID> {
    Optional<Remessa> findByPedidoId(UUID pedidoId);
    Optional<Remessa> findByCodigoRastreio(String codigoRastreio);
}
