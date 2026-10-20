package com.example.solid.after.domain;

import java.util.List;

public class Pedido {
    private final List<Item> itens;
    private final Cliente cliente;

    public Pedido(List<Item> itens, Cliente cliente) {
        this.itens = itens;
        this.cliente = cliente;
    }

    public List<Item> getItens() {
        return itens;
    }

    public Cliente getCliente() {
        return cliente;
    }

    // Responsabilidade única: regras de negócio do pedido
    public double calcularTotal() {
        double subtotal = itens.stream()
                .mapToDouble(Item::getSubtotal)
                .sum();
        return cliente.isVip() ? subtotal * 0.9 : subtotal;
    }
}