package cantina.command;

import cantina.builder.PedidoBuilder;
import cantina.model.ItemPedido;
import cantina.model.Pedido;
import cantina.model.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandosTest {

    private final Produto paoDeQueijo = new Produto(1, "Pão de queijo", new BigDecimal("4.50"));
    private final Produto coxinha = new Produto(2, "Coxinha", new BigDecimal("7.00"));
    private final Produto cafe = new Produto(4, "Café", new BigDecimal("5.00"));

    private Pedido pedido;
    private Atendente atendente;

    @BeforeEach
    void configurar() {
        pedido = new PedidoBuilder()
                .numero(1)
                .cliente("Ana")
                .adicionarItem(paoDeQueijo, 2)
                .adicionarItem(cafe, 1)
                .build();
        atendente = new Atendente();
    }

    @Test
    void adicionarItemExecutarAdicionaEDesfazerRemove() {
        atendente.executar(new AdicionarItemCommand(pedido, coxinha, 3));

        assertEquals(3, pedido.getItens().size());
        assertEquals(0, new BigDecimal("35.00").compareTo(pedido.total()));

        atendente.desfazerUltimo();

        assertEquals(2, pedido.getItens().size());
        assertTrue(pedido.getItens().stream().noneMatch(i -> i.getProduto().equals(coxinha)));
        assertEquals(0, new BigDecimal("14.00").compareTo(pedido.total()));
    }

    @Test
    void adicionarQuantidadeEmItemExistenteEDesfazerRestauraQuantidadeAnterior() {
        atendente.executar(new AdicionarItemCommand(pedido, paoDeQueijo, 3));
        assertEquals(5, buscarQuantidade(paoDeQueijo));

        atendente.desfazerUltimo();

        assertEquals(2, buscarQuantidade(paoDeQueijo));
        assertEquals(0, new BigDecimal("14.00").compareTo(pedido.total()));
    }

    @Test
    void removerItemExecutarRemoveEDesfazerDevolveComMesmaQuantidade() {
        atendente.executar(new RemoverItemCommand(pedido, paoDeQueijo));

        assertEquals(1, pedido.getItens().size());
        assertTrue(pedido.getItens().stream().noneMatch(i -> i.getProduto().equals(paoDeQueijo)));
        assertEquals(0, new BigDecimal("5.00").compareTo(pedido.total()));

        atendente.desfazerUltimo();

        assertEquals(2, pedido.getItens().size());
        assertEquals(2, buscarQuantidade(paoDeQueijo));
        assertEquals(0, new BigDecimal("14.00").compareTo(pedido.total()));
    }

    @Test
    void finalizarPedidoBloqueiaAlteracoesEDesfazerReabre() {
        atendente.executar(new FinalizarPedidoCommand(pedido));

        assertEquals(Pedido.Status.FINALIZADO, pedido.getStatus());
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> atendente.executar(new AdicionarItemCommand(pedido, coxinha, 1)));
        assertEquals("O pedido #1 já foi finalizado.", erro.getMessage());

        atendente.desfazerUltimo();

        assertEquals(Pedido.Status.ABERTO, pedido.getStatus());
        atendente.executar(new AdicionarItemCommand(pedido, coxinha, 1));
        assertEquals(1, buscarQuantidade(coxinha));
    }

    @Test
    void desfazerComHistoricoVazioLancaErro() {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> atendente.desfazerUltimo());
        assertEquals("Não há ações para desfazer.", erro.getMessage());
    }

    @Test
    void comandoQueFalhaNaoEntraNoHistorico() {
        atendente.executar(new AdicionarItemCommand(pedido, coxinha, 1));

        assertThrows(IllegalStateException.class,
                () -> atendente.executar(new RemoverItemCommand(pedido, new Produto(99, "Empada", new BigDecimal("6.00")))));

        atendente.executar(new FinalizarPedidoCommand(pedido));
        assertThrows(IllegalStateException.class,
                () -> atendente.executar(new AdicionarItemCommand(pedido, cafe, 1)));

        assertEquals(
                List.of("Adicionar 1x Coxinha", "Finalizar pedido #1"),
                atendente.historico()
        );
    }

    private int buscarQuantidade(Produto produto) {
        return pedido.getItens().stream()
                .filter(i -> i.getProduto().equals(produto))
                .findFirst()
                .map(ItemPedido::getQuantidade)
                .orElse(0);
    }
}
