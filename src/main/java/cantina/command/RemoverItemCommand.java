package cantina.command;

import cantina.model.ItemPedido;
import cantina.model.Pedido;
import cantina.model.Produto;

/**
 * Comando concreto: remove um item inteiro do pedido.
 * Guarda o item removido para conseguir desfazer a remoção.
 */
public class RemoverItemCommand implements Comando {

    private final Pedido pedido;
    private final Produto produto;
    private ItemPedido itemRemovido;

    public RemoverItemCommand(Pedido pedido, Produto produto) {
        this.pedido = pedido;
        this.produto = produto;
    }

    @Override
    public void executar() {
        itemRemovido = pedido.removerItem(produto);
    }

    @Override
    public void desfazer() {
        pedido.adicionarItem(itemRemovido.getProduto(), itemRemovido.getQuantidade());
    }

    @Override
    public String descricao() {
        return "Remover " + produto.getNome();
    }
}
