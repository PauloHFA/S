# SRP — Single Responsibility Principle

Documentação base para o repositório que ilustra o **Princípio da Responsabilidade Única.**

---

## 1. Objetivo do repositório

Mostrar, de forma prática e comparável, o mesmo sistema implementado de duas maneiras:

**Antes:** código que viola o SRP (classe que faz tudo).

**Depois:** código refatorado, em que cada classe tem um único motivo para mudar.

Quem ler o repositório deve conseguir ver o problema, entender o porquê dele e enxergar o ganho em testabilidade, manutenção e leitura.

---

## 2. O que é o SRP

> "Um módulo deve ter um, e apenas um, motivo para mudar." — Robert C. Martin (Uncle Bob)

Em *Clean Architecture,* Martin refina a definição: **um módulo deve ser responsável por um, e apenas um, ator** (grupo de pessoas ou área que pede mudanças naquele código).

### Pontos que costumam gerar confusão

**SRP não significa "fazer uma única coisa"** ou "ter um único método". Uma classe pode ter vários métodos coesos, desde que todos sirvam à mesma responsabilidade.

**Responsabilidade = motivo de mudança.** Se regras fiscais, formato de relatório e banco de dados mudam por razões e por pessoas diferentes, não devem estar na mesma classe.

**Coesão alta, acoplamento baixo.** Agrupe o que muda junto; separe o que muda por razões diferentes.

---

## 3. Como identificar uma violação (code smells)

- Classe com nome genérico: `Manager`, `Helper`, `Utils`, `Service` que faz de tudo.
- Nome da classe só se explica usando "e" (ex.: "calcula o total **e** envia e-mail").
- Imports de camadas muito diferentes na mesma classe (banco, HTTP, e-mail, formatação).
- Testes unitários que exigem muitos mocks para testar uma regra simples.
- Um mesmo arquivo alterado por pedidos de mudança que não têm relação entre si.
- Classes com centenas de linhas e muitos métodos que não usam os mesmos atributos.

---

## 4. Cenário proposto: sistema de pedidos (e-commerce)

Um domínio simples e que todo mundo entende, com responsabilidades claramente distintas:

| Responsabilidade | Quem pede mudança (ator) | Exemplo de mudança |
| --- | --- | --- |
| Regras de negócio do pedido (total, desconto) | Área comercial | Nova regra de desconto |
| Persistência | Time de infraestrutura / DBA | Trocar banco de dados |
| Notificação ao cliente | Marketing | Novo texto ou novo canal (SMS) |
| Geração de relatório/nota | Área financeira / fiscal | Novo layout, novo formato |

### 4.1 Versão que VIOLA o SRP

```java
public class Pedido {
    // 1) Regra de negócio
    public double calcularTotal() { ... }

    // 2) Persistência
    public void salvar() { ... }

    // 3) Notificação
    public void enviarEmailConfirmacao() { ... }

    // 4) Relatório
    public String gerarNotaHtml() { ... }
}
```

**Por que é um problema?** Essa classe tem quatro motivos para mudar. Alterar o layout da nota pode quebrar o cálculo do total; trocar o SMTP obriga a recompilar e retestar a regra de negócio; e testar `calcularTotal` exige lidar com banco e e-mail.

### 4.2 Versão que RESPEITA o SRP

```java
// domain/Pedido.java — só regra de negócio
public class Pedido {
    public double calcularTotal() { ... }
}

// infra/PedidoRepository.java — só persistência
public class PedidoRepository {
    public void salvar(Pedido pedido) { ... }
}

// notification/NotificadorDePedido.java — só notificação
public class NotificadorDePedido {
    public void enviarConfirmacao(Pedido pedido) { ... }
}

// report/GeradorDeNota.java — só relatório
public class GeradorDeNota {
    public String gerarHtml(Pedido pedido) { ... }
}

// application/FinalizarPedido.java — orquestra o caso de uso
public class FinalizarPedido {
    private final PedidoRepository repo;
    private final NotificadorDePedido notificador;

    public FinalizarPedido(PedidoRepository repo, NotificadorDePedido notificador) {
        this.repo = repo;
        this.notificador = notificador;
    }

    public void executar(Pedido pedido) {
        repo.salvar(pedido);
        notificador.enviarConfirmacao(pedido);
    }
}
```

A classe `FinalizarPedido` **não viola** o SRP: sua única responsabilidade é coordenar o fluxo do caso de uso, sem conhecer os detalhes de cada etapa.

---

## 5. Estrutura sugerida do repositório

```
solid-01-srp/
├── README.md
├── docs/
│ └── srp.md
├── before/
│ ├── src/
│ └── tests/
├── after/
│ ├── src/
│ │ ├── domain/
│ │ ├── infra/
│ │ ├── notification/
│ │ ├── report/
│ │ └── application/
│ └── tests/
└── CHANGELOG.md
```

**Alternativa:** manter uma única base de código e usar *commits* ou *tags* (`v1-violacao`, `v2-srp`) para mostrar a evolução. Pastas lado a lado costumam ser mais fáceis de comparar.

---

## 6. Requisitos de implementação

- Mesmo comportamento externo nas duas versões (mesmos testes de aceitação passando em ambas).
- Cada classe da versão `after` deve caber em uma frase **sem usar "e"** para descrever sua responsabilidade.
- Dependências injetadas pelo construtor (prepara o terreno para o **D** do SOLID).
- Testes unitários da regra de negócio **sem mocks** de banco ou e-mail.
- Nomes expressivos, sem sufixos genéricos como `Manager` ou `Helper`.
- Código simples, sem frameworks desnecessários, para o foco ficar no princípio.

---

## 7. Testes que provam o ganho

| Teste | Antes | Depois |
| --- | --- | --- |
| Calcular total com desconto VIP | Precisa de banco/SMTP simulados | Teste puro, sem dependências |
| Trocar e-mail por SMS | Altera a classe Pedido | Nova implementação de notificador |
| Mudar layout da nota | Risco de afetar o cálculo | Só `GeradorDeNota` é alterada |

---

## 8. Cuidados: o outro extremo

**Fragmentação excessiva:** criar uma classe para cada linha de código gera complexidade sem benefício. Separe por *motivos de mudança reais,* não por tamanho.

**Não antecipe mudanças que nunca virão.** Se hoje só existe um canal de notificação e não há indício de outro, separar já é suficiente; criar abstrações elaboradas é exagero.

**Cuidado com o "anêmico":** separar responsabilidades não significa esvaziar o domínio. A regra de negócio deve continuar na entidade.

---

## 9. Checklist de revisão

- [ ] Consigo descrever cada classe em uma frase sem "e"?
- [ ] Cada classe responde a um único ator/área?
- [ ] Mudar o layout da nota ou o banco deixa a regra de negócio intacta?
- [ ] A regra de negócio é testável sem infraestrutura?
- [ ] Os nomes revelam a intenção?
- [ ] Os mesmos testes de aceitação passam em `before` e `after`?
- [ ] O README explica o problema, a solução e como rodar?

---

## 10. Modelo de README do repositório

1. **Título e objetivo** — qual princípio é ilustrado.
2. **Conceito** — definição do SRP em poucas linhas.
3. **Problema** — o que está errado em `before` (com trecho de código).
4. **Solução** — como `after` resolve (com diagrama simples de classes, se possível).
5. **Como executar** — instalação, comandos de build e testes.
6. **Comparativo** — tabela antes vs. depois.
7. **Referências.**

---

## 11. Referências

- Robert C. Martin — *Clean Code* (cap. 10, Classes) e *Clean Architecture* (cap. 7, SRP).
- Robert C. Martin — "The Single Responsibility Principle" (blog Clean Coder).
- Martin Fowler — Catálogo de refatorações (*Extract Class, Move Method*).

---

## 12. Próximo passo

Com o SRP implementado, o repositório seguinte **(O – Open/Closed)** pode reaproveitar o mesmo domínio de pedidos, mostrando como estender comportamento sem modificar código existente.