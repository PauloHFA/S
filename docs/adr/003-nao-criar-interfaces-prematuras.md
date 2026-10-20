# ADR 003: Não Criar Interfaces Prematuras (YAGNI)

## Status
Aceito

## Contexto
Ao aplicar SRP e DIP, há tentação de criar interfaces para **todas** as classes:
- `PedidoRepository` → `PedidoRepositoryInterface`
- `GeradorDeNota` → `GeradorDeNotaInterface`
- `Pedido` → `PedidoInterface`

Mas: **hoje só existe uma implementação** para cada. Criar interfaces "por precaução" viola YAGNI (You Aren't Gonna Need It).

## Decisão
**Criar interfaces apenas quando houver necessidade real de múltiplas implementações:**

| Classe | Interface? | Justificativa |
|--------|------------|---------------|
| `Notificador` | ✅ **Sim** | Já existem 3 implementações: E-mail, SMS, WhatsApp |
| `PedidoRepository` | ❌ Não | Só 1 implementação (JDBC). Se trocar para JPA/Mongo, refatorar depois |
| `GeradorDeNota` | ❌ Não | Só 1 formato (HTML). Se precisar PDF, criar interface na hora |
| `Pedido` (domain) | ❌ Não | Entidade de domínio rica, não é serviço |

**Regra prática**: "Extraia interface quando a **segunda implementação** surgir, não antes."

## Consequências

### Positivas
- ✅ Menos arquivos, menos complexidade
- ✅ Código mais direto, fácil de navegar
- ✅ Evita "Interface Explosion" (muitas interfaces com 1 implementação)
- ✅ Refatoração posterior é trivial (Extract Interface no IDE)

### Negativas
- ⚠️ Se precisar de segunda implementação depois, exige refatoração
- ⚠️ Testes de unidade da classe que usa a dependência precisam de mock da classe concreta (Mockito faz isso bem)

## Exceção: `Notificador`
Criamos `Notificador` (interface) **antes** da segunda implementação porque:
1. O domínio **exige** múltiplos canais (requisito de negócio conhecido)
2. Marketing **já pediu** SMS e WhatsApp no backlog
3. Demonstra **OCP/DIP** didaticamente no exemplo

## Referências
- Ron Jeffries — *YAGNI* (Extreme Programming)
- Martin Fowler — *Yagni* (martinfowler.com/bliki/Yagni.html)
- Robert C. Martin — *Clean Architecture*: "Interfaces belong to the client, not the server"