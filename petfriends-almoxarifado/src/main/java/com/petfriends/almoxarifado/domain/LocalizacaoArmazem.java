package com.petfriends.almoxarifado.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * QUESTÃO 1.2 — Value Object do agregado ItemEstoque (entity agregate).
 *
 * Características de um Value Object:
 *  -imutável só leitura, não tem id etc
 */
@Embeddable
public class LocalizacaoArmazem {

    private String corredor;
    private String prateleira;
    private int nivel;

    protected LocalizacaoArmazem() {
    }

    public LocalizacaoArmazem(String corredor, String prateleira, int nivel) {
        if (corredor == null || corredor.isBlank()) {
            throw new IllegalArgumentException("Corredor é obrigatório");
        }
        if (prateleira == null || prateleira.isBlank()) {
            throw new IllegalArgumentException("Prateleira é obrigatória");
        }
        if (nivel <= 0) {
            throw new IllegalArgumentException("Nível deve ser positivo");
        }
        this.corredor = corredor;
        this.prateleira = prateleira;
        this.nivel = nivel;
    }

    public String descricao() {
        return corredor + "-" + prateleira + "-" + nivel;
    }

    public String getCorredor() { return corredor; }
    public String getPrateleira() { return prateleira; }
    public int getNivel() { return nivel; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalizacaoArmazem outra)) return false;
        return nivel == outra.nivel
                && Objects.equals(corredor, outra.corredor)
                && Objects.equals(prateleira, outra.prateleira);
    }

    @Override
    public int hashCode() {
        return Objects.hash(corredor, prateleira, nivel);
    }
}
