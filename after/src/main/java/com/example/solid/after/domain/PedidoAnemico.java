package com.example.solid.after.domain;

import java.util.List;

/**
 * ❌ ANTI-PATTERN: Domínio Anêmico (Anemic Domain Model)
 * 
 * PROBLEMA: A entidade vira apenas um "DTO com getters/setters".
 * Toda regra de negócio vaza para Services/UseCases externos.
 * 
 * SINTOMAS:
 * - Apenas getters/setters, nenhum comportamento
 * - Regras de negócio em classes "Service" separadas
 * - Entidade não protege seus invariantes
 * - Fácil criar estado inválido (ex: pedido sem itens, total negativo)
 */
public class PedidoAnemico {
    private List<Item> itens;
    private Cliente cliente;
    private double total; // Calculado externamente!
    private String status; // Modificado externamente!
    
    // Construtor vazio ou apenas setters
    public PedidoAnemico() {}
    
    // Getters e setters públicos - zero encapsulamento
    public List<Item> getItens() { return itens; }
    public void setItens(List<Item> itens) { this.itens = itens; }
    
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; } // Perigoso!
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; } // Perigoso!
    
    // PROBLEMA: Cliente pode fazer isso:
    // pedido.setTotal(-100); // Total negativo?
    // pedido.setStatus("ENTREGUE"); // Sem validação de transição?
    // pedido.setItens(null); // NPE depois?
}

/**
 * Service que contém a regra de negócio (vazou do domínio)
 * Agora o domínio é passivo, só carrega dados.
 */
class PedidoServiceAnemico {
    public double calcularTotal(PedidoAnemico pedido) {
        // Regra de negócio FORA da entidade
        double subtotal = pedido.getItens().stream()
                .mapToDouble(Item::getSubtotal)
                .sum();
        double total = pedido.getCliente().isVip() ? subtotal * 0.9 : subtotal;
        pedido.setTotal(total); // Mutação externa
        return total;
    }
    
    public void finalizar(PedidoAnemico pedido) {
        // Lógica de transição de estado FORA da entidade
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new IllegalStateException("Pedido sem itens");
        }
        calcularTotal(pedido);
        pedido.setStatus("CONFIRMADO");
    }
}