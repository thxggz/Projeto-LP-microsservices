package com.petfriends.transporte.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * QUESTÃO 1.4 — Value Object do agregado Remessa.
 *
 * Endereço é o exemplo clássico de Value Object: não tem identidade própria
 é imutável ,Valida o CEP no construtor, então nunca existe um
 * EnderecoEntrega inválido no sistema.
 */
@Embeddable
public class EnderecoEntrega {

    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    protected EnderecoEntrega() {
    }

    public EnderecoEntrega(String logradouro, String numero, String complemento,
                           String bairro, String cidade, String estado, String cep) {
        if (logradouro == null || logradouro.isBlank()) {
            throw new IllegalArgumentException("Logradouro é obrigatório");
        }
        if (cidade == null || cidade.isBlank()) {
            throw new IllegalArgumentException("Cidade é obrigatória");
        }
        if (estado == null || estado.length() != 2) {
            throw new IllegalArgumentException("Estado deve ter 2 letras (UF)");
        }
        if (cep == null || !cep.matches("\\d{5}-?\\d{3}")) {
            throw new IllegalArgumentException("CEP inválido: " + cep);
        }
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado.toUpperCase();
        this.cep = cep;
    }

    public String enderecoFormatado() {
        return logradouro + ", " + numero
                + (complemento != null && !complemento.isBlank() ? " - " + complemento : "")
                + " - " + bairro + ", " + cidade + "/" + estado + " - CEP " + cep;
    }

    public String getLogradouro() { return logradouro; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getCep() { return cep; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnderecoEntrega outro)) return false;
        return Objects.equals(logradouro, outro.logradouro)
                && Objects.equals(numero, outro.numero)
                && Objects.equals(complemento, outro.complemento)
                && Objects.equals(bairro, outro.bairro)
                && Objects.equals(cidade, outro.cidade)
                && Objects.equals(estado, outro.estado)
                && Objects.equals(cep, outro.cep);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logradouro, numero, complemento, bairro, cidade, estado, cep);
    }
}
