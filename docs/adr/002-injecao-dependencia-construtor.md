# ADR 002: Injeção de Dependência pelo Construtor

## Status
Aceito

## Contexto
Após separar as responsabilidades (ADR 001), as classes precisam colaborar. `FinalizarPedido` precisa de `PedidoRepository` e `Notificador`. Como fornecer essas dependências?

## Decisão
Usar **injeção de dependência pelo construtor** (constructor injection) em todas as classes que dependem de outras:

```java
public class FinalizarPedido {
    private final PedidoRepository repository;
    private final Notificador notificador;
    
    public FinalizarPedido(PedidoRepository repository, Notificador notificador) {
        this.repository = repository;
        this.notificador = notificador;
    }
}
```

## Consequências

### Positivas
- ✅ **Imutabilidade**: dependências são `final`, definidas na construção
- ✅ **Testabilidade**: fácil substituir por mocks/stubs nos testes
- ✅ **Explícito**: dependências visíveis na assinatura do construtor
- ✅ **Prepara DIP**: `FinalizarPedido` depende de `Notificador` (interface), não de `NotificadorDePedido` (concreto)
- ✅ **Falha rápida**: se dependência faltar, erro no startup, não em runtime

### Negativas
- ⚠️ Construtores podem ficar longos com muitas dependências
- ⚠️ Requer fábrica/container para montar o grafo (manual ou framework)

## Alternativas Consideradas
1. **Setter Injection** - Rejeitado: permite objeto em estado inválido, dependências opcionais confusas
2. **Field Injection (@Autowired)** - Rejeitado: oculto, difícil testar sem container, imutabilidade quebrada
3. **Service Locator** - Rejeitado: anti-pattern, acoplamento oculto, difícil rastrear dependências
4. **Factory Methods** - Considerado para casos complexos, mas construtor é mais simples e direto

## Referências
- Martin Fowler — *Inversion of Control Containers and the Dependency Injection pattern*
- Robert C. Martin — *Clean Architecture*, Cap. 11 (DIP)