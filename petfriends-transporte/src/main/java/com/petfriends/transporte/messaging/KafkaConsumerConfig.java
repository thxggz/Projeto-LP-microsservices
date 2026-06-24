package com.petfriends.transporte.messaging;

import com.petfriends.transporte.messaging.event.PedidoDespachadoEvent;
import com.petfriends.transporte.messaging.event.PedidoReservadoEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configura dois consumers Kafka:
 * - kafkaListenerContainerFactory: consome PedidoDespachadoEvent do tópico pedidos.pedido-despachado
 * - pedidoReservadoContainerFactory: consome PedidoReservadoEvent do tópico almoxarifado.pedido-reservado
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:petfriends-transporte}")
    private String groupId;

    // ── PedidoDespachadoEvent (tópico original) ──────────────────────────────

    @Bean
    public ConsumerFactory<String, PedidoDespachadoEvent> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(baseProps(
                PedidoDespachadoEvent.class.getName()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PedidoDespachadoEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PedidoDespachadoEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    // ── PedidoReservadoEvent (tópico do Almoxarifado) ────────────────────────

    @Bean
    public ConsumerFactory<String, PedidoReservadoEvent> pedidoReservadoConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(baseProps(
                PedidoReservadoEvent.class.getName()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PedidoReservadoEvent> pedidoReservadoContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PedidoReservadoEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(pedidoReservadoConsumerFactory());
        return factory;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> baseProps(String targetType) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, targetType);
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return props;
    }
}
