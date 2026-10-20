# ADR 001: Separar Responsabilidades da Classe Pedido

## Status
Aceito

## Contexto
A classe `Pedido` original (versão `before`) continha 4 responsabilidades distintas:
1. Regras de negócio (cálculo de total, desconto VIP)
2. Persistência (salvar no banco de dados)
3. Notificação (enviar e-mail de confirmação)
4. Geração de relatório (nota fiscal em HTML)

Cada responsabilidade atende a um **ator diferente** (área comercial, DBA/infra, marketing, financeiro/fiscal) e muda por **motivos diferentes**.

## Decisão
Separar a classe `Pedido` em 5 classes, cada uma com uma única responsabilidade:

| Classe | Responsabilidade | Ator | Pacote |
|--------|------------------|------|--------|
| `Pedido` (domain) | Regras de negócio do pedido | Comercial | `domain` |
| `PedidoRepository` | Persistência | Infra/DBA | `infra` |
| `NotificadorDePedido` | Notificação via e-mail | Marketing | `notification` |
| `GeradorDeNota` | Geração de relatório/nota fiscal | Financeiro/Fiscal | `report` |
| `FinalizarPedido` | Orquestração do caso de uso | Arquitetura | `application` |

## Consequências

### Positivas
- ✅ Cada classe tem **um único motivo para mudar**
- ✅ Regra de negócio testável **sem mocks** de infraestrutura
- ✅ Trocar banco de dados não afeta cálculo de total
- ✅ Adicionar novo canal de notificação (SMS, WhatsApp) não toca no domínio
- ✅ Preparado para **OCP** (novos notificadores) e **DIP** (depende de abstração)

### Negativas
- ⚠️ Mais classes para gerenciar (complexidade estrutural)
- ⚠️ Necessita orquestrador (`FinalizarPedido`) para coordenar fluxo
- ⚠️ Curva de aprendizado para desenvolvedores acostumados com "God Classes"

## Alternativas Consideradas
1. **Manter classe única com métodos privados** - Rejeitado: não resolve acoplamento, testes ainda precisam de mocks
2. **Service único com @Transactional** - Rejeitado: viola SRP, mesma classe muda por motivos diferentes
3. **Eventos de domínio** - Considerado para evolução futura, mas adiciona complexidade desnecessária agora

## Referências
- Robert C. Martin — *Clean Architecture*, Cap. 7 (SRP)
- Martin Fowler — *Extract Class* refactoring