package cantina.model;

import java.math.BigDecimal;

/**
 * Um produto dentro de um pedido, com a quantidade pedida.
 */
public class ItemPedido {

    private final Produto produto;
    private int quantidade;

    public ItemPedido(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto é obrigatório.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    void aumentar(int quantidade) {
        this.quantidade += quantidade;
    }

    void diminuir(int quantidade) {
        this.quantidade -= quantidade;
    }

    public BigDecimal subtotal() {
        return produto.getPreco().multiply(BigDecimal.valueOf(quantidade));
    }

    @Override
    public String toString() {
        return String.format("%dx %s = R$ %.2f", quantidade, produto.getNome(), subtotal());
    }
}
