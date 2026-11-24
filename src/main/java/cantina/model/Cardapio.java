package cantina.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Produtos disponíveis na cantina, mantidos em memória (sem banco de dados).
 */
public class Cardapio {

    private final List<Produto> produtos = List.of(
            new Produto(1, "Pão de queijo", new BigDecimal("4.50")),
            new Produto(2, "Coxinha", new BigDecimal("7.00")),
            new Produto(3, "Xis salada", new BigDecimal("18.00")),
            new Produto(4, "Café", new BigDecimal("5.00")),
            new Produto(5, "Suco natural", new BigDecimal("8.50")),
            new Produto(6, "Refrigerante lata", new BigDecimal("6.00"))
    );

    public List<Produto> getProdutos() {
        return produtos;
    }

    public Optional<Produto> buscar(int codigo) {
        return produtos.stream().filter(p -> p.getCodigo() == codigo).findFirst();
    }
}
