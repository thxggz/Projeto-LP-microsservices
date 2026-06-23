package com.petfriends.transporte;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"pedidos.pedido-despachado"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext
class TransporteApplicationTests {

    @Test
    void contextLoads() {
    }
}
