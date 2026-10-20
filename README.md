# SOLID #1 – Single Responsibility Principle (SRP)

Repositório educacional que ilustra o **Princípio da Responsabilidade Única (SRP)** através de um exemplo prático e comparável: um sistema de pedidos de e-commerce implementado de duas formas.

## 🎯 Objetivo

Mostrar, de forma prática, o mesmo sistema implementado de duas maneiras:

- **`before/`** — Código que **viola o SRP** (uma única classe `Pedido` faz tudo)
- **`after/`** — Código **refatorado**, onde cada classe tem um único motivo para mudar

## 📖 O que é o SRP

> "Um módulo deve ter um, e apenas um, motivo para mudar." — Robert C. Martin (Uncle Bob)

Em *Clean Architecture*, Martin refina: **um módulo deve ser responsável por um, e apenas um, ator** (grupo de pessoas ou área que pede mudanças naquele código).

### Pontos-chave

- **SRP ≠ "fazer uma única coisa"** — Uma classe pode ter vários métodos coesos, desde que todos sirvam à mesma responsabilidade
- **Responsabilidade = motivo de mudança** — Se regras fiscais, formato de relatório e banco de dados mudam por razões e pessoas diferentes, não devem estar na mesma classe
- **Coesão alta, acoplamento baixo** — Agrupe o que muda junto; separe o que muda por razões diferentes

## 🛒 Cenário: Sistema de Pedidos (E-commerce)

Um domínio simples com responsabilidades claramente distintas:

| Responsabilidade | Ator (quem pede mudança) | Exemplo de mudança |
|------------------|--------------------------|-------------------|
| Regras de negócio (total, desconto) | Área Comercial | Nova regra de desconto |
| Persistência | Infraestrutura / DBA | Trocar banco de dados |
| Notificação ao cliente | Marketing | Novo canal (SMS, WhatsApp) |
| Geração de relatório/nota | Financeiro / Fiscal | Novo layout, novo formato |

## 📁 Estrutura do Projeto

```
solid-01-srp/
├── before/                    # Versão que VIOLA o SRP
│   └── src/main/java/com/example/solid/before/
│       ├── Item.java
│       ├── Cliente.java
│       └── Pedido.java        # ← Faz tudo: cálculo, persistência, e-mail, relatório
│   └── src/test/.../PedidoTest.java
│
├── after/                     # Versão que RESPEITA o SRP
│   └── src/main/java/com/example/solid/after/
│       ├── domain/            # Regras de negócio
│       │   ├── Item.java
│       │   ├── Cliente.java
│       │   └── Pedido.java    # ← Só regras de negócio
│       ├── infra/             # Persistência
│       │   └── PedidoRepository.java
│       ├── notification/      # Notificação
│       │   └── NotificadorDePedido.java
│       ├── report/            # Relatório
│       │   └── GeradorDeNota.java
│       └── application/       # Orquestração do caso de uso
│           └── FinalizarPedido.java
│   └── src/test/.../
│       ├── domain/PedidoTest.java
│       └── application/FinalizarPedidoTest.java
│
├── docs/
│   └── srp.md                 # Documentação detalhada do SRP
└── README.md                  # Este arquivo
```

## 🔴 Versão `before` — Violação do SRP

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

**Problemas:**
- 4 motivos para mudar
- Alterar layout da nota pode quebrar cálculo do total
- Trocar SMTP obriga recompilar e retestar regra de negócio
- Testar `calcularTotal` exige lidar com banco e e-mail

## 🟢 Versão `after` — Respeitando o SRP

```java
// domain/Pedido.java — Só regra de negócio
public class Pedido {
    public double calcularTotal() { ... }
}

// infra/PedidoRepository.java — Só persistência
public class PedidoRepository {
    public void salvar(Pedido pedido) { ... }
}

// notification/NotificadorDePedido.java — Só notificação
public class NotificadorDePedido {
    public void enviarConfirmacao(Pedido pedido) { ... }
}

// report/GeradorDeNota.java — Só relatório
public class GeradorDeNota {
    public String gerarHtml(Pedido pedido) { ... }
}

// application/FinalizarPedido.java — Orquestra o caso de uso
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

**Benefícios:**
- Cada classe tem **um único motivo para mudar**
- `FinalizarPedido` não viola SRP: sua única responsabilidade é coordenar o fluxo
- Dependências injetadas pelo construtor (prepara para o **D** do SOLID)
- Regra de negócio testável **sem mocks** de infraestrutura

## 🧪 Testes que Provam o Ganho

| Teste | Antes (`before`) | Depois (`after`) |
|-------|------------------|------------------|
| Calcular total com desconto VIP | Precisa de banco/SMTP simulados | **Teste puro, sem dependências** |
| Trocar e-mail por SMS | Altera a classe `Pedido` | **Nova implementação de notificador** |
| Mudar layout da nota | Risco de afetar o cálculo | **Só `GeradorDeNota` é alterada** |

## ▶️ Como Executar

### Pré-requisitos
- Java 17+
- Maven ou Gradle

### Compilar e testar versão `before`
```bash
cd before
# Com Maven
mvn test

# Ou compilar manualmente
javac -d out src/main/java/com/example/solid/before/*.java
javac -cp out:lib/junit-jupiter-api-5.9.0.jar -d test-out src/test/java/com/example/solid/before/PedidoTest.java
java -cp test-out:out:lib/* org.junit.platform.console.ConsoleLauncher --scan-classpath
```

### Compilar e testar versão `after`
```bash
cd after
mvn test
```

## ✅ Checklist de Revisão do SRP

- [ ] Consigo descrever cada classe em uma frase **sem usar "e"**?
- [ ] Cada classe responde a um único ator/área?
- [ ] Mudar o layout da nota ou o banco deixa a regra de negócio intacta?
- [ ] A regra de negócio é testável sem infraestrutura?
- [ ] Os nomes revelam a intenção (sem `Manager`, `Helper`, `Utils`)?
- [ ] Os mesmos testes de aceitação passam em `before` e `after`?

## 📚 Referências

- Robert C. Martin — *Clean Code* (Cap. 10, Classes)
- Robert C. Martin — *Clean Architecture* (Cap. 7, SRP)
- Robert C. Martin — "The Single Responsibility Principle" (blog Clean Coder)
- Martin Fowler — Catálogo de refatorações (*Extract Class, Move Method*)

## 🔜 Próximo Passo

Com o SRP implementado, o próximo princípio **O – Open/Closed Principle (OCP)** pode reaproveitar o mesmo domínio de pedidos, mostrando como estender comportamento sem modificar código existente.