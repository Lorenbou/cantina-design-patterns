package cantina.builder;

import cantina.model.ItemPedido;
import cantina.model.Pedido;
import cantina.model.Pedido.TipoConsumo;
import cantina.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoBuilderTest {

    private final Produto paoDeQueijo = new Produto(1, "Pão de queijo", new BigDecimal("4.50"));
    private final Produto xisSalada = new Produto(3, "Xis salada", new BigDecimal("18.00"));

    @Test
    void pedidoValidoEhCriadoComClienteTipoEItensCertos() {
        Pedido pedido = new PedidoBuilder()
                .numero(1)
                .cliente("Ana")
                .paraViagem()
                .adicionarItem(paoDeQueijo, 2)
                .adicionarItem(xisSalada, 1)
                .build();

        assertEquals(1, pedido.getNumero());
        assertEquals("Ana", pedido.getCliente());
        assertEquals(TipoConsumo.VIAGEM, pedido.getTipoConsumo());
        assertEquals(Pedido.Status.ABERTO, pedido.getStatus());

        assertEquals(2, pedido.getItens().size());
        ItemPedido primeiro = pedido.getItens().get(0);
        assertEquals(paoDeQueijo, primeiro.getProduto());
        assertEquals(2, primeiro.getQuantidade());
        ItemPedido segundo = pedido.getItens().get(1);
        assertEquals(xisSalada, segundo.getProduto());
        assertEquals(1, segundo.getQuantidade());
    }

    @Test
    void tipoDeConsumoPadraoEhLocal() {
        Pedido pedido = new PedidoBuilder()
                .numero(1)
                .cliente("Ana")
                .adicionarItem(paoDeQueijo, 1)
                .build();

        assertEquals(TipoConsumo.LOCAL, pedido.getTipoConsumo());
    }

    @Test
    void buildSemClienteLancaErro() {
        PedidoBuilder builder = new PedidoBuilder()
                .numero(1)
                .adicionarItem(paoDeQueijo, 1);

        IllegalStateException erro = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("O pedido precisa do nome do cliente.", erro.getMessage());
    }

    @Test
    void buildComClienteEmBrancoLancaErro() {
        PedidoBuilder builder = new PedidoBuilder()
                .numero(1)
                .cliente("   ")
                .adicionarItem(paoDeQueijo, 1);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void buildSemItensLancaErro() {
        PedidoBuilder builder = new PedidoBuilder()
                .numero(1)
                .cliente("Ana");

        IllegalStateException erro = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("O pedido precisa de pelo menos um item.", erro.getMessage());
    }

    @Test
    void mesmoProdutoAdicionadoDuasVezesSomaAQuantidade() {
        Pedido pedido = new PedidoBuilder()
                .numero(1)
                .cliente("Ana")
                .adicionarItem(paoDeQueijo, 2)
                .adicionarItem(paoDeQueijo, 3)
                .build();

        assertEquals(1, pedido.getItens().size());
        assertEquals(paoDeQueijo, pedido.getItens().get(0).getProduto());
        assertEquals(5, pedido.getItens().get(0).getQuantidade());
    }

    @Test
    void totalDoPedidoEstaCerto() {
        Pedido pedido = new PedidoBuilder()
                .numero(1)
                .cliente("Ana")
                .adicionarItem(paoDeQueijo, 2)   // 2 x 4,50  =  9,00
                .adicionarItem(xisSalada, 1)     // 1 x 18,00 = 18,00
                .build();

        assertEquals(0, new BigDecimal("27.00").compareTo(pedido.total()),
                "total esperado 27.00, mas foi " + pedido.total());
    }
}
