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
- **OCP/DIP ready**: `Notificador` é uma interface, permite `NotificadorSMS`, `NotificadorWhatsApp` sem modificar `FinalizarPedido`

## 📊 Diagrama de Arquitetura (Versão `after`)

```mermaid
graph TD
    subgraph Application["Application Layer"]
        FP[FinalizarPedido\nOrquestra caso de uso]
    end
    
    subgraph Domain["Domain Layer (Regras de Negocio)"]
        P[Pedido\ncalcularTotal()]
        I[Item]
        C[Cliente]
    end
    
    subgraph Infra["Infrastructure Layer"]
        PR[PedidoRepository\nsalvar()]
    end
    
    subgraph Notification["Notification Layer"]
        N[Notificador (interface)]
        NE[NotificadorDePedido\nE-mail]
        NS[NotificadorSMS\nSMS]
        NW[NotificadorWhatsApp\nWhatsApp]
    end
    
    subgraph Report["Report Layer"]
        GN[GeradorDeNota\ngerarHtml()]
    end

    FP --> PR
    FP -.->|depende de abstração| N
    N <|.. NE
    N <|.. NS
    N <|.. NW
    PR --> P
    NE --> P
    NS --> P
    NW --> P
    GN --> P
    
    style Domain fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    style Application fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    style Infra fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    style Notification fill:#e8f5e9,stroke:#388e3c,stroke-width:2px
    style Report fill:#fffde7,stroke:#fbc02d,stroke-width:2px
```

**Legenda das cores:**
- 🔵 **Azul (Domain)**: Núcleo do negócio - regras que mudam por área comercial
- 🟠 **Laranja (Application)**: Orquestração - muda quando fluxo do caso de uso muda
- 🟣 **Roxo (Infra)**: Persistência - muda por DBA/infraestrutura
- 🟢 **Verde (Notification)**: Canais de comunicação - muda por Marketing
- 🟡 **Amarelo (Report)**: Relatórios fiscais - muda por Financeiro/Fiscal

> **Nota**: As setas tracejadas (`-.->`) indicam dependência de **abstração** (interface `Notificador`), não de implementação concreta. Isso é o **DIP (Dependency Inversion Principle)** em ação - preparando o terreno para o **D** do SOLID.

## 🧪 Testes que Provam o Ganho

| Teste | Antes (`before`) | Depois (`after`) |
|-------|------------------|------------------|
| Calcular total com desconto VIP | Precisa de banco/SMTP simulados | **Teste puro, sem dependências** |
| Trocar e-mail por SMS | Altera a classe `Pedido` | **Nova implementação de notificador** |
| Mudar layout da nota | Risco de afetar o cálculo | **Só `GeradorDeNota` é alterada** |

## ▶️ Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.8+

### Opção 1: Maven (Recomendado) - Executa tudo incluindo qualidade

```bash
# Compilar e testar tudo (before, after, acceptance-test)
mvn clean test

# Análise estática completa
mvn verify spotbugs:check checkstyle:check pmd:check

# Relatório de cobertura JaCoCo
mvn verify jacoco:report
# Abre target/site/jacoco/index.html
```

### Opção 2: Maven por módulo

```bash
# Versão before (viola SRP)
cd before && mvn test

# Versão after (respeita SRP)  
cd after && mvn test

# Testes de aceitação compartilhados (compara before vs after)
cd acceptance-test && mvn test
```

### Opção 3: Compilação manual (sem Maven)

```bash
# Versão before
cd before
javac -d out src/main/java/com/example/solid/before/*.java
# Requer JUnit no classpath para testes

# Versão after
cd after
javac -d out src/main/java/com/example/solid/after/**/*.java
```

### SonarQube Local (Docker)

```bash
# Subir SonarQube
docker run -d --name sonarqube -p 9000:9000 sonarqube:lts-community

# Aguardar subir (~2 min), acessar http://localhost:9000 (admin/admin)
# Criar projeto "solid-01-srp", gerar token

# Analisar
mvn sonar:sonar -Dsonar.projectKey=solid-01-srp -Dsonar.host.url=http://localhost:9000 -Dsonar.token=SEU_TOKEN
```

## 📊 Métricas de Qualidade Esperadas

| Métrica | `before` (Viola SRP) | `after` (Respeita SRP) |
|---------|---------------------|------------------------|
| **Classes** | 3 | 8 |
| **Métodos por classe** | ~5 (Pedido) | 1-3 |
| **Complexidade Ciclomática (Pedido)** | Alta (~8) | Baixa (~2) |
| **Acoplamento (CBO - Pedido)** | Alto (4+) | Baixo (0-1) |
| **Coesão (LCOM4 - Pedido)** | Baixa | Alta |
| **Testabilidade (mocks necessários)** | 3+ mocks | 0 mocks (domínio) |
| **Cobertura de testes** | Parcial | 100% domínio |

> Execute `mvn verify` e veja os relatórios em `target/site/` de cada módulo.

## 🎓 Exemplos Didáticos Adicionais (na pasta `after/`)

### 1. Domínio Anêmico vs Rico (`domain/PedidoAnemico.java` vs `domain/PedidoRico.java`)

```java
// ❌ ANÊMICO - Apenas getters/setters, regras vazam para Service
public class PedidoAnemico {
    public void setTotal(double total) { this.total = total; } // Perigoso!
    public void setStatus(String status) { this.status = status; } // Sem validação!
}

// ✅ RICO/EQUILIBRADO - Encapsula estado + comportamento coeso
public class PedidoRico {
    public void confirmar() { ... }        // Transição controlada
    public void adicionarItem(Item item) { ... } // Protege invariantes
    public double calcularTotal() { ... }  // Regra pura, testável
}
```

| Aspecto | Anêmico (❌) | Rico/Equilibrado (✅) |
|---------|-------------|----------------------|
| Estado inválido | Possível | Impossível |
| Regras de negócio | Em Service externo | Na entidade (coeso) |
| Transições de estado | Livres (`setStatus`) | Controladas (`confirmar()`) |
| Testabilidade | Precisa de Service | Teste direto na entidade |

> **Lição**: "Responsabilidade única" ≠ "Um método só". `PedidoRico` tem vários métodos, mas TODOS servem à mesma responsabilidade: *"Gerenciar ciclo de vida e regras do pedido"*.

### 2. Logger/Config no Domínio (Anti-pattern Sutil) - `domain/PedidoComInfra.java`

```java
// ❌ VIOLAÇÃO: Domínio conhece infraestrutura
public class PedidoComInfra {
    private final Logger logger;      // ❌ Domínio não deve logar
    private final AppConfig config;   // ❌ Domínio não deve ler config
    
    public double calcularTotal() {
        logger.info("Calculando...");  // Responsabilidade da app/infra
        double desconto = config.getDouble("desconto.vip"); // Responsabilidade da app
    }
}

// ✅ CORRETO: Domínio puro
public class PedidoCorreto {
    public double calcularTotal() { ... } // Zero logging, zero config
}
```

**Onde cada coisa pertence:**
- **Domain**: Zero logging, zero config — só regras puras
- **Application** (`FinalizarPedido`): Log de auditoria, feature flags, timeouts
- **Infra** (`PedidoRepository`): Log de SQL, performance, connection pool

## 📋 ADRs (Architecture Decision Records)

Decisões arquiteturais documentadas em [`docs/adr/`](docs/adr/):

| ADR | Título | Resumo |
|-----|--------|--------|
| [001](docs/adr/001-separar-responsabilidades-pedido.md) | Separar Responsabilidades do Pedido | Extração de 4 responsabilidades em 5 classes |
| [002](docs/adr/002-injecao-dependencia-construtor.md) | Injeção de Dependência pelo Construtor | Imutabilidade, testabilidade, prepara DIP |
| [003](docs/adr/003-nao-criar-interfaces-prematuras.md) | Não Criar Interfaces Prematuras (YAGNI) | Interface só quando 2ª implementação surgir |

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