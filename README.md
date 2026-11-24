# Cantina — Projeto Final de Design Patterns

**Tema 3: Cantina** · Patterns: **Builder + Command**

## Descrição do sistema

Aplicação de console para registrar pedidos em uma cantina. O atendente:

1. consulta o cardápio;
2. monta um novo pedido passo a passo (cliente, consumo no local ou para viagem, observação e itens);
3. altera o pedido depois de criado (adicionar ou remover itens), podendo **desfazer** a última ação;
4. finaliza o pedido, que a partir daí não aceita mais alterações (a finalização também pode ser desfeita, reabrindo o pedido).

Os dados ficam apenas em memória, sem banco de dados.

### Entidades

| Entidade | Responsabilidade |
|---|---|
| `Produto` | Item do cardápio (código, nome, preço). |
| `Pedido` | Pedido de um cliente: tipo de consumo, observação, itens, status e total. |
| `ItemPedido` | Um produto dentro do pedido, com quantidade e subtotal. |

`Cardapio` é só a lista fixa de produtos disponíveis.

## Tecnologias e pré-requisitos

- **Java 17 ou superior** (JDK)
- **Maven 3.8+** (opcional: dá para rodar só com `javac`/`java`)
- Nenhuma dependência externa

## Como executar

### Com Maven

```bash
mvn compile exec:java
```

Demonstração automática do fluxo completo:

```bash
mvn compile exec:java -Dexec.args="--demo"
```

### Sem Maven (só com o JDK)

```bash
javac -d out $(find src -name "*.java")
java -cp out cantina.Main          # menu interativo
java -cp out cantina.Main --demo   # demonstração automática
```

No Windows (PowerShell):

```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out cantina.Main
```

## Exemplos de uso

### Menu interativo

```
=== Cantina ===
1 - Ver cardápio
2 - Novo pedido
3 - Listar pedidos
4 - Abrir pedido existente
0 - Sair
Opção: 2
Nome do cliente: Carlos
Consumo (1 - no local, 2 - para viagem): 1
Observação (Enter para nenhuma):
Código do produto (0 para terminar): 3
Quantidade: 1
Código do produto (0 para terminar): 0
Pedido criado!
Pedido #1 | Cliente: Carlos | LOCAL | ABERTO
  1x Xis salada = R$ 18,00
Total: R$ 18,00

--- Pedido #1 (ABERTO) ---
1 - Adicionar item
2 - Remover item
3 - Desfazer última ação
4 - Ver pedido
5 - Ver histórico de ações
6 - Finalizar pedido
0 - Voltar
```

### Demonstração (`--demo`)

```
=== DEMO: Builder ===
Pedido #1 | Cliente: Ana | VIAGEM | ABERTO
Observação: café sem açúcar
  2x Pão de queijo = R$ 9,00
  1x Café = R$ 5,00
Total: R$ 14,00

=== DEMO: Builder rejeita pedido sem itens ===
Erro esperado: O pedido precisa de pelo menos um item.

=== DEMO: Command ===
...
Desfazendo: Remover Pão de queijo
...
Tentando alterar pedido finalizado:
Erro esperado: O pedido #1 já foi finalizado.

Histórico de ações:
1. Adicionar 1x Coxinha
2. Adicionar 1x Suco natural
3. Finalizar pedido #1
```

## Design patterns utilizados

### Builder

**Classes:** `builder.PedidoBuilder` → constrói `model.Pedido`

**Problema resolvido:** um pedido tem campos obrigatórios (número, cliente, pelo menos um item) e campos opcionais (tipo de consumo, observação). Um construtor com todos esses parâmetros seria difícil de ler e de usar. O `PedidoBuilder` divide a criação em **passos encadeáveis**, e o `build()` valida tudo antes de criar o objeto. Assim, nunca existe um `Pedido` em estado inválido.

```java
Pedido pedido = new PedidoBuilder()
        .numero(1)
        .cliente("Ana")
        .paraViagem()
        .observacao("café sem açúcar")
        .adicionarItem(paoDeQueijo, 2)
        .adicionarItem(cafe, 1)
        .build();
```

**Como estender:** para criar um novo passo de montagem, adicione um método no `PedidoBuilder` que guarde o valor e retorne `this`, e repasse esse valor ao `Pedido` no `build()`.

### Command

**Classes:**

| Papel no pattern | Classe |
|---|---|
| Command (interface) | `command.Comando` (`executar()`, `desfazer()`, `descricao()`) |
| Comandos concretos | `AdicionarItemCommand`, `RemoverItemCommand`, `FinalizarPedidoCommand` |
| Invoker | `command.Atendente` (executa e guarda o histórico) |
| Receiver | `model.Pedido` |
| Client | `Main` (cria os comandos a partir das opções do menu) |

**Problema resolvido:** cada ação sobre o pedido vira um **objeto**. O `Atendente` executa qualquer comando sem saber o que ele faz, guarda o histórico e consegue **desfazer** a última ação. Cada comando sabe desfazer a si mesmo: por exemplo, `RemoverItemCommand` guarda o item removido para conseguir devolvê-lo. Um comando só entra no histórico se for executado sem erro.

**Como estender:** para criar um novo comando, implemente `Comando` com `executar()` e `desfazer()` e adicione uma opção no menu que o envie ao `Atendente`. O `Atendente` não precisa ser alterado.

## Estrutura do projeto

```
src/main/java/cantina/
├── Main.java                    # menu de console e modo --demo
├── model/
│   ├── Produto.java
│   ├── Pedido.java
│   ├── ItemPedido.java
│   └── Cardapio.java
├── builder/
│   └── PedidoBuilder.java       # Builder
└── command/
    ├── Comando.java             # Command (interface)
    ├── AdicionarItemCommand.java
    ├── RemoverItemCommand.java
    ├── FinalizarPedidoCommand.java
    └── Atendente.java           # Invoker
```

## Declaração de uso de IA

- **Modelo:** Claude Opus 5.5 (Anthropic), usado pelo Claude Code.
- **Onde foi usada:** geração da estrutura inicial do projeto (classes de modelo, `PedidoBuilder`, comandos, `Atendente`, menu de console) e rascunho deste README.
- **O que a equipe fez:** <!-- TODO: descrevam o que revisaram, ajustaram e testaram -->

## Contribuição dos integrantes

<!-- TODO: preencher com nome, usuário do GitHub e o que cada um fez -->

| Integrante | GitHub | Contribuição |
|---|---|---|
| Nome 1 | @usuario1 | |
| Nome 2 | @usuario2 | |
| Nome 3 | @usuario3 | |
| Nome 4 | @usuario4 | |
