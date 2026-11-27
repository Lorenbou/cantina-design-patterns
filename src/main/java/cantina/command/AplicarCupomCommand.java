package cantina.command;

import cantina.model.Cupom;
import cantina.model.Pedido;

import java.util.Objects;

public class AplicarCupomCommand implements Comando {

    private final Pedido pedido;
    private final Cupom cupom;
    private Cupom cupomAnterior;

    public AplicarCupomCommand(Pedido pedido, Cupom cupom) {
        this.pedido = Objects.requireNonNull(pedido);
        this.cupom = Objects.requireNonNull(cupom);
    }

    @Override
    public void executar() {
        cupomAnterior = pedido.getCupom().orElse(null);
        pedido.aplicarCupom(cupom);
    }

    @Override
    public void desfazer() {
        if (cupomAnterior != null) {
            pedido.aplicarCupom(cupomAnterior);
        } else {
            pedido.removerCupom();
        }
    }

    @Override
    public String descricao() {
        return "Aplicar cupom " + cupom;
    }
}
