package cantina;

import cantina.builder.PedidoBuilder;
import cantina.command.AdicionarItemCommand;
import cantina.command.AplicarCupomCommand;
import cantina.command.Atendente;
import cantina.command.Comando;
import cantina.command.FinalizarPedidoCommand;
import cantina.command.RemoverItemCommand;
import cantina.model.Cardapio;
import cantina.model.Cupom;
import cantina.model.ItemPedido;
import cantina.model.Pedido;
import cantina.model.Produto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Aplicação de console da cantina.
 *
 * Fluxo: ver cardápio -> montar pedido (Builder) -> alterar o pedido com
 * comandos, desfazendo se precisar (Command) -> finalizar.
 *
 * Execute com o argumento --demo para ver um fluxo completo automático.
 */
public class Main {

    private final Cardapio cardapio = new Cardapio();
    /** Pedidos em memória, cada um com o seu atendente (histórico próprio). */
    private final Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
    private final Map<Integer, Atendente> atendentes = new LinkedHashMap<>();
    private final Scanner scanner = new Scanner(System.in);
    private int proximoNumero = 1;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--demo")) {
            demo();
        } else {
            new Main().executar();
        }
    }

    // ------------------------------------------------------------------
    // Menu principal
    // ------------------------------------------------------------------

    private void executar() {
        System.out.println("=== Cantina ===");
        while (true) {
            System.out.println();
            System.out.println("1 - Ver cardápio");
            System.out.println("2 - Novo pedido");
            System.out.println("3 - Listar pedidos");
            System.out.println("4 - Abrir pedido existente");
            System.out.println("5 - Relatório do dia");
            System.out.println("0 - Sair");
            int opcao = lerInteiro("Opção: ");
            switch (opcao) {
                case 1 -> mostrarCardapio();
                case 2 -> novoPedido();
                case 3 -> listarPedidos();
                case 4 -> abrirPedidoExistente();
                case 5 -> relatorioDoDia();
                case 0 -> {
                    System.out.println("Até mais!");
                    return;
                }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void mostrarCardapio() {
        System.out.println("--- Cardápio ---");
        cardapio.getProdutos().forEach(System.out::println);
    }

    /** Monta o pedido passo a passo usando o Builder. */
    private void novoPedido() {
        PedidoBuilder builder = new PedidoBuilder().numero(proximoNumero);

        builder.cliente(lerTexto("Nome do cliente: "));

        int tipo = lerInteiro("Consumo (1 - no local, 2 - para viagem): ");
        if (tipo == 2) {
            builder.paraViagem();
        } else {
            builder.paraConsumirNoLocal();
        }

        builder.observacao(lerTexto("Observação (Enter para nenhuma): "));

        mostrarCardapio();
        while (true) {
            int codigo = lerInteiro("Código do produto (0 para terminar): ");
            if (codigo == 0) break;
            Optional<Produto> produto = cardapio.buscar(codigo);
            if (produto.isEmpty()) {
                System.out.println("Produto não encontrado.");
                continue;
            }
            int quantidade = lerInteiro("Quantidade: ");
            try {
                builder.adicionarItem(produto.get(), quantidade);
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }

        try {
            Pedido pedido = builder.build();
            pedidos.put(pedido.getNumero(), pedido);
            atendentes.put(pedido.getNumero(), new Atendente());
            proximoNumero++;
            System.out.println("Pedido criado!");
            System.out.println(pedido.resumo());
            atenderPedido(pedido);
        } catch (IllegalStateException e) {
            System.out.println("Não foi possível criar o pedido: " + e.getMessage());
        }
    }

    private void listarPedidos() {
        if (pedidos.isEmpty()) {
            System.out.println("Nenhum pedido ainda.");
            return;
        }
        pedidos.values().forEach(p -> System.out.printf("#%d - %s - %s - R$ %.2f%n",
                p.getNumero(), p.getCliente(), p.getStatus(), p.total()));
    }

    private void abrirPedidoExistente() {
        listarPedidos();
        if (pedidos.isEmpty()) return;
        Pedido pedido = pedidos.get(lerInteiro("Número do pedido: "));
        if (pedido == null) {
            System.out.println("Pedido não encontrado.");
            return;
        }
        atenderPedido(pedido);
    }

    /**
     * Relatório do dia: só consulta os pedidos em memória, não altera nada.
     * Faturamento e produto mais vendido contam apenas pedidos FINALIZADOS.
     */
    private void relatorioDoDia() {
        System.out.println("--- Relatório do dia ---");
        if (pedidos.isEmpty()) {
            System.out.println("Nenhum pedido ainda.");
            return;
        }

        int finalizados = 0;
        int emAberto = 0;
        BigDecimal faturamento = BigDecimal.ZERO;
        Map<Produto, Integer> quantidadeVendida = new LinkedHashMap<>();

        for (Pedido pedido : pedidos.values()) {
            if (pedido.getStatus() == Pedido.Status.FINALIZADO) {
                finalizados++;
                faturamento = faturamento.add(pedido.total());
                for (ItemPedido item : pedido.getItens()) {
                    quantidadeVendida.merge(item.getProduto(), item.getQuantidade(), Integer::sum);
                }
            } else {
                emAberto++;
            }
        }

        System.out.println("Pedidos finalizados: " + finalizados);
        System.out.println("Pedidos em aberto: " + emAberto);
        System.out.printf("Faturamento total: R$ %.2f%n", faturamento);

        Produto maisVendido = null;
        int maiorQuantidade = 0;
        for (Map.Entry<Produto, Integer> entrada : quantidadeVendida.entrySet()) {
            if (entrada.getValue() > maiorQuantidade) {
                maisVendido = entrada.getKey();
                maiorQuantidade = entrada.getValue();
            }
        }
        if (maisVendido == null) {
            System.out.println("Produto mais vendido: nenhum pedido finalizado ainda.");
        } else {
            System.out.println("Produto mais vendido: " + maisVendido.getNome()
                    + " (" + maiorQuantidade + " unidades)");
        }
    }

    // ------------------------------------------------------------------
    // Atendimento de um pedido: cada ação é um Command
    // ------------------------------------------------------------------

    private void atenderPedido(Pedido pedido) {
        Atendente atendente = atendentes.get(pedido.getNumero());
        while (true) {
            System.out.println();
            System.out.println("--- Pedido #" + pedido.getNumero() + " (" + pedido.getStatus() + ") ---");
            System.out.println("1 - Adicionar item");
            System.out.println("2 - Remover item");
            System.out.println("3 - Desfazer última ação");
            System.out.println("4 - Ver pedido");
            System.out.println("5 - Ver histórico de ações");
            System.out.println("6 - Finalizar pedido");
            System.out.println("7 - Aplicar cupom de desconto");
            System.out.println("0 - Voltar");
            int opcao = lerInteiro("Opção: ");
            try {
                switch (opcao) {
                    case 1 -> {
                        Produto produto = escolherProduto();
                        if (produto != null) {
                            int quantidade = lerInteiro("Quantidade: ");
                            executar(atendente, new AdicionarItemCommand(pedido, produto, quantidade));
                        }
                    }
                    case 2 -> {
                        Produto produto = escolherProduto();
                        if (produto != null) {
                            executar(atendente, new RemoverItemCommand(pedido, produto));
                        }
                    }
                    case 3 -> {
                        Comando desfeito = atendente.desfazerUltimo();
                        System.out.println("Desfeito: " + desfeito.descricao());
                    }
                    case 4 -> System.out.println(pedido.resumo());
                    case 5 -> mostrarHistorico(atendente.historico());
                    case 6 -> executar(atendente, new FinalizarPedidoCommand(pedido));
                    case 7 -> executar(atendente, new AplicarCupomCommand(pedido, lerCupom()));
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalStateException | IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    private void executar(Atendente atendente, Comando comando) {
        atendente.executar(comando);
        System.out.println("OK: " + comando.descricao());
    }

    private Produto escolherProduto() {
        mostrarCardapio();
        Optional<Produto> produto = cardapio.buscar(lerInteiro("Código do produto: "));
        if (produto.isEmpty()) {
            System.out.println("Produto não encontrado.");
            return null;
        }
        return produto.get();
    }

    private Cupom lerCupom() {
        return Cupom.builder()
                .codigo(lerTexto("Código do cupom: "))
                .percentual(lerInteiro("Percentual de desconto: "))
                .build();
    }

    private static void mostrarHistorico(List<String> historico) {
        if (historico.isEmpty()) {
            System.out.println("Nenhuma ação registrada.");
            return;
        }
        for (int i = 0; i < historico.size(); i++) {
            System.out.println((i + 1) + ". " + historico.get(i));
        }
    }

    // ------------------------------------------------------------------
    // Leitura do teclado
    // ------------------------------------------------------------------

    private int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            if (!scanner.hasNextLine()) {
                System.exit(0);
            }
            String linha = scanner.nextLine().trim();
            try {
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.println("Digite um número.");
            }
        }
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        if (!scanner.hasNextLine()) {
            System.exit(0);
        }
        return scanner.nextLine();
    }

    // ------------------------------------------------------------------
    // Demonstração automática (java ... cantina.Main --demo)
    // ------------------------------------------------------------------

    private static void demo() {
        Cardapio cardapio = new Cardapio();
        Produto paoDeQueijo = cardapio.buscar(1).orElseThrow();
        Produto coxinha = cardapio.buscar(2).orElseThrow();
        Produto cafe = cardapio.buscar(4).orElseThrow();
        Produto suco = cardapio.buscar(5).orElseThrow();

        System.out.println("=== DEMO: Builder ===");
        Pedido pedido = new PedidoBuilder()
                .numero(1)
                .cliente("Ana")
                .paraViagem()
                .observacao("café sem açúcar")
                .adicionarItem(paoDeQueijo, 2)
                .adicionarItem(cafe, 1)
                .build();
        System.out.println(pedido.resumo());

        System.out.println();
        System.out.println("=== DEMO: Builder rejeita pedido sem itens ===");
        try {
            new PedidoBuilder().numero(2).cliente("Bruno").build();
        } catch (IllegalStateException e) {
            System.out.println("Erro esperado: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== DEMO: Command ===");
        Atendente atendente = new Atendente();
        atendente.executar(new AdicionarItemCommand(pedido, coxinha, 1));
        atendente.executar(new AdicionarItemCommand(pedido, suco, 1));
        atendente.executar(new RemoverItemCommand(pedido, paoDeQueijo));
        System.out.println(pedido.resumo());

        System.out.println();
        System.out.println("Desfazendo: " + atendente.desfazerUltimo().descricao());
        System.out.println(pedido.resumo());

        System.out.println();
        atendente.executar(new FinalizarPedidoCommand(pedido));
        System.out.println(pedido.resumo());

        System.out.println();
        System.out.println("Tentando alterar pedido finalizado:");
        try {
            atendente.executar(new AdicionarItemCommand(pedido, coxinha, 1));
        } catch (IllegalStateException e) {
            System.out.println("Erro esperado: " + e.getMessage());
        }

        System.out.println();
        System.out.println("Histórico de ações:");
        mostrarHistorico(atendente.historico());
    }
}
