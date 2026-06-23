package com.petfriends.almoxarifado.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;

/**

 * itemEstoque = (Aggregate Root)
 * "não posso reservar mais do que existe disponível".
 *
 * O @Version habilita lock otimista: protege a invariante quando dois eventos
 * tentam reservar o mesmo item ao mesmo tempo.
 */
@Entity
@Table(name = "item_estoque")
public class ItemEstoque {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private int quantidadeDisponivel;

    @Column(nullable = false)
    private int quantidadeReservada;

    // Value Object embutido (QUESTÃO 1.2)
    @Embedded
    private LocalizacaoArmazem localizacao;

    @Version
    private Long versao;

    protected ItemEstoque() {
    }

    public ItemEstoque(UUID id, String sku, int quantidadeDisponivel, LocalizacaoArmazem localizacao) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU é obrigatório");
        }
        if (quantidadeDisponivel < 0) {
            throw new IllegalArgumentException("Quantidade disponível não pode ser negativa");
        }
        this.id = id;
        this.sku = sku;
        this.quantidadeDisponivel = quantidadeDisponivel;
        this.quantidadeReservada = 0;
        this.localizacao = localizacao;
    }

//metodo agregate root(regra)

    public void reservar(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade a reservar deve ser positiva");
        }
        if (quantidade > quantidadeDisponivel) {
            throw new EstoqueInsuficienteException(sku, quantidade, quantidadeDisponivel);
        }
        this.quantidadeDisponivel -= quantidade;
        this.quantidadeReservada += quantidade;
    }


    public void liberarReserva(int quantidade) {
        if (quantidade <= 0 || quantidade > quantidadeReservada) {
            throw new IllegalArgumentException("Quantidade inválida para liberação");
        }
        this.quantidadeReservada -= quantidade;
        this.quantidadeDisponivel += quantidade;
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public int getQuantidadeDisponivel() { return quantidadeDisponivel; }
    public int getQuantidadeReservada() { return quantidadeReservada; }
    public LocalizacaoArmazem getLocalizacao() { return localizacao; }
}
