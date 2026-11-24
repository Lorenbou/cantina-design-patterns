package cantina.command;

import cantina.model.Pedido;

/**
 * Comando concreto: finaliza o pedido (envia para a cozinha).
 * Desfazer reabre o pedido para novas alterações.
 */
public class FinalizarPedidoCommand implements Comando {

    private final Pedido pedido;

    public FinalizarPedidoCommand(Pedido pedido) {
        this.pedido = pedido;
    }

    @Override
    public void executar() {
        pedido.finalizar();
    }

    @Override
    public void desfazer() {
        pedido.reabrir();
    }

    @Override
    public String descricao() {
        return "Finalizar pedido #" + pedido.getNumero();
    }
}
