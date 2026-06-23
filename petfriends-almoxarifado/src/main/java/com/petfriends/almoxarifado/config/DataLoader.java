package com.petfriends.almoxarifado.config;

import com.petfriends.almoxarifado.domain.ItemEstoque;
import com.petfriends.almoxarifado.domain.ItemEstoqueRepository;
import com.petfriends.almoxarifado.domain.LocalizacaoArmazem;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class DataLoader implements CommandLineRunner {

    private final ItemEstoqueRepository repository;

    public DataLoader(ItemEstoqueRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;
        repository.save(new ItemEstoque(UUID.randomUUID(), "RACAO-CAO-15KG", 100,
                new LocalizacaoArmazem("A", "P1", 1)));
        repository.save(new ItemEstoque(UUID.randomUUID(), "BRINQUEDO-BOLA", 50,
                new LocalizacaoArmazem("B", "P3", 2)));
    }
}