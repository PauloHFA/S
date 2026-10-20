# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [1.0.0] - 2026-10-20

### Adicionado
- Implementação da versão `before` que viola o SRP
  - `Pedido` com 4 responsabilidades: cálculo, persistência, notificação, relatório
  - `Item` e `Cliente` como classes de apoio
  - Testes unitários para `calcularTotal()`
- Implementação da versão `after` que respeita o SRP
  - `domain/Pedido` — apenas regras de negócio
  - `domain/Item` e `domain/Cliente` — entidades do domínio
  - `infra/PedidoRepository` — apenas persistência
  - `notification/NotificadorDePedido` — apenas notificação
  - `report/GeradorDeNota` — apenas geração de relatório
  - `application/FinalizarPedido` — orquestração do caso de uso
  - Testes unitários puros para regra de negócio (sem mocks)
  - Teste de integração para o fluxo completo
- Documentação completa
  - `README.md` com explicação do problema, solução e como executar
  - `docs/srp.md` com documentação detalhada do princípio
- Estrutura de pastas comparativa lado a lado (`before/` e `after/`)

### Características da Refatoração
- Cada classe do `after` descrita em uma frase sem "e"
- Injeção de dependência pelo construtor
- Nomes expressivos sem sufixos genéricos (Manager, Helper, Utils)
- Código simples, sem frameworks desnecessários
- Mesmos testes de aceitação passando em ambas as versões