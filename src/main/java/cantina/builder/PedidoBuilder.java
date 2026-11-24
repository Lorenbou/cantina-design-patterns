package cantina.builder;

import cantina.model.ItemPedido;
import cantina.model.Pedido;
import cantina.model.Pedido.TipoConsumo;
import cantina.model.Produto;

import java.util.ArrayList;
import java.util.List;

/**
 * Pattern BUILDER.
 *
 * Monta um {@link Pedido} passo a passo. O pedido tem campos obrigatórios
 * (número, cliente, ao menos um item) e opcionais (tipo de consumo,
 * observação). Em vez de um construtor com muitos parâmetros, cada passo
 * da montagem é um método encadeável, e o {@link #build()} valida tudo
 * antes de criar o objeto.
 *
 * Exemplo:
 * <pre>
 * Pedido pedido = new PedidoBuilder()
 *         .numero(1)
 *         .cliente("Ana")
 *         .paraViagem()
 *         .observacao("sem cebola")
 *         .adicionarItem(xisSalada, 1)
 *         .build();
 * </pre>
 */
public class PedidoBuilder {

    private Integer numero;
    private String cliente;
    private TipoConsumo tipoConsumo = TipoConsumo.LOCAL;
    private String observacao = "";
    private final List<ItemPedido> itens = new ArrayList<>();

    public PedidoBuilder numero(int numero) {
        this.numero = numero;
        return this;
    }

    public PedidoBuilder cliente(String cliente) {
        this.cliente = cliente;
        return this;
    }

    public PedidoBuilder paraConsumirNoLocal() {
        this.tipoConsumo = TipoConsumo.LOCAL;
        return this;
    }

    public PedidoBuilder paraViagem() {
        this.tipoConsumo = TipoConsumo.VIAGEM;
        return this;
    }

    public PedidoBuilder observacao(String observacao) {
        this.observacao = observacao == null ? "" : observacao.trim();
        return this;
    }

    public PedidoBuilder adicionarItem(Produto produto, int quantidade) {
        this.itens.add(new ItemPedido(produto, quantidade));
        return this;
    }

    public Pedido build() {
        if (numero == null || numero <= 0) {
            throw new IllegalStateException("O pedido precisa de um número válido.");
        }
        if (cliente == null || cliente.isBlank()) {
            throw new IllegalStateException("O pedido precisa do nome do cliente.");
        }
        if (itens.isEmpty()) {
            throw new IllegalStateException("O pedido precisa de pelo menos um item.");
        }
        return new Pedido(numero, cliente.trim(), tipoConsumo, observacao, itens);
    }
}
