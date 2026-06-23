package com.petfriends.almoxarifado.web;

import com.petfriends.almoxarifado.domain.ItemEstoque;
import com.petfriends.almoxarifado.domain.ItemEstoqueRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/itens-estoque")
public class ItemEstoqueController {

    private final ItemEstoqueRepository repository;

    public ItemEstoqueController(ItemEstoqueRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ItemEstoque> listar() {
        return repository.findAll();
    }
}