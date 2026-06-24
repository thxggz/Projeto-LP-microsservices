package com.petfriends.almoxarifado.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemEstoqueTest {

    private ItemEstoque criarItem(int quantidade) {
        return new ItemEstoque(
                UUID.randomUUID(),
                "PET-001",
                quantidade,
                new LocalizacaoArmazem("A", "P1", 1)
        );
    }

    @Test
    void reservar_deveDescontarDisponivel_e_incrementarReservada() {
        ItemEstoque item = criarItem(10);

        item.reservar(3);

        assertThat(item.getQuantidadeDisponivel()).isEqualTo(7);
        assertThat(item.getQuantidadeReservada()).isEqualTo(3);
    }

    @Test
    void reservar_comExatamenteOEstoqueDisponivel_devePermitir() {
        ItemEstoque item = criarItem(5);

        item.reservar(5);

        assertThat(item.getQuantidadeDisponivel()).isEqualTo(0);
        assertThat(item.getQuantidadeReservada()).isEqualTo(5);
    }

    @Test
    void reservar_comEstoqueInsuficiente_deveLancarEstoqueInsuficienteException() {
        ItemEstoque item = criarItem(2);

        assertThatThrownBy(() -> item.reservar(5))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("PET-001")
                .hasMessageContaining("solicitado=5")
                .hasMessageContaining("disponível=2");
    }

    @Test
    void reservar_comQuantidadeZero_deveLancarIllegalArgumentException() {
        ItemEstoque item = criarItem(10);

        assertThatThrownBy(() -> item.reservar(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reservar_comQuantidadeNegativa_deveLancarIllegalArgumentException() {
        ItemEstoque item = criarItem(10);

        assertThatThrownBy(() -> item.reservar(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void liberarReserva_deveRestaurarEstoque() {
        ItemEstoque item = criarItem(10);
        item.reservar(4);

        item.liberarReserva(4);

        assertThat(item.getQuantidadeDisponivel()).isEqualTo(10);
        assertThat(item.getQuantidadeReservada()).isEqualTo(0);
    }

    @Test
    void liberarReserva_acimaDoReservado_deveLancarIllegalArgumentException() {
        ItemEstoque item = criarItem(10);
        item.reservar(3);

        assertThatThrownBy(() -> item.liberarReserva(5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
