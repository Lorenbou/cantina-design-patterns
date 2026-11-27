# Cantina — Projeto Final de Design Patterns

**Tema 3: Cantina** · Patterns: **Builder + Command**

## Descrição do sistema

Aplicação de console para registrar pedidos em uma cantina. O atendente:

1. consulta o cardápio;
2. monta um novo pedido passo a passo (cliente, consumo no local ou para viagem, observação e itens);
3. altera o pedido depois de criado (adicionar ou remover itens, aplicar cupom de desconto), podendo **desfazer** a última ação;
4. finaliza o pedido, que a partir daí não aceita mais alterações (a finalização também pode ser desfeita, reabrindo o pedido).

Os dados ficam apenas em memória, sem banco de dados.

### Entidades

| Entidade | Responsabilidade |
|---|---|
| `Produto` | Item do cardápio (código, nome, preço). |
| `Pedido` | Pedido de um cliente: tipo de consumo, observação, itens, status e total. |
| `ItemPedido` | Um produto dentro do pedido, com quantidade e subtotal. |
| `Cupom` | Cupom de desconto do pedido (código e porcentagem). |

`Cardapio` é só a lista fixa de produtos disponíveis.

## Tecnologias e pré-requisitos

- **Java 17 ou superior** (JDK)
- **Maven 3.8+** (opcional para executar: dá para rodar só com `javac`/`java`; necessário para rodar os testes)
- A aplicação não tem dependências externas; os testes usam **JUnit 5**, baixado automaticamente pelo Maven

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
javac -d out $(find src/main -name "*.java")
java -cp out cantina.Main          # menu interativo
java -cp out cantina.Main --demo   # demonstração automática
```

No Windows (PowerShell):

```powershell
javac -d out (Get-ChildItem -Recurse src/main -Filter *.java).FullName
java -cp out cantina.Main
```

Os comandos compilam só `src/main`, porque os testes em `src/test` dependem do JUnit.

## Como testar

Os testes automatizados usam JUnit 5 e rodam pelo Maven:

```bash
mvn test
```

`PedidoBuilderTest` verifica o Builder: criação de um pedido válido, erro ao montar sem cliente ou sem itens, soma da quantidade quando o mesmo produto é adicionado duas vezes e cálculo do total.

`ComandosTest` verifica o Command: adicionar/remover itens e desfazer (devolvendo o item removido com a mesma quantidade), finalizar e reabrir pedido ao desfazer, erro ao desfazer com histórico vazio e garantia de que comandos que falham não entram no histórico.

## Exemplos de uso

### Menu interativo

```
=== Cantina ===
1 - Ver cardápio
2 - Novo pedido
3 - Listar pedidos
4 - Abrir pedido existente
5 - Relatório do dia
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
7 - Aplicar cupom de desconto
0 - Voltar
Opção: 7
Código do cupom: aluno10
Percentual de desconto: 10
OK: Aplicar cupom ALUNO10 (-10%)
...
Opção: 4
Pedido #1 | Cliente: Carlos | LOCAL | ABERTO
  1x Xis salada = R$ 18,00
Cupom: ALUNO10 (-10%) = - R$ 1,80
Total: R$ 16,20
...
Opção: 3
Desfeito: Aplicar cupom ALUNO10 (-10%)
```

### Cupom de desconto (opção 7)

Pede o código do cupom e a porcentagem de desconto, e aplica no pedido. O código fica sempre em maiúsculo e a porcentagem tem que ser de 1 a 100. Se aplicar outro cupom, ele substitui o que estava antes. Desfazendo (opção 3), volta o cupom anterior, ou o pedido fica sem cupom se não tinha nenhum. Depois de finalizado, o pedido não aceita mais cupom.

O total do pedido já sai com o desconto, então a listagem e o relatório do dia também mostram o valor com desconto.

### Relatório do dia (opção 5)

Apresenta um resumo de todos os pedidos registrados desde a abertura do sistema, incluindo a quantidade de pedidos concluídos, a quantidade que ainda está em andamento, o valor total das vendas e o item mais vendido.

Importante: o cálculo do faturamento e a identificação do produto mais vendido levam em conta somente os pedidos com status FINALIZADO, já que os pedidos em aberto ainda podem sofrer alterações ou ter ações desfeitas.

Exemplo:

Pedido finalizado: Carlos – 1 Xis Salada + 2 Pães de Queijo.

Pedido em aberto: Bia – 3 Pães de Queijo.

```
Opção: 5
--- Relatório do dia ---
Pedidos finalizados: 1
Pedidos em aberto: 1
Faturamento total: R$ 27,00
Produto mais vendido: Pão de queijo (2 unidades)
```

Sem nenhum pedido, mostra `Nenhum pedido ainda.`

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

Na extensão do cupom foi criado mais um builder, o `Cupom.Builder` (dentro de `model.Cupom`). Ele funciona igual ao do pedido: o cupom só pode ser criado por `Cupom.builder()`, e o `build()` não deixa criar sem código ou com porcentagem fora de 1 a 100.

```java
Cupom cupom = Cupom.builder()
        .codigo("aluno10")
        .percentual(10)
        .build();
```

### Command

**Classes:**

| Papel no pattern | Classe |
|---|---|
| Command (interface) | `command.Comando` (`executar()`, `desfazer()`, `descricao()`) |
| Comandos concretos | `AdicionarItemCommand`, `RemoverItemCommand`, `FinalizarPedidoCommand`, `AplicarCupomCommand` |
| Invoker | `command.Atendente` (executa e guarda o histórico) |
| Receiver | `model.Pedido` |
| Client | `Main` (cria os comandos a partir das opções do menu) |

**Problema resolvido:** cada ação sobre o pedido vira um **objeto**. O `Atendente` executa qualquer comando sem saber o que ele faz, guarda o histórico e consegue **desfazer** a última ação. Cada comando sabe desfazer a si mesmo: por exemplo, `RemoverItemCommand` guarda o item removido para conseguir devolvê-lo. Um comando só entra no histórico se for executado sem erro.

**Como estender:** para criar um novo comando, implemente `Comando` com `executar()` e `desfazer()` e adicione uma opção no menu que o envie ao `Atendente`. O `Atendente` não precisa ser alterado.

## Extensão: cupom de desconto

Como extensão, adicionamos um cupom de desconto no pedido. O cupom tem um código (por exemplo `ALUNO10`) e uma porcentagem de desconto, que é calculada em cima do subtotal dos itens. No menu do pedido tem a opção 7 para aplicar o cupom, e dá para desfazer com a opção 3 como as outras ações.

No Builder, criamos o `Cupom` do mesmo jeito que o `Pedido`: o construtor é privado e o cupom só é criado pelo `Cupom.builder()`. A validação fica no `build()`, que não aceita cupom sem código nem porcentagem fora de 1 a 100.

No Command, criamos o `AplicarCupomCommand`. Quando executa, ele guarda o cupom que o pedido tinha antes e aplica o novo. No desfazer, ele coloca de volta o cupom antigo, ou tira o cupom se o pedido não tinha nenhum. Com isso dá para aplicar vários cupons e ir desfazendo um por um. Não precisamos mexer no `Atendente`, só criar o comando e colocar a opção no menu.

No `Pedido`, entraram os métodos `aplicarCupom()`, `removerCupom()`, `getCupom()`, `subtotal()` e `desconto()`. O `total()` agora é o subtotal menos o desconto, e o resumo do pedido mostra uma linha com o cupom. Assim como os itens, o cupom só pode ser mudado enquanto o pedido está aberto.

## Estrutura do projeto

```
src/main/java/cantina/
├── Main.java                    # menu de console e modo --demo
├── model/
│   ├── Produto.java
│   ├── Pedido.java
│   ├── ItemPedido.java
│   ├── Cardapio.java
│   └── Cupom.java               # cupom de desconto e seu Builder
├── builder/
│   └── PedidoBuilder.java       # Builder
└── command/
    ├── Comando.java             # Command (interface)
    ├── AdicionarItemCommand.java
    ├── RemoverItemCommand.java
    ├── FinalizarPedidoCommand.java
    ├── AplicarCupomCommand.java # aplica o cupom no pedido
    └── Atendente.java           # Invoker

src/test/java/cantina/
├── builder/
│   └── PedidoBuilderTest.java   # testes do Builder (JUnit 5)
└── command/
    └── ComandosTest.java        # testes do Command (JUnit 5)
```

## Declaração de uso de IA

- **Modelos:** Claude Opus 5.5, Claude Fable 5.2 (Anthropic) e Gemini (Google / Antigravity).
- **Onde foi usada:**
  - Claude Opus 5.5: geração da estrutura inicial do projeto (classes de modelo, `PedidoBuilder`, comandos, `Atendente`, menu de console) e rascunho deste README.
  - Claude Fable 5.2: configuração do JUnit 5 no `pom.xml`, geração dos testes do Builder (`PedidoBuilderTest`) e atualização deste README com a seção de testes.
  - Gemini (Antigravity): geração dos testes do Command (`ComandosTest`) e atualização deste README.
  - Claude Opus 5.5: orientação e geração do método `relatorioDoDia()` (opção "5 - Relatório do dia" no `Main.java`).


## Contribuição dos integrantes

### Equipe dona

| Integrante | GitHub | Contribuição |
|---|---|---|
| Lorenzo Severo Gotuzzo | [@Lorenbou](https://github.com/Lorenbou) | Criação do repositório e configuração do projeto Maven; implementação das entidades (`Produto`, `Pedido`, `ItemPedido`, `Cardapio`), do Builder (`PedidoBuilder`), do Command (`Comando`, comandos concretos e `Atendente`) e do menu de console; README. |
| Gabriel Minatto Guedin | [@minattinho](https://github.com/minattinho) | Revisão do código; configuração do JUnit 5 no `pom.xml` (dependência `junit-jupiter` e `maven-surefire-plugin`); testes automatizados do Builder em `PedidoBuilderTest` (pedido válido com cliente, tipo e itens corretos, erro ao montar sem cliente ou sem itens, soma da quantidade de produto repetido e cálculo do total). |
| Anderson Santos | [@andsantosx](https://github.com/andsantosx) | Revisão do código e testes de execução (`--demo` e menu interativo); testes automatizados do Command em `ComandosTest` (`AdicionarItemCommand`, `RemoverItemCommand`, `FinalizarPedidoCommand`, `Atendente` e histórico sem comandos com erro) e atualização do README. |
| Isabela Madeira José | [@isabelamadeirajose](https://github.com/isabelamadeirajose) | Opção "5 - Relatório do dia" no menu principal (`relatorioDoDia()` no `Main.java`): quantidade de pedidos finalizados e em aberto, faturamento total dos pedidos finalizados e produto mais vendido; testes manuais no menu; atualização do README (exemplo de uso do relatório). |

### Equipe extensora

| Integrante | GitHub | Contribuição |
|---|---|---|
| Mateus Leal Hemkemeier | [@mateuslh](https://github.com/mateuslh) | Cupom de desconto: classe `Cupom` com Builder, `AplicarCupomCommand` (com desfazer), alterações no `Pedido` para calcular subtotal, desconto e total, opção "7 - Aplicar cupom de desconto" no menu e atualização do README. |
