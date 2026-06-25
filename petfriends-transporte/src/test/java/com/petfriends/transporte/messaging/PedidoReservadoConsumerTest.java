package com.petfriends.transporte.messaging;

import com.petfriends.transporte.domain.Remessa;
import com.petfriends.transporte.domain.RemessaRepository;
import com.petfriends.transporte.domain.StatusRemessa;
import com.petfriends.transporte.messaging.event.EnderecoEntregaDto;
import com.petfriends.transporte.messaging.event.ItemReservado;
import com.petfriends.transporte.messaging.event.PedidoReservadoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoReservadoConsumerTest {

    @Mock
    private RemessaRepository remessaRepository;

    private PedidoReservadoConsumer consumer;

    private static final EnderecoEntregaDto ENDERECO = new EnderecoEntregaDto(
            "Rua das Flores", "123", null, "Centro", "São Paulo", "SP", "01001-000");

    @BeforeEach
    void setUp() {
        consumer = new PedidoReservadoConsumer(remessaRepository);
    }

    @Test
    void aoReceberPedidoReservado_deveCriarRemessa_e_DespacharImediatamente() {
        UUID pedidoId = UUID.randomUUID();
        PedidoReservadoEvent evento = criarEvento(pedidoId);

        when(remessaRepository.findByPedidoId(pedidoId)).thenReturn(Optional.empty());

        consumer.aoReceberPedidoReservado(evento);

        ArgumentCaptor<Remessa> captor = ArgumentCaptor.forClass(Remessa.class);
        verify(remessaRepository).save(captor.capture());

        Remessa remessaSalva = captor.getValue();
        assertThat(remessaSalva.getPedidoId()).isEqualTo(pedidoId);
        assertThat(remessaSalva.getStatus()).isEqualTo(StatusRemessa.EM_TRANSITO);
        assertThat(remessaSalva.getCodigoRastreio()).isNotBlank();
        assertThat(remessaSalva.getEnderecoEntrega()).isNotNull();
    }

    @Test
    void aoReceberPedidoReservado_codigoRastreio_deveIniciarComPF_e_TerminarComBR() {
        UUID pedidoId = UUID.randomUUID();

        when(remessaRepository.findByPedidoId(pedidoId)).thenReturn(Optional.empty());

        consumer.aoReceberPedidoReservado(criarEvento(pedidoId));

        ArgumentCaptor<Remessa> captor = ArgumentCaptor.forClass(Remessa.class);
        verify(remessaRepository).save(captor.capture());

        String codigo = captor.getValue().getCodigoRastreio();
        assertThat(codigo).startsWith("PF").endsWith("BR");
    }

    @Test
    void aoReceberPedidoReservado_comPedidoDuplicado_naoDeveCriarNovaRemessa() {
        UUID pedidoId = UUID.randomUUID();
        Remessa remessaExistente = criarRemessaExistente(pedidoId);

        when(remessaRepository.findByPedidoId(pedidoId)).thenReturn(Optional.of(remessaExistente));

        consumer.aoReceberPedidoReservado(criarEvento(pedidoId));

        verify(remessaRepository, never()).save(any());
    }

    @Test
    void aoReceberPedidoReservado_enderecoDeveSerMapeadoCorretamente() {
        UUID pedidoId = UUID.randomUUID();

        when(remessaRepository.findByPedidoId(pedidoId)).thenReturn(Optional.empty());

        consumer.aoReceberPedidoReservado(criarEvento(pedidoId));

        ArgumentCaptor<Remessa> captor = ArgumentCaptor.forClass(Remessa.class);
        verify(remessaRepository).save(captor.capture());

        var endereco = captor.getValue().getEnderecoEntrega();
        assertThat(endereco.getLogradouro()).isEqualTo("Rua das Flores");
        assertThat(endereco.getCidade()).isEqualTo("São Paulo");
        assertThat(endereco.getEstado()).isEqualTo("SP");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private PedidoReservadoEvent criarEvento(UUID pedidoId) {
        return new PedidoReservadoEvent(
                UUID.randomUUID(),
                pedidoId,
                UUID.randomUUID(),
                "João Silva",
                List.of(new ItemReservado("PET-001", 2, "A-P1-1")),
                ENDERECO,
                1.5,
                Instant.now()
        );
    }

    private Remessa criarRemessaExistente(UUID pedidoId) {
        com.petfriends.transporte.domain.EnderecoEntrega endereco =
                new com.petfriends.transporte.domain.EnderecoEntrega(
                        "Rua das Flores", "123", null,
                        "Centro", "São Paulo", "SP", "01001-000");
        return new Remessa(UUID.randomUUID(), UUID.randomUUID(), pedidoId, endereco, "PF11223344BR");
    }
}
