package com.petfriends.almoxarifado;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"pedidos.pedido-fechado", "almoxarifado.pedido-reservado"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext
class AlmoxarifadoApplicationTests {

    @Test
    void contextLoads() {
    }
}
