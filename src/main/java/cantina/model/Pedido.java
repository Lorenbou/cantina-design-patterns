package cantina.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Pedido feito na cantina.
 *
 * Deve ser criado pelo {@link cantina.builder.PedidoBuilder}, que valida os dados
 * antes de construir. Depois de criado, o pedido é alterado pelos comandos do
 * pacote {@code cantina.command}.
 */
public class Pedido {

    public enum TipoConsumo { LOCAL, VIAGEM }

    public enum Status { ABERTO, FINALIZADO }

    private final int numero;
    private final String cliente;
    private final TipoConsumo tipoConsumo;
    private final String observacao;
    private final List<ItemPedido> itens = new ArrayList<>();
    private Status status = Status.ABERTO;
    private Cupom cupom;

    public Pedido(int numero, String cliente, TipoConsumo tipoConsumo,
                  String observacao, List<ItemPedido> itens) {
        this.numero = numero;
        this.cliente = cliente;
        this.tipoConsumo = tipoConsumo;
        this.observacao = observacao;
        for (ItemPedido item : itens) {
            somarItem(item.getProduto(), item.getQuantidade());
        }
    }

    /** Adiciona o produto; se ele já estiver no pedido, soma a quantidade. */
    public void adicionarItem(Produto produto, int quantidade) {
        garantirAberto();
        somarItem(produto, quantidade);
    }

    private void somarItem(Produto produto, int quantidade) {
        ItemPedido existente = buscarItem(produto);
        if (existente != null) {
            existente.aumentar(quantidade);
        } else {
            itens.add(new ItemPedido(produto, quantidade));
        }
    }

    /** Diminui a quantidade do produto; remove o item se chegar a zero. */
    public void removerQuantidade(Produto produto, int quantidade) {
        garantirAberto();
        ItemPedido item = buscarItem(produto);
        if (item == null) {
            throw new IllegalStateException("O produto " + produto.getNome() + " não está no pedido.");
        }
        if (quantidade >= item.getQuantidade()) {
            itens.remove(item);
        } else {
            item.diminuir(quantidade);
        }
    }

    /** Remove o item inteiro e devolve o que foi removido. */
    public ItemPedido removerItem(Produto produto) {
        garantirAberto();
        ItemPedido item = buscarItem(produto);
        if (item == null) {
            throw new IllegalStateException("O produto " + produto.getNome() + " não está no pedido.");
        }
        itens.remove(item);
        return item;
    }

    public void finalizar() {
        garantirAberto();
        if (itens.isEmpty()) {
            throw new IllegalStateException("Não é possível finalizar um pedido sem itens.");
        }
        status = Status.FINALIZADO;
    }

    public void reabrir() {
        status = Status.ABERTO;
    }

    public void aplicarCupom(Cupom cupom) {
        garantirAberto();
        this.cupom = Objects.requireNonNull(cupom, "O cupom é obrigatório.");
    }

    public void removerCupom() {
        garantirAberto();
        this.cupom = null;
    }

    public BigDecimal subtotal() {
        return itens.stream()
                .map(ItemPedido::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal desconto() {
        return cupom == null ? BigDecimal.ZERO : cupom.desconto(subtotal());
    }

    public BigDecimal total() {
        return subtotal().subtract(desconto());
    }

    private ItemPedido buscarItem(Produto produto) {
        return itens.stream()
                .filter(i -> i.getProduto().equals(produto))
                .findFirst()
                .orElse(null);
    }

    private void garantirAberto() {
        if (status != Status.ABERTO) {
            throw new IllegalStateException("O pedido #" + numero + " já foi finalizado.");
        }
    }

    public int getNumero() {
        return numero;
    }

    public String getCliente() {
        return cliente;
    }

    public TipoConsumo getTipoConsumo() {
        return tipoConsumo;
    }

    public String getObservacao() {
        return observacao;
    }

    public Status getStatus() {
        return status;
    }

    public Optional<Cupom> getCupom() {
        return Optional.ofNullable(cupom);
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public String resumo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Pedido #%d | Cliente: %s | %s | %s%n",
                numero, cliente, tipoConsumo, status));
        if (observacao != null && !observacao.isBlank()) {
            sb.append("Observação: ").append(observacao).append(System.lineSeparator());
        }
        if (itens.isEmpty()) {
            sb.append("  (sem itens)").append(System.lineSeparator());
        }
        for (ItemPedido item : itens) {
            sb.append("  ").append(item).append(System.lineSeparator());
        }
        if (cupom != null) {
            sb.append(String.format("Cupom: %s = - R$ %.2f%n", cupom, desconto()));
        }
        sb.append(String.format("Total: R$ %.2f", total()));
        return sb.toString();
    }
}
