package com.example.solid.after.domain;

import java.util.List;

/**
 * ❌ ANTI-PATTERN SUTIL: Logger e Config injetados na Entidade de Domínio
 * 
 * VIOLAÇÃO: Entidade de domínio conhece infraestrutura (logging, configuração).
 * Isso acopla o núcleo do negócio a detalhes técnicos.
 * 
 * POR QUE É RUIM:
 * 1. Domínio não deve saber como logar (SLF4J, Log4j, System.out)
 * 2. Domínio não deve saber de onde vem config (arquivo, env var, Consul)
 * 3. Testes unitários precisam mockar Logger/Config - poluição
 * 4. Viola DIP: domínio (alto nível) depende de infra (baixo nível)
 * 5. Viola SRP: entidade agora tem motivo extra para mudar (troca de lib de log)
 */
public class PedidoComInfra {
    private final List<Item> itens;
    private final Cliente cliente;
    
    // ❌ VIOLAÇÃO: Logger injetado na entidade
    private final Logger logger;
    
    // ❌ VIOLAÇÃO: Config injetado na entidade
    private final AppConfig config;
    
    public PedidoComInfra(List<Item> itens, Cliente cliente, Logger logger, AppConfig config) {
        this.itens = itens;
        this.cliente = cliente;
        this.logger = logger;
        this.config = config;
    }
    
    public double calcularTotal() {
        // ❌ Domínio fazendo logging - responsabilidade da infra/app
        logger.info("Calculando total para cliente {}", cliente.getId());
        
        double subtotal = itens.stream()
                .mapToDouble(Item::getSubtotal)
                .sum();
        
        // ❌ Domínio lendo config - responsabilidade da app
        double descontoVip = config.getDouble("desconto.vip.percentual", 0.1);
        double total = cliente.isVip() ? subtotal * (1 - descontoVip) : subtotal;
        
        logger.debug("Subtotal: {}, Desconto VIP: {}, Total: {}", subtotal, descontoVip, total);
        return total;
    }
    
    public void salvar() {
        logger.info("Salvando pedido no banco...");
        // Simula JDBC
        logger.debug("SQL: INSERT INTO pedidos...");
    }
}

// Interfaces de infraestrutura (não pertencem ao domínio)
interface Logger {
    void info(String msg, Object... args);
    void debug(String msg, Object... args);
}

interface AppConfig {
    double getDouble(String key, double defaultValue);
    String getString(String key);
}

/**
 * ✅ CORRETO: Domínio puro, sem infraestrutura
 * 
 * - Logger vai na camada de Application/Infra (FinalizarPedido, Repository)
 * - Config vai na camada de Application (lida no startup, injetada como valor)
 * - Domínio só tem: dados + regras de negócio puras
 */
class PedidoCorreto {
    private final List<Item> itens;
    private final Cliente cliente;
    
    public PedidoCorreto(List<Item> itens, Cliente cliente) {
        this.itens = itens;
        this.cliente = cliente;
    }
    
    // Puro, testável, sem mocks
    public double calcularTotal() {
        double subtotal = itens.stream()
                .mapToDouble(Item::getSubtotal)
                .sum();
        return cliente.isVip() ? subtotal * 0.9 : subtotal;
    }
}

/**
 * Onde o logging/config pertencem:
 * 
 * Application Layer (FinalizarPedido):
 *   - Logger: log de auditoria, início/fim de caso de uso
 *   - Config: timeouts, feature flags, URLs externas
 * 
 * Infra Layer (PedidoRepository):
 *   - Logger: SQL executado, performance, erros de conexão
 *   - Config: connection pool, dialect SQL
 * 
 * Domain Layer (Pedido):
 *   - ZERO logging, ZERO config
 *   - Só: calcularTotal(), adicionarItem(), confirmar(), etc.
 */