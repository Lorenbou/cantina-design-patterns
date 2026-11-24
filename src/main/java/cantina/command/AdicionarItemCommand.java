package cantina.command;

import cantina.model.Pedido;
import cantina.model.Produto;

/**
 * Comando concreto: adiciona uma quantidade de um produto ao pedido.
 */
public class AdicionarItemCommand implements Comando {

    private final Pedido pedido;
    private final Produto produto;
    private final int quantidade;

    public AdicionarItemCommand(Pedido pedido, Produto produto, int quantidade) {
        this.pedido = pedido;
        this.produto = produto;
        this.quantidade = quantidade;
    }

    @Override
    public void executar() {
        pedido.adicionarItem(produto, quantidade);
    }

    @Override
    public void desfazer() {
        pedido.removerQuantidade(produto, quantidade);
    }

    @Override
    public String descricao() {
        return "Adicionar " + quantidade + "x " + produto.getNome();
    }
}
