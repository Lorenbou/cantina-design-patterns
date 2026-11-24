package cantina.command;

/**
 * Pattern COMMAND: interface comum a todas as ações sobre um pedido.
 *
 * Cada ação (adicionar item, remover item, finalizar...) vira um objeto
 * que sabe se executar e se desfazer. Assim o {@link Atendente} consegue
 * executar qualquer ação, guardar o histórico e desfazer a última, sem
 * conhecer os detalhes de cada uma.
 */
public interface Comando {

    void executar();

    void desfazer();

    /** Texto curto usado no histórico, ex.: "Adicionar 2x Coxinha". */
    String descricao();
}
