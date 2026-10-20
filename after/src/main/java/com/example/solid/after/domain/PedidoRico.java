package com.example.solid.after.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ✅ EQUILÍBRIO CORRETO: Domínio Rico (Rich Domain Model) com SRP
 * 
 * PRINCÍPIOS:
 * 1. Entidade encapsula estado E comportamento coeso
 * 2. Protege invariantes (não permite estado inválido)
 * 3. Expõe apenas operações de negócio significativas
 * 4. SRP: Responsabilidade única = "Gerenciar ciclo de vida do pedido"
 *    (não "fazer tudo", mas "ser o dono das regras do pedido")
 */
public class PedidoRico {
    private final List<Item> itens;
    private final Cliente cliente;
    private String status;
    private double totalCalculado;
    
    // Fábrica - garante criação válida
    public static PedidoRico criarNovo(Cliente cliente, List<Item> itens) {
        if (cliente == null) throw new IllegalArgumentException("Cliente obrigatório");
        if (itens == null || itens.isEmpty()) throw new IllegalArgumentException("Pedido deve ter itens");
        
        PedidoRico pedido = new PedidoRico(cliente, new ArrayList<>(itens));
        pedido.recalcularTotal();
        return pedido;
    }
    
    // Construtor privado - força uso da fábrica
    private PedidoRico(Cliente cliente, List<Item> itens) {
        this.cliente = cliente;
        this.itens = itens;
        this.status = "RASCUNHO";
        this.totalCalculado = 0;
    }
    
    // === Comportamentos de negócio (não getters/setters anêmicos) ===
    
    /** Adiciona item - protege invariante: só em RASCUNHO */
    public void adicionarItem(Item item) {
        validarPodeModificar();
        if (item == null) throw new IllegalArgumentException("Item não pode ser nulo");
        this.itens.add(item);
        this.recalcularTotal();
    }
    
    /** Remove item - protege invariante: só em RASCUNHO, mínimo 1 item */
    public void removerItem(Item item) {
        validarPodeModificar();
        if (this.itens.size() <= 1) throw new IllegalStateException("Pedido deve ter pelo menos 1 item");
        this.itens.remove(item);
        this.recalcularTotal();
    }
    
    /** Confirma pedido - transição de estado controlada */
    public void confirmar() {
        if (!"RASCUNHO".equals(status)) {
            throw new IllegalStateException("Só pedidos em RASCUNHO podem ser confirmados. Status: " + status);
        }
        if (itens.isEmpty()) throw new IllegalStateException("Pedido sem itens");
        this.status = "CONFIRMADO";
        this.recalcularTotal();
    }
    
    /** Cancela pedido - regra de negócio encapsulada */
    public void cancelar(String motivo) {
        if ("ENTREGUE".equals(status)) {
            throw new IllegalStateException("Pedido entregue não pode ser cancelado");
        }
        this.status = "CANCELADO";
        // totalCalculado mantido para auditoria
    }
    
    /** Marca como entregue - transição válida */
    public void marcarEntregue() {
        if (!"CONFIRMADO".equals(status)) {
            throw new IllegalStateException("Só pedidos CONFIRMADOS podem ser entregues. Status: " + status);
        }
        this.status = "ENTREGUE";
    }
    
    // === Regras de negócio puras (sem side effects) ===
    
    /** Calcula total - regra de negócio pura, testável isoladamente */
    public double calcularTotal() {
        double subtotal = itens.stream()
                .mapToDouble(Item::getSubtotal)
                .sum();
        return cliente.isVip() ? subtotal * 0.9 : subtotal;
    }
    
    /** Verifica se cliente tem desconto VIP */
    public boolean temDescontoVip() {
        return cliente.isVip();
    }
    
    /** Verifica se pedido pode ser modificado */
    public boolean podeSerModificado() {
        return "RASCUNHO".equals(status);
    }
    
    // === Acesso controlado (não expõe mutabilidade) ===
    
    public List<Item> getItens() {
        return Collections.unmodifiableList(itens); // Protege lista interna
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public String getStatus() {
        return status;
    }
    
    public double getTotal() {
        return totalCalculado;
    }
    
    // === Privados ===
    
    private void recalcularTotal() {
        this.totalCalculado = calcularTotal();
    }
    
    private void validarPodeModificar() {
        if (!podeSerModificado()) {
            throw new IllegalStateException("Pedido " + status + " não pode ser modificado");
        }
    }
}

/**
 * Comparação rápida:
 * 
 * | Aspecto              | Anêmico (❌)           | Rico/Equilibrado (✅)        |
 * |----------------------|------------------------|------------------------------|
 * | Estado inválido      | Possível (setTotal(-1))| Impossível (encapsulado)     |
 * | Regras de negócio    | Em Service externo     | Na entidade (coeso)          |
 * | Transições de estado | Livres (setStatus)     | Controladas (confirmar())    |
 * | Testabilidade        | Precisa de Service     | Teste direto na entidade     |
 * | SRP                  | Service faz tudo       | Entidade = regras do pedido  |
 * 
 * O segredo: "Responsabilidade única" ≠ "Um método só".
 * PedidoRico tem vários métodos, mas TODOS servem à mesma responsabilidade:
 * "Gerenciar o ciclo de vida e regras do pedido".
 */