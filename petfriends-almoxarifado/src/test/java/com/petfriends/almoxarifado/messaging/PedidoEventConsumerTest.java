package com.petfriends.almoxarifado.messaging;

import com.petfriends.almoxarifado.domain.ItemEstoque;
import com.petfriends.almoxarifado.domain.ItemEstoqueRepository;
import com.petfriends.almoxarifado.domain.LocalizacaoArmazem;
import com.petfriends.almoxarifado.messaging.event.EnderecoEntregaDto;
import com.petfriends.almoxarifado.messaging.event.ItemPedido;
import com.petfriends.almoxarifado.messaging.event.PedidoFechadoEvent;
import com.petfriends.almoxarifado.messaging.event.PedidoRejeitadoEvent;
import com.petfriends.almoxarifado.messaging.event.PedidoReservadoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoEventConsumerTest {

    @Mock
    private ItemEstoqueRepository itemEstoqueRepository;

    @Mock
    private KafkaOperations<String, PedidoReservadoEvent> kafkaTemplate;

    @Mock
    private KafkaTemplate<String, PedidoRejeitadoEvent> rejeitadoTemplate;

    private PedidoEventConsumer consumer;

    private static final EnderecoEntregaDto ENDERECO = new EnderecoEntregaDto(
            "Rua das Flores", "123", null, "Centro", "São Paulo", "SP", "01001-000");

    @BeforeEach
    void setUp() {
        consumer = new PedidoEventConsumer(itemEstoqueRepository, kafkaTemplate, rejeitadoTemplate);
    }

    @Test
    void aoReceberPedidoFechado_deveReservarItens_e_publicarPedidoReservadoEvent() {
        UUID correlationId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        ItemEstoque item = new ItemEstoque(
                UUID.randomUUID(), "PET-001", 10,
                new LocalizacaoArmazem("A", "P1", 1));

        PedidoFechadoEvent evento = new PedidoFechadoEvent(
                correlationId, pedidoId, clienteId, "João Silva",
                List.of(new ItemPedido("PET-001", 2)),
                ENDERECO, 1.5);

        when(itemEstoqueRepository.findBySku("PET-001")).thenReturn(Optional.of(item));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        consumer.aoReceberPedidoFechado(evento);

        assertThat(item.getQuantidadeDisponivel()).isEqualTo(8);
        assertThat(item.getQuantidadeReservada()).isEqualTo(2);
        verify(itemEstoqueRepository).save(item);

        ArgumentCaptor<PedidoReservadoEvent> captor = ArgumentCaptor.forClass(PedidoReservadoEvent.class);
        verify(kafkaTemplate).send(eq("almoxarifado.pedido-reservado"), eq(pedidoId.toString()), captor.capture());

        PedidoReservadoEvent publicado = captor.getValue();
        assertThat(publicado.correlationId()).isEqualTo(correlationId);
        assertThat(publicado.pedidoId()).isEqualTo(pedidoId);
        assertThat(publicado.clienteId()).isEqualTo(clienteId);
        assertThat(publicado.nomeCliente()).isEqualTo("João Silva");
        assertThat(publicado.reservadoEm()).isNotNull();
        assertThat(publicado.itensReservados()).hasSize(1);
        assertThat(publicado.itensReservados().get(0).sku()).isEqualTo("PET-001");
        assertThat(publicado.itensReservados().get(0).quantidade()).isEqualTo(2);
        assertThat(publicado.itensReservados().get(0).localizacaoArmazem()).isEqualTo("A-P1-1");
    }

    @Test
    void aoReceberPedidoFechado_comDoisItens_deveReservarAmbos_e_publicarEventoCompleto() {
        UUID correlationId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();

        ItemEstoque item1 = new ItemEstoque(UUID.randomUUID(), "PET-001", 10,
                new LocalizacaoArmazem("A", "P1", 1));
        ItemEstoque item2 = new ItemEstoque(UUID.randomUUID(), "PET-002", 5,
                new LocalizacaoArmazem("B", "P2", 2));

        PedidoFechadoEvent evento = new PedidoFechadoEvent(
                correlationId, pedidoId, UUID.randomUUID(), "Maria",
                List.of(new ItemPedido("PET-001", 1), new ItemPedido("PET-002", 3)),
                ENDERECO, 2.0);

        when(itemEstoqueRepository.findBySku("PET-001")).thenReturn(Optional.of(item1));
        when(itemEstoqueRepository.findBySku("PET-002")).thenReturn(Optional.of(item2));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        consumer.aoReceberPedidoFechado(evento);

        assertThat(item1.getQuantidadeDisponivel()).isEqualTo(9);
        assertThat(item2.getQuantidadeDisponivel()).isEqualTo(2);

        ArgumentCaptor<PedidoReservadoEvent> captor = ArgumentCaptor.forClass(PedidoReservadoEvent.class);
        verify(kafkaTemplate).send(anyString(), anyString(), captor.capture());
        assertThat(captor.getValue().itensReservados()).hasSize(2);
    }

    @Test
    void aoReceberPedidoFechado_comSkuInexistente_devePublicarPedidoRejeitadoEvent() {
        UUID correlationId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();

        PedidoFechadoEvent evento = new PedidoFechadoEvent(
                correlationId, pedidoId, UUID.randomUUID(), "Carlos",
                List.of(new ItemPedido("SKU-INEXISTENTE", 1)),
                ENDERECO, 0.5);

        when(itemEstoqueRepository.findBySku("SKU-INEXISTENTE")).thenReturn(Optional.empty());
        when(rejeitadoTemplate.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        consumer.aoReceberPedidoFechado(evento);

        ArgumentCaptor<PedidoRejeitadoEvent> captor = ArgumentCaptor.forClass(PedidoRejeitadoEvent.class);
        verify(rejeitadoTemplate).send(eq("almoxarifado.pedido-rejeitado"), eq(pedidoId.toString()), captor.capture());

        PedidoRejeitadoEvent rejeitado = captor.getValue();
        assertThat(rejeitado.correlationId()).isEqualTo(correlationId);
        assertThat(rejeitado.pedidoId()).isEqualTo(pedidoId);
        assertThat(rejeitado.motivo()).contains("SKU-INEXISTENTE");
    }
}
