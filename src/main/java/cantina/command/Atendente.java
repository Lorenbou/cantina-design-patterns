package cantina.command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Pattern COMMAND: invocador.
 *
 * Executa os comandos e guarda o histórico do que deu certo, o que permite
 * desfazer a última ação. O atendente não sabe o que cada comando faz:
 * ele só chama executar() e desfazer().
 */
public class Atendente {

    private final Deque<Comando> historico = new ArrayDeque<>();

    /** Executa o comando; só entra no histórico se não lançar erro. */
    public void executar(Comando comando) {
        comando.executar();
        historico.push(comando);
    }

    /** Desfaz o último comando executado. Retorna o comando desfeito. */
    public Comando desfazerUltimo() {
        if (historico.isEmpty()) {
            throw new IllegalStateException("Não há ações para desfazer.");
        }
        Comando comando = historico.pop();
        comando.desfazer();
        return comando;
    }

    /** Histórico do mais antigo para o mais recente. */
    public List<String> historico() {
        List<String> descricoes = new ArrayList<>();
        historico.descendingIterator().forEachRemaining(c -> descricoes.add(c.descricao()));
        return descricoes;
    }
}
