package com.petfriends.transporte.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemessaTest {

    private Remessa criarRemessa() {
        EnderecoEntrega endereco = new EnderecoEntrega(
                "Rua das Flores", "123", null,
                "Centro", "São Paulo", "SP", "01001-000");
        return new Remessa(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), endereco, "PF1A2B3C4DBR");
    }

    @Test
    void novaRemessa_deveIniciarComStatusCriada() {
        Remessa remessa = criarRemessa();

        assertThat(remessa.getStatus()).isEqualTo(StatusRemessa.CRIADA);
    }

    @Test
    void novaRemessa_devePreencherCamposObrigatorios() {
        UUID pedidoId = UUID.randomUUID();
        EnderecoEntrega endereco = new EnderecoEntrega(
                "Av. Paulista", "1000", "Apto 1",
                "Bela Vista", "São Paulo", "SP", "01310-100");
        String codigoRastreio = "PF9X8Y7Z6WBR";

        Remessa remessa = new Remessa(UUID.randomUUID(), UUID.randomUUID(), pedidoId, endereco, codigoRastreio);

        assertThat(remessa.getPedidoId()).isEqualTo(pedidoId);
        assertThat(remessa.getCodigoRastreio()).isEqualTo(codigoRastreio);
        assertThat(remessa.getCriadaEm()).isNotNull();
    }

    @Test
    void despachar_deveAlterarStatusParaEmTransito() {
        Remessa remessa = criarRemessa();

        remessa.despachar();

        assertThat(remessa.getStatus()).isEqualTo(StatusRemessa.EM_TRANSITO);
    }

    @Test
    void despachar_quandoJaEmTransito_deveLancarIllegalStateException() {
        Remessa remessa = criarRemessa();
        remessa.despachar();

        assertThatThrownBy(remessa::despachar)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void confirmarEntrega_deveAlterarStatusParaEntregue() {
        Remessa remessa = criarRemessa();
        remessa.despachar();

        remessa.confirmarEntrega();

        assertThat(remessa.getStatus()).isEqualTo(StatusRemessa.ENTREGUE);
    }

    @Test
    void confirmarEntrega_semEstarEmTransito_deveLancarIllegalStateException() {
        Remessa remessa = criarRemessa();

        assertThatThrownBy(remessa::confirmarEntrega)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void registrarDevolucao_deveAlterarStatusParaDevolvida() {
        Remessa remessa = criarRemessa();
        remessa.despachar();

        remessa.registrarDevolucao();

        assertThat(remessa.getStatus()).isEqualTo(StatusRemessa.DEVOLVIDA);
    }

    @Test
    void registrarDevolucao_semEstarEmTransito_deveLancarIllegalStateException() {
        Remessa remessa = criarRemessa();

        assertThatThrownBy(remessa::registrarDevolucao)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void construtor_semPedidoId_deveLancarIllegalArgumentException() {
        EnderecoEntrega endereco = new EnderecoEntrega(
                "Rua A", "1", null, "Bairro", "Cidade", "SP", "01001-000");

        assertThatThrownBy(() -> new Remessa(UUID.randomUUID(), UUID.randomUUID(), null, endereco, "PF111111BR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pedidoId");
    }

    @Test
    void construtor_semCodigoRastreio_deveLancarIllegalArgumentException() {
        EnderecoEntrega endereco = new EnderecoEntrega(
                "Rua A", "1", null, "Bairro", "Cidade", "SP", "01001-000");

        assertThatThrownBy(() -> new Remessa(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), endereco, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rastreio");
    }
}
