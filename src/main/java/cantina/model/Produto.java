package cantina.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Item do cardápio da cantina.
 */
public class Produto {

    private final int codigo;
    private final String nome;
    private final BigDecimal preco;

    public Produto(int codigo, String nome, BigDecimal preco) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (preco == null || preco.signum() <= 0) {
            throw new IllegalArgumentException("O preço do produto deve ser maior que zero.");
        }
        this.codigo = codigo;
        this.nome = nome;
        this.preco = preco;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Produto)) return false;
        return codigo == ((Produto) o).codigo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return String.format("%d - %s (R$ %.2f)", codigo, nome, preco);
    }
}
