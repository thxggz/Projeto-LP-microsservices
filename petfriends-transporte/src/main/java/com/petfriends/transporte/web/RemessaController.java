package com.petfriends.transporte.web;

import com.petfriends.transporte.domain.Remessa;
import com.petfriends.transporte.domain.RemessaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/remessas")
public class RemessaController {

    private final RemessaRepository repository;

    public RemessaController(RemessaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Remessa> listar() {
        return repository.findAll();
    }
}